import Database from 'better-sqlite3';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const dbPath = process.env.DB_PATH || path.join(__dirname, '..', 'budget.db');
const db = new Database(dbPath);

export function initDatabase() {
  // Table des transactions
  db.exec(`
    CREATE TABLE IF NOT EXISTS transactions (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      categorie TEXT NOT NULL,
      montant REAL NOT NULL,
      type TEXT NOT NULL CHECK(type IN ('revenu', 'depense')),
      description TEXT,
      date TEXT NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP
    )
  `);

  // Table des budgets
  db.exec(`
    CREATE TABLE IF NOT EXISTS budgets (
      id INTEGER PRIMARY KEY AUTOINCREMENT,
      categorie TEXT NOT NULL,
      montant_limite REAL NOT NULL,
      mois INTEGER NOT NULL,
      annee INTEGER NOT NULL,
      created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
      UNIQUE(categorie, mois, annee)
    )
  `);

  console.log('✅ Base de données initialisée');
}

export default db;
