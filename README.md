# Budget Personnel

Application web pour gérer son budget personnel avec suivi des dépenses, alertes de dépassement et exports CSV.

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

- **Frontend** : React + Vite
- **Backend** : Node.js + Express
- **Base de données** : SQLite (better-sqlite3)
- **CLI (legacy)** : Java 17 + Maven

## Lancement

### Interface Web (recommandé)

**Backend**
```bash
cd backend
npm install
npm run dev
```
Le backend tourne sur http://localhost:3001

**Frontend**
```bash
cd frontend
npm install
npm run dev
```
Le frontend tourne sur http://localhost:5173

### CLI (mode texte)

```bash
mvn clean package
java -jar target/budget-app.jar
```

## Tests

### Tests CLI (Java)

**Exécuter tous les tests**
```bash
mvn clean test
```

**71 tests** au total :
- 28 tests pour `TransactionService`
- 37 tests pour `BudgetService`
- 6 tests pour `ExportService`

**Vérifier la couverture**
```bash
mvn clean test jacoco:report
```
Le rapport JaCoCo est généré dans `target/site/jacoco/index.html`  
**Couverture requise** : 80% minimum sur la couche service ✅

### Build du JAR

```bash
mvn clean package
```
Génère le JAR exécutable avec dépendances incluses : `target/budget-app.jar`

## Architecture

### Backend API (Node.js)
```
backend/
├── server.js           # Point d'entrée Express
├── db/
│   └── init.js        # Configuration SQLite
└── routes/
    ├── transactions.js # CRUD transactions + stats
    ├── budgets.js      # CRUD budgets + résumés
    └── export.js       # Export CSV
```

### Frontend (React)
```
frontend/
├── src/
│   ├── App.jsx         # Routing et navigation
│   ├── services/
│   │   └── api.js      # Client API axios
│   └── pages/
│       ├── Dashboard.jsx    # Vue d'ensemble
│       ├── Transactions.jsx # Gestion transactions
│       └── Budgets.jsx      # Gestion budgets
```

### CLI (Java - legacy)
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

- Les transactions et budgets sont partagés entre le CLI et l'interface web (même base SQLite)
- Le solde affiché = revenus - dépenses (basé sur les transactions)
- Les alertes de budget s'affichent automatiquement (interface web) ou lors du calcul (CLI)
- L'API backend est accessible sur `http://localhost:3001`
- L'interface web est accessible sur `http://localhost:5173`
- Les fichiers CSV exportés incluent : ID, catégorie, montant, type, description, date


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
