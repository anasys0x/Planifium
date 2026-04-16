package com.diro.ift2255;

import com.diro.ift2255.config.Routes;
import com.diro.ift2255.discord.ReviewBot;
import com.diro.ift2255.service.ReviewService;

import io.javalin.Javalin;




/**
 * Point d’entree de l’application.
 *
 * Demarre le serveur HTTP (Javalin), enregistre les routes et initialise le bot Discord.
 *
 * Le bot Discord est optionnel. Pour l’activer, definir les variables d’environnement :
 *   DISCORD_BOT_TOKEN     - le token du bot Discord
 *   DISCORD_CHANNEL_ID    - l’ID du canal de reviews
 *
 * Sans ces variables, le serveur demarre normalement sans le bot.
 */
public class Main {
    public static void main(String[] args) {

        ReviewService reviewService = new ReviewService();

        String token = System.getenv("DISCORD_BOT_TOKEN");
        String reviewChannelId = System.getenv("DISCORD_CHANNEL_ID");

        try {
            if (token != null && reviewChannelId != null) {
                ReviewBot.start(token, reviewService, reviewChannelId);
                System.out.println("Bot Discord démarré.");
            } else {
                System.out.println("⚠️ Bot Discord non démarré (token ou channel ID manquant)");
            }
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du démarrage du bot Discord : " + e.getMessage());
        }

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.staticFiles.add("/public");
        });

        Routes.register(app, reviewService);

        app.start(8070);
        app.get("/", ctx -> ctx.redirect("/index.html")); // J'ai essayer de render, mais ca ne marche pas du coup j'ai du redirect
        System.out.println("Serveur démarré sur http://localhost:8070");
    }
}
