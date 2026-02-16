# Gestion de Budget Personnel

Application en ligne de commande pour gérer son budget personnel avec suivi des dépenses et alertes de dépassement.

## Fonctionnalités

- 💰 Ajouter, modifier et supprimer des transactions
- 📊 Définir des budgets mensuels par catégorie
- ⚠️ Alertes automatiques en cas de dépassement de budget
- 📈 Visualisation des dépenses et statistiques
- 📁 Export des transactions en CSV

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

```bash
mvn test
```

Couverture minimale requise : 80% sur la couche service

```bash
mvn test jacoco:report
```

Le rapport de couverture est disponible dans `target/site/jacoco/index.html`

## Architecture

```
src/
├── main/java/com/mybudget/
│   ├── model/          # Modèles de domaine
│   ├── repository/     # Couche de persistance (SQLite)
│   ├── service/        # Logique métier
│   └── cli/            # Interface utilisateur
└── test/java/com/mybudget/
    └── service/        # Tests unitaires
```

## Technologies

- Java 17
- Maven
- SQLite 3.45.0.0
- JUnit 5.10.1
- Mockito 5.8.0
- JaCoCo 0.8.12

## Base de données

L'application utilise SQLite. La base de données `budget.db` est créée automatiquement au premier lancement.

## Auteurs

Younes, Tom, Marvin, Jeremy

## Licence

Projet académique
