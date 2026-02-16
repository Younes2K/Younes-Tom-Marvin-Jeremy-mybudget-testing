import express from 'express';
import db from '../db/init.js';

const router = express.Router();

// GET - Exporter les transactions en CSV
router.get('/csv', (req, res) => {
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
    
    // Générer le CSV
    let csv = 'ID,Catégorie,Montant,Type,Description,Date\n';
    
    transactions.forEach(t => {
      csv += `${t.id},"${t.categorie}",${t.montant},"${t.type}","${t.description || ''}","${t.date}"\n`;
    });
    
    res.setHeader('Content-Type', 'text/csv');
    res.setHeader('Content-Disposition', 'attachment; filename="transactions.csv"');
    res.send(csv);
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

export default router;
