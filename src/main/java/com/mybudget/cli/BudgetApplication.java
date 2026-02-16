package com.mybudget.cli;

import com.mybudget.model.Budget;
import com.mybudget.model.Transaction;
import com.mybudget.model.ValidationException;
import com.mybudget.repository.BudgetRepository;
import com.mybudget.repository.DatabaseManager;
import com.mybudget.repository.TransactionRepository;
import com.mybudget.service.BudgetService;
import com.mybudget.service.ExportService;
import com.mybudget.service.TransactionService;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class BudgetApplication {
    private final Scanner scanner;
    private final TransactionService transactionService;
    private final BudgetService budgetService;
    private final ExportService exportService;
    private final DateTimeFormatter formateurDate = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public BudgetApplication(TransactionService transactionService, BudgetService budgetService, ExportService exportService) {
        this.scanner = new Scanner(System.in);
        this.transactionService = transactionService;
        this.budgetService = budgetService;
        this.exportService = exportService;
    }

    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager("jdbc:sqlite:budget.db");
        TransactionRepository transactionRepository = new TransactionRepository(databaseManager);
        BudgetRepository budgetRepository = new BudgetRepository(databaseManager);
        
        TransactionService transactionService = new TransactionService(transactionRepository);
        BudgetService budgetService = new BudgetService(budgetRepository, transactionService);
        ExportService exportService = new ExportService();

        BudgetApplication app = new BudgetApplication(transactionService, budgetService, exportService);
        app.demarrer();
    }

    public void demarrer() {
        System.out.println("=== Gestion de Budget Personnel ===");
        boolean continuer = true;

        while (continuer) {
            afficherMenuPrincipal();
            String choix = scanner.nextLine().trim();

            try {
                switch (choix) {
                    case "1" -> ajouterTransaction();
                    case "2" -> listerTransactions();
                    case "3" -> modifierTransaction();
                    case "4" -> supprimerTransaction();
                    case "5" -> definirBudget();
                    case "6" -> consulterBudgets();
                    case "7" -> exporterTransactions();
                    case "0" -> {
                        System.out.println("Au revoir !");
                        continuer = false;
                    }
                    default -> System.out.println("❌ Choix invalide. Veuillez réessayer.");
                }
            } catch (ValidationException e) {
                System.out.println("❌ Erreur de validation : " + e.getMessage());
            } catch (Exception e) {
                System.out.println("❌ Erreur inattendue : " + e.getMessage());
            }

            if (continuer) {
                System.out.println("\nAppuyez sur Entrée pour continuer...");
                scanner.nextLine();
            }
        }

        scanner.close();
    }

    private void afficherMenuPrincipal() {
        System.out.println("\n==============================================");
        System.out.println("              MENU PRINCIPAL");
        System.out.println("==============================================");
        System.out.println("1. Ajouter une transaction");
        System.out.println("2. Lister les transactions");
        System.out.println("3. Modifier une transaction");
        System.out.println("4. Supprimer une transaction");
        System.out.println("5. Définir un budget");
        System.out.println("6. Consulter les budgets");
        System.out.println("7. Exporter les transactions (CSV)");
        System.out.println("0. Quitter");
        System.out.println("==============================================");
        System.out.print("Votre choix : ");
    }

    private void ajouterTransaction() {
        System.out.println("\n--- Ajouter une transaction ---");
        
        System.out.print("Catégorie : ");
        String categorie = scanner.nextLine().trim();
        
        System.out.print("Montant (€) : ");
        BigDecimal montant = lireMontant();
        
        System.out.print("Description (optionnelle) : ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            description = null;
        }
        
        System.out.print("Date (JJ/MM/AAAA, vide = aujourd'hui) : ");
        LocalDate date = lireDate();

        Transaction transaction = transactionService.ajouterTransaction(categorie, montant, description, date);
        System.out.println("✅ Transaction ajoutée avec succès (ID: " + transaction.getId() + ")");

        // Vérifier si le budget est dépassé
        verifierEtAfficherAlerteDepassement(categorie, date);
    }

    private void listerTransactions() {
        System.out.println("\n--- Liste des transactions ---");
        System.out.println("1. Toutes les transactions");
        System.out.println("2. Par catégorie");
        System.out.print("Votre choix : ");
        
        String choix = scanner.nextLine().trim();
        List<Transaction> transactions;

        if ("2".equals(choix)) {
            System.out.print("Catégorie : ");
            String categorie = scanner.nextLine().trim();
            transactions = transactionService.listerTransactionsParCategorie(categorie);
        } else {
            transactions = transactionService.listerTransactions();
        }

        if (transactions.isEmpty()) {
            System.out.println("❌ Aucune transaction trouvée.");
        } else {
            System.out.println("\n┌─────────┬─────────────────┬────────────┬───────────────────────────────┬────────────┐");
            System.out.println("│   ID    │   Catégorie     │  Montant   │         Description           │    Date    │");
            System.out.println("├─────────┼─────────────────┼────────────┼───────────────────────────────┼────────────┤");
            
            for (Transaction t : transactions) {
                String description = t.getDescription() != null ? t.getDescription() : "-";
                if (description.length() > 29) {
                    description = description.substring(0, 26) + "...";
                }
                System.out.printf("│ %-7d │ %-15s │ %8.2f € │ %-29s │ %10s │%n",
                    t.getId(),
                    tronquer(t.getCategorie(), 15),
                    t.getMontant(),
                    description,
                    t.getDate().format(formateurDate));
            }
            
            System.out.println("└─────────┴─────────────────┴────────────┴───────────────────────────────┴────────────┘");
            System.out.println("Total : " + transactions.size() + " transaction(s)");
        }
    }

    private void modifierTransaction() {
        System.out.println("\n--- Modifier une transaction ---");
        
        System.out.print("ID de la transaction à modifier : ");
        Long id = lireId();
        
        System.out.print("Nouvelle catégorie : ");
        String categorie = scanner.nextLine().trim();
        
        System.out.print("Nouveau montant (€) : ");
        BigDecimal montant = lireMontant();
        
        System.out.print("Nouvelle description (optionnelle) : ");
        String description = scanner.nextLine().trim();
        if (description.isEmpty()) {
            description = null;
        }
        
        System.out.print("Nouvelle date (JJ/MM/AAAA, vide = aujourd'hui) : ");
        LocalDate date = lireDate();

        transactionService.modifierTransaction(id, categorie, montant, description, date);
        System.out.println("✅ Transaction modifiée avec succès");
    }

    private void supprimerTransaction() {
        System.out.println("\n--- Supprimer une transaction ---");
        
        System.out.print("ID de la transaction à supprimer : ");
        Long id = lireId();
        
        System.out.print("Confirmer la suppression ? (O/N) : ");
        String confirmation = scanner.nextLine().trim().toUpperCase();
        
        if ("O".equals(confirmation)) {
            transactionService.supprimerTransaction(id);
            System.out.println("✅ Transaction supprimée avec succès");
        } else {
            System.out.println("❌ Suppression annulée");
        }
    }

    private void definirBudget() {
        System.out.println("\n--- Définir un budget ---");
        
        System.out.print("Catégorie : ");
        String categorie = scanner.nextLine().trim();
        
        System.out.print("Mois (1-12) : ");
        int mois = lireEntier();
        
        System.out.print("Année : ");
        int annee = lireEntier();
        
        System.out.print("Limite (€) : ");
        BigDecimal limite = lireMontant();

        Budget budget = budgetService.definirBudget(categorie, mois, annee, limite);
        System.out.println("✅ Budget défini avec succès (ID: " + budget.getId() + ")");
    }

    private void consulterBudgets() {
        System.out.println("\n--- Consulter les budgets ---");
        
        List<Budget> budgets = budgetService.listerBudgets();
        
        if (budgets.isEmpty()) {
            System.out.println("❌ Aucun budget défini.");
        } else {
            System.out.println("\n┌────────────────┬──────────────┬─────────────┬──────────────┬──────────────┬──────────────┐");
            System.out.println("│   Catégorie    │     Mois     │    Limite   │    Dépensé   │    Restant   │      %       │");
            System.out.println("├────────────────┼──────────────┼─────────────┼──────────────┼──────────────┼──────────────┤");
            
            for (Budget budget : budgets) {
                String categorie = budget.getCategorie();
                int mois = budget.getMois();
                int annee = budget.getAnnee();
                
                BigDecimal depense = transactionService.calculerTotalParCategorie(categorie, mois, annee);
                BigDecimal restant = budgetService.calculerMontantRestant(categorie, mois, annee);
                BigDecimal pourcentage = budgetService.calculerPourcentageUtilisation(categorie, mois, annee);
                boolean depasse = budgetService.verifierDepassement(categorie, mois, annee);
                
                String indicateur = depasse ? "⚠️" : "✅";
                System.out.printf("│ %-14s │ %02d/%4d %s  │ %9.2f € │ %10.2f € │ %10.2f € │ %10.2f %% │%n",
                    tronquer(categorie, 14),
                    mois, annee, indicateur,
                    budget.getLimite(),
                    depense,
                    restant,
                    pourcentage);
            }
            
            System.out.println("└────────────────┴──────────────┴─────────────┴──────────────┴──────────────┴──────────────┘");
            System.out.println("Total : " + budgets.size() + " budget(s)");
        }
    }

    private void exporterTransactions() {
        System.out.println("\n--- Exporter les transactions ---");
        
        System.out.print("Nom du fichier (sans extension) : ");
        String nomFichier = scanner.nextLine().trim();
        String cheminFichier = nomFichier + ".csv";
        
        try {
            List<Transaction> transactions = transactionService.listerTransactions();
            exportService.exporterVersCSV(transactions, cheminFichier);
            System.out.println("✅ Transactions exportées avec succès : " + cheminFichier);
            System.out.println("   (" + transactions.size() + " transaction(s) exportée(s))");
        } catch (IOException e) {
            System.out.println("❌ Erreur lors de l'export : " + e.getMessage());
        }
    }

    private void verifierEtAfficherAlerteDepassement(String categorie, LocalDate date) {
        int mois = date.getMonthValue();
        int annee = date.getYear();
        
        Optional<Budget> budget = budgetService.obtenirBudget(categorie, mois, annee);
        if (budget.isPresent() && budgetService.verifierDepassement(categorie, mois, annee)) {
            BigDecimal depense = transactionService.calculerTotalParCategorie(categorie, mois, annee);
            BigDecimal limite = budget.get().getLimite();
            BigDecimal depassement = depense.subtract(limite);
            
            System.out.println("\n⚠️  ALERTE : Budget dépassé !");
            System.out.println("   Catégorie : " + categorie);
            System.out.println("   Limite    : " + limite + " €");
            System.out.println("   Dépensé   : " + depense + " €");
            System.out.println("   Dépassement : +" + depassement + " €");
        }
    }

    private BigDecimal lireMontant() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return new BigDecimal(input);
            } catch (NumberFormatException e) {
                System.out.print("❌ Montant invalide. Réessayez : ");
            }
        }
    }

    private LocalDate lireDate() {
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return LocalDate.now();
        }
        
        while (true) {
            try {
                return LocalDate.parse(input, formateurDate);
            } catch (DateTimeParseException e) {
                System.out.print("❌ Date invalide (format JJ/MM/AAAA). Réessayez : ");
                input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    return LocalDate.now();
                }
            }
        }
    }

    private int lireEntier() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("❌ Nombre invalide. Réessayez : ");
            }
        }
    }

    private Long lireId() {
        while (true) {
            try {
                String input = scanner.nextLine().trim();
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.print("❌ ID invalide. Réessayez : ");
            }
        }
    }

    private String tronquer(String texte, int longueurMax) {
        if (texte.length() <= longueurMax) {
            return texte;
        }
        return texte.substring(0, longueurMax - 3) + "...";
    }
}
