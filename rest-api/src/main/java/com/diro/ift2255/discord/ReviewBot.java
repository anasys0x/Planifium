package com.diro.ift2255.discord;

import com.diro.ift2255.model.Review;
import com.diro.ift2255.model.dto.ReviewExtraction;
import com.diro.ift2255.service.LlmReviewParser;
import com.diro.ift2255.service.OllamaReviewParser;
import com.diro.ift2255.service.ReviewService;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReviewBot extends ListenerAdapter {

    private static final String TAG = "[RB] ";

    private static final String SYNTAX =
            TAG + "Syntaxe : `!review <codeCours> <difficulté 1-10> <commentaire>`\n" +
            TAG + "Exemple : `!review IFT2255 8 super cours`";

    private static final Pattern STRICT =
            Pattern.compile("^!review\\s+([A-Za-z]{3}\\d{4})\\s+(10|[1-9])\\s+(.+)$",
                    Pattern.CASE_INSENSITIVE);

    private static final Map<String, Long> SEEN = new ConcurrentHashMap<>();
    private static boolean alreadyHandled(String messageId) {
        long now = Instant.now().toEpochMilli();
        SEEN.entrySet().removeIf(e -> now - e.getValue() > 60_000);
        return SEEN.putIfAbsent(messageId, now) != null;
    }

    private final ReviewService reviewService;
    private final String channelId;
    private final LlmReviewParser llmParser;

    public ReviewBot(ReviewService service, String channelId, LlmReviewParser llmParser) {
        this.reviewService = service;
        this.channelId = channelId;
        this.llmParser = llmParser;
    }

    public static JDA start(String token, ReviewService service, String channelId) throws Exception {
        LlmReviewParser parser = new OllamaReviewParser("http://localhost:11434", "mistral:7b");
        return JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGES)
                .disableCache(CacheFlag.ACTIVITY)
                .addEventListeners(new ReviewBot(service, channelId, parser))
                .build();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(channelId)) return;
        if (alreadyHandled(event.getMessageId())) return;

        String msg = event.getMessage().getContentRaw().trim();

        if (
            msg.equalsIgnoreCase("!help") ||
            msg.equalsIgnoreCase("/help") ||
            msg.equalsIgnoreCase("!aide") ||
            msg.equalsIgnoreCase("/aide")
        ) {
            event.getChannel().sendMessage(
"```" +
"╔════════════════════════════════════╗\n" +
"║        BOT D’AVIS – COURS           ║\n" +
"╚════════════════════════════════════╝\n" +
"\n" +
"📌 Utilisation recommandée\n" +
"!review <codeCours> <difficulté 1-10> <commentaire>\n" +
"\n" +
"Exemple :\n" +
"!review IFT2255 8 super cours, prof sympa\n" +
"\n" +
"────────────────────────────────────\n" +
"🤖 Mode pseudo-conversationnel (LLM)\n" +
"\n" +
"Si la syntaxe n’est pas respectée, le bot\n" +
"tente d’interpréter automatiquement le\n" +
"message (cours, difficulté, commentaire).\n" +
"\n" +
"Exemple :\n" +
"!review J’ai trouvé IFT2255 intéressant\n" +
"mais demandant, je dirais 7/10\n" +
"\n" +
"ℹ️ Astuce : la syntaxe stricte reste\n" +
"la plus fiable et la plus précise.\n" +
"```"

            ).queue();
            return;
        }

        if (!msg.toLowerCase(Locale.ROOT).startsWith("!review")) return;

        // 1) Strict
        ParsedReview strict = parseStrict(msg);
        if (strict != null) {
            saveAndAck(event, strict.courseId(), strict.difficulty(), strict.comment());
            return;
        }

        // 2) LLM
        String rest = msg.substring("!review".length()).trim();
        if (rest.isEmpty()) {
            event.getChannel().sendMessage(TAG + "Il manque des infos.\n" + SYNTAX).queue();
            return;
        }

        event.getChannel().sendMessage(
                TAG + "Je ne reconnais pas la syntaxe exacte. J’essaie d’interpréter ton message…"
        ).queue();

        ParsedReview llm = parseWithLlm(rest);
        if (llm != null) {
            saveAndAck(event, llm.courseId(), llm.difficulty(), llm.comment());
            return;
        }

        event.getChannel().sendMessage(
                TAG + "Je n’ai pas réussi à extraire (cours / difficulté / commentaire).\n" + SYNTAX
        ).queue();
    }

    private ParsedReview parseStrict(String msg) {
        Matcher m = STRICT.matcher(msg);
        if (!m.matches()) return null;

        String courseId = m.group(1).toUpperCase(Locale.ROOT);
        int difficulty = Integer.parseInt(m.group(2));
        String comment = m.group(3).trim();

        if (comment.isEmpty()) return null;
        return new ParsedReview(courseId, difficulty, comment);
    }

    private ParsedReview parseWithLlm(String textAfterCommand) {
        ReviewExtraction ex = llmParser.extract(textAfterCommand);

        String courseId = ex.courseCode() == null ? null : ex.courseCode().trim().toUpperCase(Locale.ROOT);
        Integer difficulty = ex.difficulty();

        if (courseId == null || !courseId.matches("^[A-Z]{3}\\d{4}$")) return null;
        if (difficulty == null || difficulty < 1 || difficulty > 10) return null;

        // commentaire fidèle = texte original
        String comment = buildCommentFromOriginal(textAfterCommand, courseId, difficulty);
        if (comment.isBlank()) return null;

        return new ParsedReview(courseId, difficulty, comment);
    }

    private static String buildCommentFromOriginal(String original, String courseId, int difficulty) {
        String s = original.trim();

        s = s.replaceAll("(?i)\\b" + Pattern.quote(courseId) + "\\b", " ");
        s = s.replaceAll("(?i)\\b" + difficulty + "\\s*/\\s*10\\b", " ");
        s = s.replaceAll("(?i)\\b" + difficulty + "\\s*sur\\s*10\\b", " ");
        s = s.replaceAll("(?i)\\b(diffic(ulté|ulte)?)\\s*(est|=|:)\\s*" + difficulty + "\\b", " ");
        s = s.replaceAll("\\s{2,}", " ").trim();

        return s.isEmpty() ? original.trim() : s;
    }

    private void saveAndAck(MessageReceivedEvent event, String courseId, int difficulty, String comment) {
        String author = event.getAuthor().getName();
        reviewService.addReview(new Review(courseId, author, difficulty, comment));
        event.getChannel().sendMessage(TAG + "Merci pour ton avis sur " + courseId + " !").queue();
    }

    private record ParsedReview(String courseId, int difficulty, String comment) {}
}
