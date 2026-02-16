package com.mybudget.service;

import com.mybudget.model.Budget;
import com.mybudget.model.ValidationException;
import com.mybudget.repository.BudgetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BudgetServiceTest {
    private BudgetRepository budgetRepository;
    private TransactionService transactionService;
    private BudgetService budgetService;

    @BeforeEach
    void setUp() {
        budgetRepository = mock(BudgetRepository.class);
        transactionService = mock(TransactionService.class);
        budgetService = new BudgetService(budgetRepository, transactionService);
    }

    // Tests de validation - categorie
    @Test
    void definirBudget_devrait_rejeter_categorie_nulle() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget(null, 1, 2024, new BigDecimal("500"))
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
        verify(budgetRepository, never()).enregistrer(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void definirBudget_devrait_rejeter_categorie_vide_ou_blanche(String categorieInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget(categorieInvalide, 1, 2024, new BigDecimal("500"))
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
        verify(budgetRepository, never()).enregistrer(any());
    }

    // Tests de validation - mois
    @ParameterizedTest
    @ValueSource(ints = {0, -1, 13, 100})
    void definirBudget_devrait_rejeter_mois_invalide(int moisInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget("Alimentation", moisInvalide, 2024, new BigDecimal("500"))
        );
        assertEquals("Le mois doit être entre 1 et 12", exception.getMessage());
        verify(budgetRepository, never()).enregistrer(any());
    }

    // Tests de validation - annee
    @ParameterizedTest
    @ValueSource(ints = {1999, 1900, 0, -2024})
    void definirBudget_devrait_rejeter_annee_invalide(int anneeInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget("Alimentation", 1, anneeInvalide, new BigDecimal("500"))
        );
        assertEquals("L'année doit être >= 2000", exception.getMessage());
        verify(budgetRepository, never()).enregistrer(any());
    }

    // Tests de validation - limite
    @Test
    void definirBudget_devrait_rejeter_limite_nulle() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget("Alimentation", 1, 2024, null)
        );
        assertEquals("La limite doit être positive", exception.getMessage());
        verify(budgetRepository, never()).enregistrer(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "-500", "-1000.50"})
    void definirBudget_devrait_rejeter_limite_zero_ou_negative(String limiteInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            budgetService.definirBudget("Alimentation", 1, 2024, new BigDecimal(limiteInvalide))
        );
        assertEquals("La limite doit être positive", exception.getMessage());
        verify(budgetRepository, never()).enregistrer(any());
    }

    // Tests de succès - creation
    @Test
    void definirBudget_devrait_creer_nouveau_budget() {
        String categorie = "Alimentation";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("500.00");

        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.empty());
        when(budgetRepository.enregistrer(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Budget resultat = budgetService.definirBudget(categorie, mois, annee, limite);

        assertNotNull(resultat);
        assertEquals(categorie, resultat.getCategorie());
        assertEquals(mois, resultat.getMois());
        assertEquals(annee, resultat.getAnnee());
        assertEquals(limite, resultat.getLimite());
        verify(budgetRepository, times(1)).trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
        verify(budgetRepository, times(1)).enregistrer(any(Budget.class));
        verify(budgetRepository, never()).modifier(any());
    }

    // Tests de succès - mise à jour
    @Test
    void definirBudget_devrait_mettre_a_jour_budget_existant() {
        String categorie = "Alimentation";
        int mois = 1;
        int annee = 2024;
        BigDecimal ancienneLimite = new BigDecimal("500.00");
        BigDecimal nouvelleLimite = new BigDecimal("750.00");

        Budget budgetExistant = new Budget(1L, categorie, mois, annee, ancienneLimite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budgetExistant));

        Budget resultat = budgetService.definirBudget(categorie, mois, annee, nouvelleLimite);

        assertNotNull(resultat);
        assertEquals(nouvelleLimite, resultat.getLimite());
        verify(budgetRepository, times(1)).trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
        verify(budgetRepository, times(1)).modifier(budgetExistant);
        verify(budgetRepository, never()).enregistrer(any());
    }

    @Test
    void definirBudget_devrait_normaliser_categorie() {
        String categorieAvecEspaces = "  Transport  ";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("300.00");

        when(budgetRepository.trouverParCategorieEtMoisEtAnnee("Transport", mois, annee)).thenReturn(Optional.empty());
        when(budgetRepository.enregistrer(any(Budget.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Budget resultat = budgetService.definirBudget(categorieAvecEspaces, mois, annee, limite);

        assertEquals("Transport", resultat.getCategorie());
        verify(budgetRepository, times(1)).enregistrer(any(Budget.class));
    }

    // Tests de listerBudgets
    @Test
    void listerBudgets_devrait_retourner_tous_les_budgets() {
        List<Budget> budgetsAttendus = Arrays.asList(
            new Budget(1L, "Alimentation", 1, 2024, new BigDecimal("500")),
            new Budget(2L, "Transport", 1, 2024, new BigDecimal("300"))
        );
        when(budgetRepository.trouverTout()).thenReturn(budgetsAttendus);

        List<Budget> resultat = budgetService.listerBudgets();

        assertEquals(2, resultat.size());
        verify(budgetRepository, times(1)).trouverTout();
    }

    @Test
    void listerBudgets_devrait_retourner_liste_vide_si_aucun_budget() {
        when(budgetRepository.trouverTout()).thenReturn(List.of());

        List<Budget> resultat = budgetService.listerBudgets();

        assertTrue(resultat.isEmpty());
        verify(budgetRepository, times(1)).trouverTout();
    }

    // Tests de obtenirBudget
    @Test
    void obtenirBudget_devrait_retourner_budget_existant() {
        String categorie = "Alimentation";
        int mois = 1;
        int annee = 2024;
        Budget budgetAttendu = new Budget(1L, categorie, mois, annee, new BigDecimal("500"));
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budgetAttendu));

        Optional<Budget> resultat = budgetService.obtenirBudget(categorie, mois, annee);

        assertTrue(resultat.isPresent());
        assertEquals(budgetAttendu, resultat.get());
        verify(budgetRepository, times(1)).trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
    }

    @Test
    void obtenirBudget_devrait_retourner_vide_si_inexistant() {
        String categorie = "Loisirs";
        int mois = 1;
        int annee = 2024;
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.empty());

        Optional<Budget> resultat = budgetService.obtenirBudget(categorie, mois, annee);

        assertFalse(resultat.isPresent());
        verify(budgetRepository, times(1)).trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
    }

    // Tests de calculerMontantRestant
    @Test
    void calculerMontantRestant_devrait_calculer_correctement() {
        String categorie = "Alimentation";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("500.00");
        BigDecimal depense = new BigDecimal("250.00");

        Budget budget = new Budget(1L, categorie, mois, annee, limite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budget));
        when(transactionService.calculerTotalParCategorie(categorie, mois, annee)).thenReturn(depense);

        BigDecimal restant = budgetService.calculerMontantRestant(categorie, mois, annee);

        assertEquals(new BigDecimal("250.00"), restant);
        verify(budgetRepository, times(1)).trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
        verify(transactionService, times(1)).calculerTotalParCategorie(categorie, mois, annee);
    }

    @Test
    void calculerMontantRestant_devrait_retourner_zero_si_pas_de_budget() {
        String categorie = "Loisirs";
        int mois = 1;
        int annee = 2024;
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.empty());

        BigDecimal restant = budgetService.calculerMontantRestant(categorie, mois, annee);

        assertEquals(BigDecimal.ZERO, restant);
        verify(budgetRepository, times(1)).trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
        verify(transactionService, never()).calculerTotalParCategorie(anyString(), anyInt(), anyInt());
    }

    @Test
    void calculerMontantRestant_peut_etre_negatif_si_depassement() {
        String categorie = "Transport";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("300.00");
        BigDecimal depense = new BigDecimal("450.00");

        Budget budget = new Budget(1L, categorie, mois, annee, limite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budget));
        when(transactionService.calculerTotalParCategorie(categorie, mois, annee)).thenReturn(depense);

        BigDecimal restant = budgetService.calculerMontantRestant(categorie, mois, annee);

        assertEquals(new BigDecimal("-150.00"), restant);
    }

    // Tests de calculerPourcentageUtilisation
    @Test
    void calculerPourcentageUtilisation_devrait_calculer_correctement() {
        String categorie = "Alimentation";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("500.00");
        BigDecimal depense = new BigDecimal("250.00");

        Budget budget = new Budget(1L, categorie, mois, annee, limite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budget));
        when(transactionService.calculerTotalParCategorie(categorie, mois, annee)).thenReturn(depense);

        BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation(categorie, mois, annee);

        assertEquals(new BigDecimal("50.00"), pourcentage);
    }

    @Test
    void calculerPourcentageUtilisation_devrait_retourner_zero_si_pas_de_budget() {
        String categorie = "Loisirs";
        int mois = 1;
        int annee = 2024;
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.empty());

        BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation(categorie, mois, annee);

        assertEquals(BigDecimal.ZERO, pourcentage);
    }

    @Test
    void calculerPourcentageUtilisation_peut_depasser_100() {
        String categorie = "Transport";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("300.00");
        BigDecimal depense = new BigDecimal("450.00");

        Budget budget = new Budget(1L, categorie, mois, annee, limite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budget));
        when(transactionService.calculerTotalParCategorie(categorie, mois, annee)).thenReturn(depense);

        BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation(categorie, mois, annee);

        assertEquals(new BigDecimal("150.00"), pourcentage);
    }

    @Test
    void calculerPourcentageUtilisation_devrait_retourner_zero_si_aucune_depense() {
        String categorie = "Loisirs";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("200.00");
        BigDecimal depense = BigDecimal.ZERO;

        Budget budget = new Budget(1L, categorie, mois, annee, limite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budget));
        when(transactionService.calculerTotalParCategorie(categorie, mois, annee)).thenReturn(depense);

        BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation(categorie, mois, annee);

        assertEquals(BigDecimal.ZERO, pourcentage);
    }

    // Tests de verifierDepassement
    @Test
    void verifierDepassement_devrait_retourner_true_si_budget_depasse() {
        String categorie = "Transport";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("300.00");
        BigDecimal depense = new BigDecimal("350.00");

        Budget budget = new Budget(1L, categorie, mois, annee, limite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budget));
        when(transactionService.calculerTotalParCategorie(categorie, mois, annee)).thenReturn(depense);

        boolean depasse = budgetService.verifierDepassement(categorie, mois, annee);

        assertTrue(depasse);
    }

    @Test
    void verifierDepassement_devrait_retourner_false_si_budget_respecte() {
        String categorie = "Alimentation";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("500.00");
        BigDecimal depense = new BigDecimal("250.00");

        Budget budget = new Budget(1L, categorie, mois, annee, limite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budget));
        when(transactionService.calculerTotalParCategorie(categorie, mois, annee)).thenReturn(depense);

        boolean depasse = budgetService.verifierDepassement(categorie, mois, annee);

        assertFalse(depasse);
    }

    @Test
    void verifierDepassement_devrait_retourner_false_si_pas_de_budget() {
        String categorie = "Loisirs";
        int mois = 1;
        int annee = 2024;
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.empty());

        boolean depasse = budgetService.verifierDepassement(categorie, mois, annee);

        assertFalse(depasse);
        verify(transactionService, never()).calculerTotalParCategorie(anyString(), anyInt(), anyInt());
    }

    @Test
    void verifierDepassement_devrait_retourner_true_si_depasse_exactement() {
        String categorie = "Transport";
        int mois = 1;
        int annee = 2024;
        BigDecimal limite = new BigDecimal("300.00");
        BigDecimal depense = new BigDecimal("300.01");

        Budget budget = new Budget(1L, categorie, mois, annee, limite);
        when(budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(Optional.of(budget));
        when(transactionService.calculerTotalParCategorie(categorie, mois, annee)).thenReturn(depense);

        boolean depasse = budgetService.verifierDepassement(categorie, mois, annee);

        assertTrue(depasse);
    }
}
