package com.mybudget.service;

import com.mybudget.model.Transaction;
import com.mybudget.model.ValidationException;
import com.mybudget.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TransactionServiceTest {
    private TransactionRepository transactionRepository;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = mock(TransactionRepository.class);
        transactionService = new TransactionService(transactionRepository);
    }

    // Tests de validation - categorie
    @Test
    void ajouterTransaction_devrait_rejeter_categorie_nulle() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction(null, new BigDecimal("100"), "Description", LocalDate.now())
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
        verify(transactionRepository, never()).enregistrer(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void ajouterTransaction_devrait_rejeter_categorie_vide_ou_blanche(String categorieInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction(categorieInvalide, new BigDecimal("100"), "Description", LocalDate.now())
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
        verify(transactionRepository, never()).enregistrer(any());
    }

    // Tests de validation - montant
    @Test
    void ajouterTransaction_devrait_rejeter_montant_nul() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction("Alimentation", null, "Description", LocalDate.now())
        );
        assertEquals("Le montant doit être positif", exception.getMessage());
        verify(transactionRepository, never()).enregistrer(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-0.01", "-100", "-1000.50"})
    void ajouterTransaction_devrait_rejeter_montant_zero_ou_negatif(String montantInvalide) {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction("Alimentation", new BigDecimal(montantInvalide), "Description", LocalDate.now())
        );
        assertEquals("Le montant doit être positif", exception.getMessage());
        verify(transactionRepository, never()).enregistrer(any());
    }

    // Tests de validation - date
    @Test
    void ajouterTransaction_devrait_rejeter_date_nulle() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction("Alimentation", new BigDecimal("100"), "Description", null)
        );
        assertEquals("La date ne peut pas être nulle", exception.getMessage());
        verify(transactionRepository, never()).enregistrer(any());
    }

    @Test
    void ajouterTransaction_devrait_rejeter_date_future() {
        LocalDate dateFuture = LocalDate.now().plusDays(1);
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.ajouterTransaction("Alimentation", new BigDecimal("100"), "Description", dateFuture)
        );
        assertEquals("La date ne peut pas être dans le futur", exception.getMessage());
        verify(transactionRepository, never()).enregistrer(any());
    }

    // Tests de succès
    @Test
    void ajouterTransaction_devrait_enregistrer_transaction_valide() {
        String categorie = "Alimentation";
        BigDecimal montant = new BigDecimal("50.00");
        String description = "Courses du mois";
        LocalDate date = LocalDate.now();

        Transaction transactionAttendue = new Transaction(null, categorie, montant, description, date);
        when(transactionRepository.enregistrer(any(Transaction.class))).thenReturn(transactionAttendue);

        Transaction resultat = transactionService.ajouterTransaction(categorie, montant, description, date);

        assertNotNull(resultat);
        assertEquals(categorie, resultat.getCategorie());
        assertEquals(montant, resultat.getMontant());
        assertEquals(description, resultat.getDescription());
        assertEquals(date, resultat.getDate());
        verify(transactionRepository, times(1)).enregistrer(any(Transaction.class));
    }

    @Test
    void ajouterTransaction_devrait_autoriser_description_nulle() {
        String categorie = "Transport";
        BigDecimal montant = new BigDecimal("25.00");
        LocalDate date = LocalDate.now();

        Transaction transactionAttendue = new Transaction(null, categorie, montant, null, date);
        when(transactionRepository.enregistrer(any(Transaction.class))).thenReturn(transactionAttendue);

        Transaction resultat = transactionService.ajouterTransaction(categorie, montant, null, date);

        assertNotNull(resultat);
        assertNull(resultat.getDescription());
        verify(transactionRepository, times(1)).enregistrer(any(Transaction.class));
    }

    @Test
    void ajouterTransaction_devrait_normaliser_categorie() {
        String categorieAvecEspaces = "  Alimentation  ";
        BigDecimal montant = new BigDecimal("50.00");
        LocalDate date = LocalDate.now();

        when(transactionRepository.enregistrer(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transaction resultat = transactionService.ajouterTransaction(categorieAvecEspaces, montant, "Test", date);

        assertEquals("Alimentation", resultat.getCategorie());
        verify(transactionRepository, times(1)).enregistrer(any(Transaction.class));
    }

    // Tests de listerTransactions
    @Test
    void listerTransactions_devrait_retourner_toutes_les_transactions() {
        List<Transaction> transactionsAttendues = Arrays.asList(
            new Transaction(1L, "Alimentation", new BigDecimal("50"), "Courses", LocalDate.now()),
            new Transaction(2L, "Transport", new BigDecimal("30"), "Essence", LocalDate.now())
        );
        when(transactionRepository.trouverTout()).thenReturn(transactionsAttendues);

        List<Transaction> resultat = transactionService.listerTransactions();

        assertEquals(2, resultat.size());
        verify(transactionRepository, times(1)).trouverTout();
    }

    @Test
    void listerTransactions_devrait_retourner_liste_vide_si_aucune_transaction() {
        when(transactionRepository.trouverTout()).thenReturn(List.of());

        List<Transaction> resultat = transactionService.listerTransactions();

        assertTrue(resultat.isEmpty());
        verify(transactionRepository, times(1)).trouverTout();
    }

    // Tests de listerTransactionsParCategorie
    @Test
    void listerTransactionsParCategorie_devrait_filtrer_par_categorie() {
        String categorie = "Alimentation";
        List<Transaction> transactionsAttendues = Arrays.asList(
            new Transaction(1L, categorie, new BigDecimal("50"), "Courses", LocalDate.now()),
            new Transaction(3L, categorie, new BigDecimal("25"), "Restaurant", LocalDate.now())
        );
        when(transactionRepository.trouverParCategorie(categorie)).thenReturn(transactionsAttendues);

        List<Transaction> resultat = transactionService.listerTransactionsParCategorie(categorie);

        assertEquals(2, resultat.size());
        assertTrue(resultat.stream().allMatch(t -> t.getCategorie().equals(categorie)));
        verify(transactionRepository, times(1)).trouverParCategorie(categorie);
    }

    // Tests de calculerTotalParCategorie
    @Test
    void calculerTotalParCategorie_devrait_sommer_montants() {
        String categorie = "Alimentation";
        int mois = 1;
        int annee = 2024;
        List<Transaction> transactions = Arrays.asList(
            new Transaction(1L, categorie, new BigDecimal("50.00"), "Courses", LocalDate.of(2024, 1, 15)),
            new Transaction(2L, categorie, new BigDecimal("25.50"), "Restaurant", LocalDate.of(2024, 1, 20))
        );
        when(transactionRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(transactions);

        BigDecimal total = transactionService.calculerTotalParCategorie(categorie, mois, annee);

        assertEquals(new BigDecimal("75.50"), total);
        verify(transactionRepository, times(1)).trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
    }

    @Test
    void calculerTotalParCategorie_devrait_retourner_zero_si_aucune_transaction() {
        String categorie = "Transport";
        int mois = 1;
        int annee = 2024;
        when(transactionRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee)).thenReturn(List.of());

        BigDecimal total = transactionService.calculerTotalParCategorie(categorie, mois, annee);

        assertEquals(BigDecimal.ZERO, total);
        verify(transactionRepository, times(1)).trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
    }

    // Tests de supprimerTransaction
    @Test
    void supprimerTransaction_devrait_supprimer_transaction_existante() {
        Long id = 1L;

        transactionService.supprimerTransaction(id);

        verify(transactionRepository, times(1)).supprimerParId(id);
    }

    @Test
    void supprimerTransaction_devrait_rejeter_id_nul() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.supprimerTransaction(null)
        );
        assertEquals("L'identifiant ne peut pas être nul", exception.getMessage());
        verify(transactionRepository, never()).supprimerParId(any());
    }

    // Tests de modifierTransaction
    @Test
    void modifierTransaction_devrait_modifier_transaction_valide() {
        Long id = 1L;
        String categorie = "Alimentation";
        BigDecimal montant = new BigDecimal("75.00");
        String description = "Courses modifiées";
        LocalDate date = LocalDate.now();

        transactionService.modifierTransaction(id, categorie, montant, description, date);

        verify(transactionRepository, times(1)).modifier(any(Transaction.class));
    }

    @Test
    void modifierTransaction_devrait_rejeter_id_nul() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.modifierTransaction(null, "Alimentation", new BigDecimal("100"), "Test", LocalDate.now())
        );
        assertEquals("L'identifiant ne peut pas être nul", exception.getMessage());
        verify(transactionRepository, never()).modifier(any());
    }

    @Test
    void modifierTransaction_devrait_valider_categorie() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.modifierTransaction(1L, "", new BigDecimal("100"), "Test", LocalDate.now())
        );
        assertEquals("La catégorie ne peut pas être vide", exception.getMessage());
        verify(transactionRepository, never()).modifier(any());
    }

    @Test
    void modifierTransaction_devrait_valider_montant() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.modifierTransaction(1L, "Alimentation", new BigDecimal("-50"), "Test", LocalDate.now())
        );
        assertEquals("Le montant doit être positif", exception.getMessage());
        verify(transactionRepository, never()).modifier(any());
    }

    @Test
    void modifierTransaction_devrait_valider_date() {
        ValidationException exception = assertThrows(ValidationException.class, () ->
            transactionService.modifierTransaction(1L, "Alimentation", new BigDecimal("100"), "Test", null)
        );
        assertEquals("La date ne peut pas être nulle", exception.getMessage());
        verify(transactionRepository, never()).modifier(any());
    }
}
