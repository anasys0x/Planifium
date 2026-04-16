package com.diro.ift2255.repository;

import com.diro.ift2255.model.Review;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ReviewRepository {

    private static final String FILE_PATH = "data/reviews.xml";
    private final Object lock = new Object();

    public ReviewRepository() {
        initFileIfNeeded();
    }

    private void initFileIfNeeded() {
        try {
            File file = new File(FILE_PATH);
            if (file.exists()) return;

            file.getParentFile().mkdirs();

            DocumentBuilder builder = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder();

            Document doc = builder.newDocument();
            Element root = doc.createElement("reviews");
            doc.appendChild(root);

            saveDocument(doc);
        } catch (Exception e) {
            throw new RuntimeException("Erreur init XML", e);
        }
    }

    public void addReview(Review r) {
        synchronized (lock) {
            try {
                Document doc = loadDocument();
                Element root = doc.getDocumentElement();

                Element review = doc.createElement("review");

                review.appendChild(create(doc, "courseId", r.getCourseId().toUpperCase()));
                review.appendChild(create(doc, "author", r.getAuthor()));
                review.appendChild(create(doc, "difficulty", String.valueOf(r.getDifficulty())));
                review.appendChild(create(doc, "comment", r.getComment()));

                root.appendChild(review);
                saveDocument(doc);

            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public List<Review> getReviews(String courseId) {
        synchronized (lock) {
            List<Review> list = new ArrayList<>();

            try {
                Document doc = loadDocument();
                NodeList reviews = doc.getElementsByTagName("review");

                for (int i = 0; i < reviews.getLength(); i++) {
                    Element r = (Element) reviews.item(i);

                    String cid = text(r, "courseId");
                    if (!cid.equalsIgnoreCase(courseId)) continue;

                    list.add(new Review(
                            cid,
                            text(r, "author"),
                            Integer.parseInt(text(r, "difficulty")),
                            text(r, "comment")
                    ));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return list;
        }
    }

    /* ===== Helpers ===== */

    private Document loadDocument() throws Exception {
        DocumentBuilder builder = DocumentBuilderFactory
                .newInstance()
                .newDocumentBuilder();

        Document doc = builder.parse(new File(FILE_PATH));
        removeBlankTextNodes(doc.getDocumentElement());
        doc.normalizeDocument();

        return doc;
    }

    private void saveDocument(Document doc) throws TransformerException {
        // Nettoie avant d'écrire (évite l'explosion de whitespace)
        removeBlankTextNodes(doc.getDocumentElement());
        doc.normalizeDocument();

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

        // Indentation
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        transformer.transform(
                new DOMSource(doc),
                new StreamResult(new File(FILE_PATH))
        );
    }

    private Element create(Document doc, String tag, String value) {
        Element e = doc.createElement(tag);
        e.setTextContent(value);
        return e;
    }

    private String text(Element parent, String tag) {
        return parent.getElementsByTagName(tag).item(0).getTextContent();
    }

    private void removeBlankTextNodes(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);

            if (child.getNodeType() == Node.TEXT_NODE) {
                if (child.getTextContent().trim().isEmpty()) {
                    node.removeChild(child);
                }
            } else if (child.getNodeType() == Node.ELEMENT_NODE) {
                removeBlankTextNodes(child);
            }
        }
    }
}
