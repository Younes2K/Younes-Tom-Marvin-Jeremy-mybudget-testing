package com.mybudget.service;

import com.mybudget.model.Transaction;
import com.mybudget.model.ValidationException;
import com.mybudget.repository.DatabaseManager;
import com.mybudget.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class TransactionServiceTest {
    private TransactionService transactionService;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        // Utiliser une base de données temporaire pour les tests
        String dbUrl = "jdbc:sqlite:test_" + System.nanoTime() + ".db";
        databaseManager = new DatabaseManager(dbUrl);
        TransactionRepository transactionRepository = new TransactionRepository(databaseManager);
        transactionService = new TransactionService(transactionRepository);
    }

    @Test
    void ajouterTransaction_devrait_rejeter_categorie_nulle() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction(null, new BigDecimal("100"), "Description", LocalDate.now())
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void ajouterTransaction_devrait_rejeter_categorie_vide_ou_blanche(String categorieInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction(categorieInvalide, new BigDecimal("100"), "Description", LocalDate.now())
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
    }

    @Test
    void ajouterTransaction_devrait_rejeter_montant_nul() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction("Alimentation", null, "Description", LocalDate.now())
        );
        assertEquals("Le montant doit être positif", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "-100", "-1000.50"})
    void ajouterTransaction_devrait_rejeter_montant_zero_ou_negatif(String montantInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction("Alimentation", new BigDecimal(montantInvalide), "Description", LocalDate.now())
        );
        assertEquals("Le montant doit être positif", exception.getMessage());
    }

    @Test
    void ajouterTransaction_devrait_rejeter_date_nulle() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction("Alimentation", new BigDecimal("100"), "Description", null)
        );
        assertEquals("La date ne peut pas être nulle", exception.getMessage());
    }

    @Test
    void ajouterTransaction_devrait_rejeter_date_future() {
        LocalDate dateFuture = LocalDate.now().plusDays(1);
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction("Alimentation", new BigDecimal("100"), "Description", dateFuture)
        );
        assertEquals("La date ne peut pas être dans le futur", exception.getMessage());
    }

    @Test
    void ajouterTransaction_devrait_enregistrer_transaction_valide() {
        Transaction resultat = transactionService.ajouterTransaction("Alimentation", new BigDecimal("50.00"), "Courses", LocalDate.now());

        assertNotNull(resultat);
        assertNotNull(resultat.getId());
        assertEquals("Alimentation", resultat.getCategorie());
        assertEquals(new BigDecimal("50.00"), resultat.getMontant());
    }

    @Test
    void ajouterTransaction_devrait_autoriser_description_nulle() {
        Transaction resultat = transactionService.ajouterTransaction("Transport", new BigDecimal("25.00"), null, LocalDate.now());

        assertNotNull(resultat);
        assertNull(resultat.getDescription());
    }

    @Test
    void ajouterTransaction_devrait_normaliser_categorie() {
        Transaction resultat = transactionService.ajouterTransaction("  Alimentation  ", new BigDecimal("50.00"), "Test", LocalDate.now());
        assertEquals("Alimentation", resultat.getCategorie());
    }

    @Test
    void listerTransactions_devrait_retourner_toutes_les_transactions() {
        transactionService.ajouterTransaction("Alimentation", new BigDecimal("50"), "Courses", LocalDate.now());
        transactionService.ajouterTransaction("Transport", new BigDecimal("30"), "Essence", LocalDate.now());

        List<Transaction> resultat = transactionService.listerTransactions();
        assertEquals(2, resultat.size());
    }

    @Test
    void listerTransactions_devrait_retourner_liste_vide_si_aucune_transaction() {
        List<Transaction> resultat = transactionService.listerTransactions();
        assertTrue(resultat.isEmpty());
    }

    @Test
    void listerTransactionsParCategorie_devrait_filtrer_par_categorie() {
        transactionService.ajouterTransaction("Alimentation", new BigDecimal("50"), "Courses", LocalDate.now());
        transactionService.ajouterTransaction("Transport", new BigDecimal("30"), "Essence", LocalDate.now());
        transactionService.ajouterTransaction("Alimentation", new BigDecimal("25"), "Restaurant", LocalDate.now());

        List<Transaction> resultat = transactionService.listerTransactionsParCategorie("Alimentation");

        assertEquals(2, resultat.size());
        assertTrue(resultat.stream().allMatch(t -> t.getCategorie().equals("Alimentation")));
    }

    @Test
    void calculerTotalParCategorie_devrait_sommer_montants() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();
        transactionService.ajouterTransaction("Alimentation", new BigDecimal("50.00"), "Courses", LocalDate.now());
        transactionService.ajouterTransaction("Alimentation", new BigDecimal("25.50"), "Restaurant", LocalDate.now());

        BigDecimal total = transactionService.calculerTotalParCategorie("Alimentation", mois, annee);
        assertEquals(0, new BigDecimal("75.50").compareTo(total));
    }

    @Test
    void calculerTotalParCategorie_devrait_retourner_zero_si_aucune_transaction() {
        BigDecimal total = transactionService.calculerTotalParCategorie("Transport", 1, 2024);
        assertEquals(BigDecimal.ZERO, total);
    }

    @Test
    void supprimerTransaction_devrait_supprimer_transaction_existante() {
        Transaction transaction = transactionService.ajouterTransaction("Test", new BigDecimal("100"), "Test", LocalDate.now());
        
        transactionService.supprimerTransaction(transaction.getId());

        List<Transaction> transactions = transactionService.listerTransactions();
        assertTrue(transactions.isEmpty());
    }

    @Test
    void supprimerTransaction_devrait_rejeter_id_nul() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.supprimerTransaction(null)
        );
        assertEquals("L'identifiant ne peut pas être nul", exception.getMessage());
    }

    @Test
    void modifierTransaction_devrait_modifier_transaction_valide() {
        Transaction transaction = transactionService.ajouterTransaction("Alimentation", new BigDecimal("50"), "Test", LocalDate.now());
        
        transactionService.modifierTransaction(transaction.getId(), "Transport", new BigDecimal("75.00"), "Modifié", LocalDate.now());

        List<Transaction> transactions = transactionService.listerTransactions();
        assertEquals(1, transactions.size());
        assertEquals("Transport", transactions.get(0).getCategorie());
        assertEquals(0, new BigDecimal("75.00").compareTo(transactions.get(0).getMontant()));
    }

    @Test
    void modifierTransaction_devrait_rejeter_id_nul() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.modifierTransaction(null, "Alimentation", new BigDecimal("100"), "Test", LocalDate.now())
        );
        assertEquals("L'identifiant ne peut pas être nul", exception.getMessage());
    }

    @Test
    void modifierTransaction_devrait_valider_categorie() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.modifierTransaction(1L, "", new BigDecimal("100"), "Test", LocalDate.now())
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
    }

    @Test
    void modifierTransaction_devrait_valider_montant() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.modifierTransaction(1L, "Alimentation", new BigDecimal("-50"), "Test", LocalDate.now())
        );
        assertEquals("Le montant doit être positif", exception.getMessage());
    }

    @Test
    void modifierTransaction_devrait_valider_date() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.modifierTransaction(1L, "Alimentation", new BigDecimal("100"), "Test", null)
        );
        assertEquals("La date ne peut pas être nulle", exception.getMessage());
    }
}
