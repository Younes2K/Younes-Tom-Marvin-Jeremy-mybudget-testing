package com.mybudget.service;

import com.mybudget.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportServiceTest {
    private ExportService exportService;
    private Path fichierTemp;

    @BeforeEach
    void setUp() throws IOException {
        exportService = new ExportService();
        fichierTemp = Files.createTempFile("test-export-", ".csv");
    }

    @Test
    void exporterVersCSV_devrait_creer_fichier_avec_entetes() throws IOException {
        List<Transaction> transactions = List.of();

        exportService.exporterVersCSV(transactions, fichierTemp.toString());

        assertTrue(Files.exists(fichierTemp));
        List<String> lignes = Files.readAllLines(fichierTemp);
        assertEquals(1, lignes.size());
        assertEquals("ID,Catégorie,Montant,Description,Date", lignes.get(0));
    }

    @Test
    void exporterVersCSV_devrait_exporter_une_transaction() throws IOException {
        Transaction transaction = new Transaction(1L, "Alimentation", new BigDecimal("50.00"), "Courses", LocalDate.of(2024, 1, 15));
        List<Transaction> transactions = List.of(transaction);

        exportService.exporterVersCSV(transactions, fichierTemp.toString());

        List<String> lignes = Files.readAllLines(fichierTemp);
        assertEquals(2, lignes.size());
        assertEquals("ID,Catégorie,Montant,Description,Date", lignes.get(0));
        assertEquals("1,Alimentation,50.00,Courses,2024-01-15", lignes.get(1));
    }

    @Test
    void exporterVersCSV_devrait_exporter_plusieurs_transactions() throws IOException {
        List<Transaction> transactions = Arrays.asList(
            new Transaction(1L, "Alimentation", new BigDecimal("50.00"), "Courses", LocalDate.of(2024, 1, 15)),
            new Transaction(2L, "Transport", new BigDecimal("25.50"), "Essence", LocalDate.of(2024, 1, 20)),
            new Transaction(3L, "Loisirs", new BigDecimal("100.00"), "Cinéma", LocalDate.of(2024, 1, 25))
        );

        exportService.exporterVersCSV(transactions, fichierTemp.toString());

        List<String> lignes = Files.readAllLines(fichierTemp);
        assertEquals(4, lignes.size());
        assertEquals("ID,Catégorie,Montant,Description,Date", lignes.get(0));
        assertEquals("1,Alimentation,50.00,Courses,2024-01-15", lignes.get(1));
        assertEquals("2,Transport,25.50,Essence,2024-01-20", lignes.get(2));
        assertEquals("3,Loisirs,100.00,Cinéma,2024-01-25", lignes.get(3));
    }

    @Test
    void exporterVersCSV_devrait_gerer_description_nulle() throws IOException {
        Transaction transaction = new Transaction(1L, "Transport", new BigDecimal("30.00"), null, LocalDate.of(2024, 1, 10));
        List<Transaction> transactions = List.of(transaction);

        exportService.exporterVersCSV(transactions, fichierTemp.toString());

        List<String> lignes = Files.readAllLines(fichierTemp);
        assertEquals(2, lignes.size());
        assertEquals("1,Transport,30.00,,2024-01-10", lignes.get(1));
    }

    @Test
    void exporterVersCSV_devrait_echapper_virgules_dans_description() throws IOException {
        Transaction transaction = new Transaction(1L, "Alimentation", new BigDecimal("45.00"), "Courses, fruits, légumes", LocalDate.of(2024, 1, 10));
        List<Transaction> transactions = List.of(transaction);

        exportService.exporterVersCSV(transactions, fichierTemp.toString());

        List<String> lignes = Files.readAllLines(fichierTemp);
        assertEquals(2, lignes.size());
        assertEquals("1,Alimentation,45.00,\"Courses, fruits, légumes\",2024-01-10", lignes.get(1));
    }

    @Test
    void exporterVersCSV_devrait_echapper_guillemets_dans_description() throws IOException {
        Transaction transaction = new Transaction(1L, "Loisirs", new BigDecimal("20.00"), "Livre \"Harry Potter\"", LocalDate.of(2024, 1, 10));
        List<Transaction> transactions = List.of(transaction);

        exportService.exporterVersCSV(transactions, fichierTemp.toString());

        List<String> lignes = Files.readAllLines(fichierTemp);
        assertEquals(2, lignes.size());
        assertEquals("1,Loisirs,20.00,\"Livre \"\"Harry Potter\"\"\",2024-01-10", lignes.get(1));
    }
}
