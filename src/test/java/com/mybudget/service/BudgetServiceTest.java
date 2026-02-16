package com.mybudget.service;

import com.mybudget.model.Budget;
import com.mybudget.model.ValidationException;
import com.mybudget.repository.BudgetRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BudgetServiceTest {
    private BudgetService budgetService;
    private TransactionService transactionService;
    private DatabaseManager databaseManager;

    @BeforeEach
    void setUp() {
        // Utiliser une base de données temporaire pour les tests
        String dbUrl = "jdbc:sqlite:test_" + System.nanoTime() + ".db";
        databaseManager = new DatabaseManager(dbUrl);
        BudgetRepository budgetRepository = new BudgetRepository(databaseManager);
        TransactionRepository transactionRepository = new TransactionRepository(databaseManager);
        transactionService = new TransactionService(transactionRepository);
        budgetService = new BudgetService(budgetRepository, transactionService);
    }

    @Test
    void definirBudget_devrait_rejeter_categorie_nulle() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget(null, 1, 2024, new BigDecimal("500"))
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void definirBudget_devrait_rejeter_categorie_vide_ou_blanche(String categorieInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget(categorieInvalide, 1, 2024, new BigDecimal("500"))
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 13, 100})
    void definirBudget_devrait_rejeter_mois_invalide(int moisInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget("Alimentation", moisInvalide, 2024, new BigDecimal("500"))
        );
        assertEquals("Le mois doit être entre 1 et 12", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(ints = {1999, 1900, 0, -2024})
    void definirBudget_devrait_rejeter_annee_invalide(int anneeInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget("Alimentation", 1, anneeInvalide, new BigDecimal("500"))
        );
        assertEquals("L'année doit être >= 2000", exception.getMessage());
    }

    @Test
    void definirBudget_devrait_rejeter_limite_nulle() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget("Alimentation", 1, 2024, null)
        );
        assertEquals("La limite doit être positive", exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "-500", "-1000.50"})
    void definirBudget_devrait_rejeter_limite_zero_ou_negative(String limiteInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget("Alimentation", 1, 2024, new BigDecimal(limiteInvalide))
        );
        assertEquals("La limite doit être positive", exception.getMessage());
    }

    @Test
    void definirBudget_devrait_creer_nouveau_budget() {
        Budget resultat = budgetService.definirBudget("Alimentation", 1, 2024, new BigDecimal("500.00"));

        assertNotNull(resultat);
        assertNotNull(resultat.getId());
        assertEquals("Alimentation", resultat.getCategorie());
        assertEquals(1, resultat.getMois());
        assertEquals(2024, resultat.getAnnee());
        assertEquals(new BigDecimal("500.00"), resultat.getLimite());
    }

    @Test
    void definirBudget_devrait_mettre_a_jour_budget_existant() {
        budgetService.definirBudget("Alimentation", 1, 2024, new BigDecimal("500.00"));
        Budget resultat = budgetService.definirBudget("Alimentation", 1, 2024, new BigDecimal("750.00"));

        assertEquals(new BigDecimal("750.00"), resultat.getLimite());
        List<Budget> budgets = budgetService.listerBudgets();
        assertEquals(1, budgets.size());
    }

    @Test
    void definirBudget_devrait_normaliser_categorie() {
        Budget resultat = budgetService.definirBudget("  Transport  ", 1, 2024, new BigDecimal("300.00"));
        assertEquals("Transport", resultat.getCategorie());
    }

    @Test
    void listerBudgets_devrait_retourner_tous_les_budgets() {
        budgetService.definirBudget("Alimentation", 1, 2024, new BigDecimal("500"));
        budgetService.definirBudget("Transport", 1, 2024, new BigDecimal("300"));

        List<Budget> resultat = budgetService.listerBudgets();
        assertEquals(2, resultat.size());
    }

    @Test
    void listerBudgets_devrait_retourner_liste_vide_si_aucun_budget() {
        List<Budget> resultat = budgetService.listerBudgets();
        assertTrue(resultat.isEmpty());
    }

    @Test
    void obtenirBudget_devrait_retourner_budget_existant() {
        budgetService.definirBudget("Alimentation", 1, 2024, new BigDecimal("500"));
        
        Optional<Budget> resultat = budgetService.obtenirBudget("Alimentation", 1, 2024);
        
        assertTrue(resultat.isPresent());
        assertEquals("Alimentation", resultat.get().getCategorie());
    }

    @Test
    void obtenirBudget_devrait_retourner_vide_si_inexistant() {
        Optional<Budget> resultat = budgetService.obtenirBudget("Loisirs", 1, 2024);
        assertFalse(resultat.isPresent());
    }

    @Test
    void calculerMontantRestant_devrait_calculer_correctement() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();
        budgetService.definirBudget("Alimentation", mois, annee, new BigDecimal("500.00"));
        transactionService.ajouterTransaction("Alimentation", new BigDecimal("250.00"), "Test", LocalDate.now());

        BigDecimal restant = budgetService.calculerMontantRestant("Alimentation", mois, annee);
        assertEquals(0, new BigDecimal("250.00").compareTo(restant));
    }

    @Test
    void calculerMontantRestant_devrait_retourner_zero_si_pas_de_budget() {
        BigDecimal restant = budgetService.calculerMontantRestant("Loisirs", 1, 2024);
        assertEquals(BigDecimal.ZERO, restant);
    }

    @Test
    void calculerMontantRestant_peut_etre_negatif_si_depassement() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();
        budgetService.definirBudget("Transport", mois, annee, new BigDecimal("300.00"));
        transactionService.ajouterTransaction("Transport", new BigDecimal("450.00"), "Dépassement", LocalDate.now());

        BigDecimal restant = budgetService.calculerMontantRestant("Transport", mois, annee);
        assertEquals(0, new BigDecimal("-150.00").compareTo(restant));
    }

    @Test
    void calculerPourcentageUtilisation_devrait_calculer_correctement() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();
        budgetService.definirBudget("Alimentation", mois, annee, new BigDecimal("500.00"));
        transactionService.ajouterTransaction("Alimentation", new BigDecimal("250.00"), "Test", LocalDate.now());

        BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation("Alimentation", mois, annee);
        assertEquals(new BigDecimal("50.00"), pourcentage);
    }

    @Test
    void calculerPourcentageUtilisation_devrait_retourner_zero_si_pas_de_budget() {
        BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation("Loisirs", 1, 2024);
        assertEquals(BigDecimal.ZERO, pourcentage);
    }

    @Test
    void calculerPourcentageUtilisation_peut_depasser_100() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();
        budgetService.definirBudget("Transport", mois, annee, new BigDecimal("300.00"));
        transactionService.ajouterTransaction("Transport", new BigDecimal("450.00"), "Dépassement", LocalDate.now());

        BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation("Transport", mois, annee);
        assertEquals(new BigDecimal("150.00"), pourcentage);
    }

    @Test
    void calculerPourcentageUtilisation_devrait_retourner_zero_si_aucune_depense() {
        budgetService.definirBudget("Loisirs", 1, 2024, new BigDecimal("200.00"));

        BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation("Loisirs", 1, 2024);
        assertEquals(BigDecimal.ZERO, pourcentage);
    }

    @Test
    void verifierDepassement_devrait_retourner_true_si_budget_depasse() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();
        budgetService.definirBudget("Transport", mois, annee, new BigDecimal("300.00"));
        transactionService.ajouterTransaction("Transport", new BigDecimal("350.00"), "Dépassement", LocalDate.now());

        boolean depasse = budgetService.verifierDepassement("Transport", mois, annee);
        assertTrue(depasse);
    }

    @Test
    void verifierDepassement_devrait_retourner_false_si_budget_respecte() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();
        budgetService.definirBudget("Alimentation", mois, annee, new BigDecimal("500.00"));
        transactionService.ajouterTransaction("Alimentation", new BigDecimal("250.00"), "OK", LocalDate.now());

        boolean depasse = budgetService.verifierDepassement("Alimentation", mois, annee);
        assertFalse(depasse);
    }

    @Test
    void verifierDepassement_devrait_retourner_false_si_pas_de_budget() {
        boolean depasse = budgetService.verifierDepassement("Loisirs", 1, 2024);
        assertFalse(depasse);
    }

    @Test
    void verifierDepassement_devrait_retourner_true_si_depasse_exactement() {
        int mois = LocalDate.now().getMonthValue();
        int annee = LocalDate.now().getYear();
        budgetService.definirBudget("Transport", mois, annee, new BigDecimal("300.00"));
        transactionService.ajouterTransaction("Transport", new BigDecimal("300.01"), "Dépassement exactement", LocalDate.now());

        boolean depasse = budgetService.verifierDepassement("Transport", mois, annee);
        assertTrue(depasse);
    }
}
