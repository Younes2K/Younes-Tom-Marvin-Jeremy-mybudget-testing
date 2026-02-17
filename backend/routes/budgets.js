import express from 'express';
import db from '../db/init.js';

const router = express.Router();

// GET - Récupérer tous les budgets
router.get('/', (req, res) => {
  try {
    const { mois, annee } = req.query;
    
    let query = 'SELECT * FROM budgets WHERE 1=1';
    const params = [];

    if (mois) {
      query += ' AND mois = ?';
      params.push(parseInt(mois));
    }
    if (annee) {
      query += ' AND annee = ?';
      params.push(parseInt(annee));
    }
    
    query += ' ORDER BY annee DESC, mois DESC';

    const stmt = db.prepare(query);
    const budgets = stmt.all(...params);
    
    res.json(budgets);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET - Récupérer un budget par ID
router.get('/:id', (req, res) => {
  try {
    const stmt = db.prepare('SELECT * FROM budgets WHERE id = ?');
    const budget = stmt.get(req.params.id);
    
    if (!budget) {
      return res.status(404).json({ error: 'Budget non trouvé' });
    }
    
    res.json(budget);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST - Créer un nouveau budget
router.post('/', (req, res) => {
  try {
    const { categorie, limite, mois, annee } = req.body;
    
    // Validation
    if (!categorie || !limite || !mois || !annee) {
      return res.status(400).json({ error: 'Champs requis manquants' });
    }
    
    if (limite <= 0) {
      return res.status(400).json({ error: 'Le montant limite doit être positif' });
    }
    
    if (mois < 1 || mois > 12) {
      return res.status(400).json({ error: 'Mois invalide (1-12)' });
    }

    const stmt = db.prepare(`
      INSERT INTO budgets (categorie, limite, mois, annee)
      VALUES (?, ?, ?, ?)
    `);
    
    try {
      const result = stmt.run(categorie, limite, mois, annee);
      
      res.status(201).json({
        id: result.lastInsertRowid,
        categorie,
        limite,
        mois,
        annee
      });
    } catch (err) {
      if (err.message.includes('UNIQUE')) {
        return res.status(409).json({ error: 'Un budget existe déjà pour cette catégorie et période' });
      }
      throw err;
    }
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// PUT - Modifier un budget
router.put('/:id', (req, res) => {
  try {
    const { limite } = req.body;
    
    if (limite && limite <= 0) {
      return res.status(400).json({ error: 'Le montant limite doit être positif' });
    }

    const stmt = db.prepare(`
      UPDATE budgets
      SET limite = COALESCE(?, limite)
      WHERE id = ?
    `);
    
    const result = stmt.run(limite, req.params.id);
    
    if (result.changes === 0) {
      return res.status(404).json({ error: 'Budget non trouvé' });
    }
    
    res.json({ message: 'Budget modifié avec succès' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE - Supprimer un budget
router.delete('/:id', (req, res) => {
  try {
    const stmt = db.prepare('DELETE FROM budgets WHERE id = ?');
    const result = stmt.run(req.params.id);
    
    if (result.changes === 0) {
      return res.status(404).json({ error: 'Budget non trouvé' });
    }
    
    res.json({ message: 'Budget supprimé avec succès' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET - Résumé d'un budget (dépensé, restant, pourcentage)
router.get('/:id/summary', (req, res) => {
  try {
    const budget = db.prepare('SELECT * FROM budgets WHERE id = ?').get(req.params.id);
    
    if (!budget) {
      return res.status(404).json({ error: 'Budget non trouvé' });
    }
    
    // Calculer les dépenses pour cette catégorie et période
    const dateDebut = `${budget.annee}-${String(budget.mois).padStart(2, '0')}-01`;
    const dateFin = budget.mois === 12 
      ? `${budget.annee + 1}-01-01`
      : `${budget.annee}-${String(budget.mois + 1).padStart(2, '0')}-01`;
    
    const depensesStmt = db.prepare(`
      SELECT COALESCE(SUM(ABS(montant)), 0) as total
      FROM transactions
      WHERE categorie = ? 
        AND montant < 0
        AND date >= ? 
        AND date < ?
    `);
    
    const depenses = depensesStmt.get(budget.categorie, dateDebut, dateFin);
    const depense = depenses.total;
    const restant = budget.limite - depense;
    const pourcentage = (depense / budget.limite) * 100;
    
    let alerte = null;
    if (pourcentage >= 100) {
      alerte = 'danger';
    } else if (pourcentage >= 80) {
      alerte = 'warning';
    }
    
    res.json({
      ...budget,
      depense,
      restant,
      pourcentage: Math.round(pourcentage * 100) / 100,
      alerte
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;
