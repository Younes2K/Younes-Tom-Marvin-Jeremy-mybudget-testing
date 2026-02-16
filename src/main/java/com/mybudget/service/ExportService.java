package com.mybudget.service;

import com.mybudget.model.Transaction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ExportService {

    public void exporterVersCSV(List<Transaction> transactions, String cheminFichier) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cheminFichier))) {
            // Écrire l'en-tête
            writer.write("ID,Catégorie,Montant,Description,Date");
            writer.newLine();

            // Écrire chaque transaction
            for (Transaction transaction : transactions) {
                writer.write(formaterLigneCSV(transaction));
                writer.newLine();
            }
        }
    }

    private String formaterLigneCSV(Transaction transaction) {
        return String.format("%d,%s,%s,%s,%s",
            transaction.getId(),
            transaction.getCategorie(),
            transaction.getMontant().toPlainString(),
            echapperChampCSV(transaction.getDescription()),
            transaction.getDate().toString()
        );
    }

    private String echapperChampCSV(String champ) {
        if (champ == null) {
            return "";
        }

        // Si le champ contient une virgule ou des guillemets, il faut l'entourer de guillemets
        if (champ.contains(",") || champ.contains("\"")) {
            // Doubler les guillemets existants
            String champEchappe = champ.replace("\"", "\"\"");
            return "\"" + champEchappe + "\"";
        }

        return champ;
    }
}
