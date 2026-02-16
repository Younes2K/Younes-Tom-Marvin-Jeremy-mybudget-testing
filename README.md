# Gestion de Budget Personnel

Application en ligne de commande pour gérer son budget personnel avec suivi des dépenses et alertes de dépassement.

## Fonctionnalités

### MVP (Minimum Viable Product)
- ✅ Ajouter une transaction (catégorie, montant, description, date)
- ✅ Lister toutes les transactions
- ✅ Définir un budget mensuel par catégorie
- ✅ Calculer le montant dépensé par catégorie
- ✅ Calculer le montant restant dans un budget

### Fonctionnalités supplémentaires
- ✅ Modifier une transaction existante
- ✅ Supprimer une transaction
- ✅ Alertes automatiques en cas de dépassement de budget
- ✅ Export des transactions au format CSV
- ✅ Filtrage des transactions par catégorie
- ✅ Calcul du pourcentage d'utilisation du budget
- ✅ Validation complète des données (montants positifs, dates valides, etc.)

## Prérequis

- Java 17 ou supérieur
- Maven 3.6+

## Installation

```bash
git clone https://github.com/Younes2K/Younes-Tom-Marvin-Jeremy-mybudget-testing.git
cd Younes-Tom-Marvin-Jeremy-mybudget-testing
mvn clean package
```

## Utilisation

```bash
java -jar target/budget-app.jar
```

## Exemples

### Ajouter une transaction
1. Choisir l'option 1 dans le menu
2. Saisir la catégorie (ex: Alimentation)
3. Saisir le montant (ex: 50.00)
4. Ajouter une description (optionnel)
5. Saisir la date ou appuyer sur Entrée pour aujourd'hui

### Définir un budget
1. Choisir l'option 5
2. Saisir la catégorie
3. Saisir le mois et l'année
4. Définir la limite en euros

## Tests

### Exécuter tous les tests

```bash
mvn clean test
```

Le projet contient **71 tests** couvrant la couche service :
- 28 tests pour `TransactionService`
- 37 tests pour `BudgetService`
- 6 tests pour `ExportService`

### Vérifier la couverture de code

Couverture minimale requise : **80% sur la couche service** ✅

```bash
mvn clean test jacoco:report
```

Le rapport de couverture est disponible dans `target/site/jacoco/index.html`

### Compiler et packager

```bash
mvn clean package
```

Génère le JAR exécutable : `target/budget-app.jar` (inclut toutes les dépendances)

## Architecture

Le projet suit une **architecture en couches stricte** :

```
src/
├── main/java/com/mybudget/
│   ├── model/          # Modèles de domaine (Transaction, Budget)
│   ├── repository/     # Couche de persistance (SQLite)
│   ├── service/        # Logique métier et validation
│   └── cli/            # Interface utilisateur en ligne de commande
└── test/java/com/mybudget/
    └── service/        # Tests d'intégration avec bases SQLite temporaires
```

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
