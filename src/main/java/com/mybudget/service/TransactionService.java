package com.mybudget.service;

import com.mybudget.model.Transaction;
import com.mybudget.model.ValidationException;
import com.mybudget.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TransactionService {
    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction ajouterTransaction(String categorie, BigDecimal montant, String description, LocalDate date) {
        validerCategorie(categorie);
        validerMontant(montant);
        validerDate(date);

        String categorieNormalisee = categorie.trim();
        Transaction transaction = new Transaction(null, categorieNormalisee, montant, description, date);
        return transactionRepository.enregistrer(transaction);
    }

    public List<Transaction> listerTransactions() {
        return transactionRepository.trouverTout();
    }

    public List<Transaction> listerTransactionsParCategorie(String categorie) {
        return transactionRepository.trouverParCategorie(categorie);
    }

    public BigDecimal calculerTotalParCategorie(String categorie, int mois, int annee) {
        List<Transaction> transactions = transactionRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
        return transactions.stream()
                .map(Transaction::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void supprimerTransaction(Long id) {
        if (id == null) {
            throw new ValidationException("L'identifiant ne peut pas être nul");
        }
        transactionRepository.supprimerParId(id);
    }

    public void modifierTransaction(Long id, String categorie, BigDecimal montant, String description, LocalDate date) {
        if (id == null) {
            throw new ValidationException("L'identifiant ne peut pas être nul");
        }
        validerCategorie(categorie);
        validerMontant(montant);
        validerDate(date);

        String categorieNormalisee = categorie.trim();
        Transaction transaction = new Transaction(id, categorieNormalisee, montant, description, date);
        transactionRepository.modifier(transaction);
    }

    private void validerCategorie(String categorie) {
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new ValidationException("La catégorie ne peut pas être vide");
        }
    }

    private void validerMontant(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Le montant doit être positif");
        }
    }

    private void validerDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("La date ne peut pas être nulle");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new ValidationException("La date ne peut pas être dans le futur");
        }
    }
}
