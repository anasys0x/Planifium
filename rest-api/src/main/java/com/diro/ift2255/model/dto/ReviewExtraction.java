package com.diro.ift2255.model.dto;

/**
 * Résultat d’extraction d’un avis à partir d’un texte libre.
 * courseCode : ex. IFT2255
 * difficulty : entier 1-10
 * comment : optionnel (on ne l’utilise pas forcément côté bot)
 */
public record ReviewExtraction(String courseCode, Integer difficulty, String comment) {}