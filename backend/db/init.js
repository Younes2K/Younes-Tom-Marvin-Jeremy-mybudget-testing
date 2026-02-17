import Database from 'better-sqlite3';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Chemin vers la racine du projet (2 niveaux au-dessus: db -> backend -> root)
const projectRoot = path.join(__dirname, '..', '..');
const dbPath = process.env.DB_PATH 
  ? (path.isAbsolute(process.env.DB_PATH) 
      ? process.env.DB_PATH 
      : path.join(projectRoot, process.env.DB_PATH))
  : path.join(projectRoot, 'budget.db');

console.log('📂 Chemin de la base de données:', dbPath);

const db = new Database(dbPath);

export function initDatabase() {
  // Table des transactions (compatible avec le CLI Java)
  db.exec(`
    CREATE TABLE IF NOT EXISTS transactions (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      categorie TEXT NOT NULL,
      montant REAL NOT NULL,
      description TEXT,
      date TEXT NOT NULL
    )
  `);

  // Table des budgets (compatible avec le CLI Java)
  db.exec(`
    CREATE TABLE IF NOT EXISTS budgets (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      categorie TEXT NOT NULL,
      mois INTEGER NOT NULL,
      annee INTEGER NOT NULL,
      limite REAL NOT NULL,
      UNIQUE(categorie, mois, annee)
    )
  `);

  console.log('✅ Base de données initialisée:', dbPath);
  
  // Afficher le nombre d'enregistrements pour debug
  const transCount = db.prepare('SELECT COUNT(*) as count FROM transactions').get();
  const budgetCount = db.prepare('SELECT COUNT(*) as count FROM budgets').get();
  console.log(`📊 Transactions: ${transCount.count}, Budgets: ${budgetCount.count}`);
}


export default db;
