# Guide de résolution des problèmes

## Les budgets n'apparaissent pas dans le tableau de bord

### Solution rapide

1. **Arrêtez tous les processus**
   ```bash
   .\stop.ps1
   ```

2. **Relancez l'application**
   ```bash
   .\start.ps1
   ```

3. **Vérifiez les logs du backend**
   - Vous devriez voir : `📂 Chemin de la base de données: ...`
   - Et : `📊 Transactions: X, Budgets: Y`

### Vérifications

1. **Le backend utilise-t-il la bonne base de données ?**
   - Ouvrez le terminal backend
   - Cherchez le message `📂 Chemin de la base de données`
   - Il devrait pointer vers `C:\Users\...\testbudget\budget.db`

2. **Les données sont-elles bien enregistrées ?**
   - Créez un budget dans l'onglet "Budgets"
   - Attendez 5 secondes
   - Le tableau de bord devrait se mettre à jour automatiquement
   - Ou cliquez sur le bouton "Actualiser"

3. **Le frontend peut-il contacter le backend ?**
   - Ouvrez la console du navigateur (F12)
   - Créez un budget
   - Vérifiez qu'il n'y a pas d'erreurs rouges

## Le calcul des dépenses ne fonctionne pas

Pour qu'un budget affiche des dépenses :

1. Le budget doit avoir une **catégorie** (ex: "Alimentation")
2. Vous devez créer des **transactions** avec :
   - Type : **Dépense**
   - Catégorie : **Alimentation** (même nom exact)
   - Date : dans le **mois/année du budget**

Exemple :
- Budget : Catégorie "Alimentation", Février 2026, Limite 500€
- Transaction : Type "Dépense", Catégorie "Alimentation", Date "17/02/2026", Montant 50€
- Résultat : Le budget affichera 50€/500€ (10%)

## L'interface ne se met pas à jour

L'interface se rafraîchit automatiquement toutes les 5 secondes.

Si ça ne fonctionne pas :
1. Cliquez sur le bouton "Actualiser" dans le tableau de bord
2. Ou rechargez la page (F5)

## Problème de port déjà utilisé

Si vous voyez `EADDRINUSE: address already in use`:
```bash
.\stop.ps1
.\start.ps1
```

## Base de données corrompue

En dernier recours, supprimez `budget.db` à la racine du projet et relancez l'application.
