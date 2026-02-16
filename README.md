# Budget Personnel

Application en ligne de commande pour gérer son budget personnel avec suivi des dépenses, alertes de dépassement et exports CSV.

## Ce qu'on peut faire

- **Transactions**
  - Ajouter une transaction (revenu ou dépense)
  - Modifier / supprimer une transaction
  - Lister l'historique des transactions
  - Filtrer par catégorie, dates ou type

- **Budgets**
  - Créer un budget (catégorie + période mensuelle)
  - Voir un résumé du budget (dépensé, restant, pourcentage)
  - Afficher une alerte si le budget est proche ou dépassé

- **Export**
  - Exporter les transactions en CSV

## Stack

- **Langage** : Java 17
- **Build** : Maven 3.6+
- **Base de données** : SQLite
- **Tests** : JUnit 5 + JaCoCo (couverture)

## Lancement

### Installation

```bash
git clone https://github.com/Younes2K/Younes-Tom-Marvin-Jeremy-mybudget-testing.git
cd Younes-Tom-Marvin-Jeremy-mybudget-testing
mvn clean package
```

### Exécution

```bash
java -jar target/budget-app.jar
```

L'application démarre en mode interactif avec un menu CLI.

## Tests

### Exécuter tous les tests

```bash
mvn clean test
```

**71 tests** au total :
- 28 tests pour `TransactionService`
- 37 tests pour `BudgetService`
- 6 tests pour `ExportService`

### Vérifier la couverture

```bash
mvn clean test jacoco:report
```

Le rapport JaCoCo est généré dans `target/site/jacoco/index.html`  
**Couverture requise** : 80% minimum sur la couche service ✅

### Compiler et packager

```bash
mvn clean package
```

Génère le JAR exécutable avec dépendances incluses : `target/budget-app.jar`

## Architecture

Le projet suit une **architecture en couches stricte** :

```
src/
├── main/java/com/mybudget/
│   ├── cli/            # Interface utilisateur (menu interactif)
│   ├── model/          # Modèles de domaine (Transaction, Budget)
│   ├── repository/     # Couche d'accès aux données (SQLite)
│   └── service/        # Logique métier et validation
└── test/java/com/mybudget/
    └── service/        # Tests d'intégration avec bases temporaires
```

## Notes utiles

- Les transactions sont persistées dans `budget.db` (SQLite)
- Le solde affiché = revenus - dépenses (basé sur les transactions)
- Les alertes de budget s'affichent automatiquement lors du calcul
- Les fichiers CSV exportés incluent : catégorie, montant, description, date

### Approche TDD
Le projet a été développé en suivant la méthodologie **Test-Driven Development** :
1. Écriture des tests en premier
2. Implémentation du code pour faire passer les tests
3. Refactorisation si nécessaire

Les tests utilisent des bases de données SQLite temporaires réelles pour valider l'intégration complète des couches repository et service.

## Technologies

- **Java 17** - Langage de programmation
- **Maven** - Gestion de projet et dépendances
- **SQLite 3.45.0.0** - Base de données embarquée
- **JUnit 5.10.1** - Framework de tests
- **JaCoCo 0.8.12** - Analyse de couverture de code (objectif : 80% sur la couche service)
- **Maven Shade Plugin 3.5.1** - Packaging JAR exécutable avec dépendances

## Base de données

L'application utilise SQLite. La base de données `budget.db` est créée automatiquement au premier lancement.

## Auteurs

Younes, Tom, Marvin, Jeremy

## Licence

Projet académique
