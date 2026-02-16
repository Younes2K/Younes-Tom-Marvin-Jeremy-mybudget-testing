package com.mybudget.service;

import com.mybudget.model.Budget;
import com.mybudget.model.ValidationException;
import com.mybudget.repository.BudgetRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

public class BudgetService {
    private final BudgetRepository budgetRepository;
    private final TransactionService transactionService;

    public BudgetService(BudgetRepository budgetRepository, TransactionService transactionService) {
        this.budgetRepository = budgetRepository;
        this.transactionService = transactionService;
    }

    public Budget definirBudget(String categorie, int mois, int annee, BigDecimal limite) {
        validerCategorie(categorie);
        validerMois(mois);
        validerAnnee(annee);
        validerLimite(limite);

        String categorieNormalisee = categorie.trim();
        Optional<Budget> budgetExistant = budgetRepository.trouverParCategorieEtMoisEtAnnee(categorieNormalisee, mois, annee);

        if (budgetExistant.isPresent()) {
            Budget budget = budgetExistant.get();
            budget.setLimite(limite);
            budgetRepository.modifier(budget);
            return budget;
        } else {
            Budget nouveauBudget = new Budget(null, categorieNormalisee, mois, annee, limite);
            return budgetRepository.enregistrer(nouveauBudget);
        }
    }

    public List<Budget> listerBudgets() {
        return budgetRepository.trouverTout();
    }

    public Optional<Budget> obtenirBudget(String categorie, int mois, int annee) {
        return budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
    }

    public BigDecimal calculerMontantRestant(String categorie, int mois, int annee) {
        Optional<Budget> budget = budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
        if (budget.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal depenses = transactionService.calculerTotalParCategorie(categorie, mois, annee);
        return budget.get().getLimite().subtract(depenses);
    }

    public BigDecimal calculerPourcentageUtilisation(String categorie, int mois, int annee) {
        Optional<Budget> budget = budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
        if (budget.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal depenses = transactionService.calculerTotalParCategorie(categorie, mois, annee);
        if (depenses.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal limite = budget.get().getLimite();
        return depenses.multiply(new BigDecimal("100"))
                .divide(limite, 2, RoundingMode.HALF_UP);
    }

    public boolean verifierDepassement(String categorie, int mois, int annee) {
        Optional<Budget> budget = budgetRepository.trouverParCategorieEtMoisEtAnnee(categorie, mois, annee);
        if (budget.isEmpty()) {
            return false;
        }

        BigDecimal depenses = transactionService.calculerTotalParCategorie(categorie, mois, annee);
        return depenses.compareTo(budget.get().getLimite()) > 0;
    }

    private void validerCategorie(String categorie) {
        if (categorie == null || categorie.trim().isEmpty()) {
            throw new ValidationException("La catégorie ne peut pas être vide");
        }
    }

    private void validerMois(int mois) {
        if (mois < 1 || mois > 12) {
            throw new ValidationException("Le mois doit être entre 1 et 12");
        }
    }

    private void validerAnnee(int annee) {
        if (annee < 2000) {
            throw new ValidationException("L'année doit être >= 2000");
        }
    }

    private void validerLimite(BigDecimal limite) {
        if (limite == null || limite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("La limite doit être positive");
        }
    }
}
