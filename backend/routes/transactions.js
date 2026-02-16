import express from 'express';
import db from '../db/init.js';

const router = express.Router();

// GET - Récupérer toutes les transactions (avec filtres optionnels)
router.get('/', (req, res) => {
  try {
    const { categorie, type, dateDebut, dateFin } = req.query;
    
    let query = 'SELECT * FROM transactions WHERE 1=1';
    const params = [];

    if (categorie) {
      query += ' AND categorie = ?';
      params.push(categorie);
    }
    if (type) {
      query += ' AND type = ?';
      params.push(type);
    }
    if (dateDebut) {
      query += ' AND date >= ?';
      params.push(dateDebut);
    }
    if (dateFin) {
      query += ' AND date <= ?';
      params.push(dateFin);
    }
    
    query += ' ORDER BY date DESC';

    const stmt = db.prepare(query);
    const transactions = stmt.all(...params);
    
    res.json(transactions);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET - Récupérer une transaction par ID
router.get('/:id', (req, res) => {
  try {
    const stmt = db.prepare('SELECT * FROM transactions WHERE id = ?');
    const transaction = stmt.get(req.params.id);
    
    if (!transaction) {
      return res.status(404).json({ error: 'Transaction non trouvée' });
    }
    
    res.json(transaction);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// POST - Créer une nouvelle transaction
router.post('/', (req, res) => {
  try {
    const { categorie, montant, type, description, date } = req.body;
    
    // Validation
    if (!categorie || !montant || !type || !date) {
      return res.status(400).json({ error: 'Champs requis manquants' });
    }
    
    if (montant <= 0) {
      return res.status(400).json({ error: 'Le montant doit être positif' });
    }
    
    if (!['revenu', 'depense'].includes(type)) {
      return res.status(400).json({ error: 'Type invalide (revenu ou depense)' });
    }

    const stmt = db.prepare(`
      INSERT INTO transactions (categorie, montant, type, description, date)
      VALUES (?, ?, ?, ?, ?)
    `);
    
    const result = stmt.run(categorie, montant, type, description || '', date);
    
    res.status(201).json({
      id: result.lastInsertRowid,
      categorie,
      montant,
      type,
      description,
      date
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// PUT - Modifier une transaction
router.put('/:id', (req, res) => {
  try {
    const { categorie, montant, type, description, date } = req.body;
    
    if (montant && montant <= 0) {
      return res.status(400).json({ error: 'Le montant doit être positif' });
    }
    
    if (type && !['revenu', 'depense'].includes(type)) {
      return res.status(400).json({ error: 'Type invalide' });
    }

    const stmt = db.prepare(`
      UPDATE transactions
      SET categorie = COALESCE(?, categorie),
          montant = COALESCE(?, montant),
          type = COALESCE(?, type),
          description = COALESCE(?, description),
          date = COALESCE(?, date)
      WHERE id = ?
    `);
    
    const result = stmt.run(categorie, montant, type, description, date, req.params.id);
    
    if (result.changes === 0) {
      return res.status(404).json({ error: 'Transaction non trouvée' });
    }
    
    res.json({ message: 'Transaction modifiée avec succès' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// DELETE - Supprimer une transaction
router.delete('/:id', (req, res) => {
  try {
    const stmt = db.prepare('DELETE FROM transactions WHERE id = ?');
    const result = stmt.run(req.params.id);
    
    if (result.changes === 0) {
      return res.status(404).json({ error: 'Transaction non trouvée' });
    }
    
    res.json({ message: 'Transaction supprimée avec succès' });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

// GET - Statistiques globales
router.get('/stats/summary', (req, res) => {
  try {
    const revenus = db.prepare('SELECT COALESCE(SUM(montant), 0) as total FROM transactions WHERE type = "revenu"').get();
    const depenses = db.prepare('SELECT COALESCE(SUM(montant), 0) as total FROM transactions WHERE type = "depense"').get();
    
    res.json({
      revenus: revenus.total,
      depenses: depenses.total,
      solde: revenus.total - depenses.total
    });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;
