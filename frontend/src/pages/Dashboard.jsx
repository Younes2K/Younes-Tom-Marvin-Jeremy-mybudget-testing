import { useState, useEffect } from 'react'
import { transactionsAPI, budgetsAPI } from '../services/api'
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip, BarChart, Bar, XAxis, YAxis, CartesianGrid } from 'recharts'

const COLORS = ['#667eea', '#764ba2', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6', '#06b6d4', '#ec4899'];

function Dashboard() {
  const [stats, setStats] = useState({ revenus: 0, depenses: 0, solde: 0 })
  const [transactions, setTransactions] = useState([])
  const [budgets, setBudgets] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
    // Rafraîchir toutes les 5 secondes pour détecter les changements
    const interval = setInterval(loadData, 5000)
    return () => clearInterval(interval)
  }, [])

  const loadData = async () => {
    try {
      const [statsRes, transRes, budgetsRes] = await Promise.all([
        transactionsAPI.getStats(),
        transactionsAPI.getAll(),
        budgetsAPI.getAll()
      ])
      
      setStats(statsRes.data)
      setTransactions(transRes.data)
      
      // Charger les résumés des budgets
      const budgetsWithSummary = await Promise.all(
        budgetsRes.data.map(async (budget) => {
          const summary = await budgetsAPI.getSummary(budget.id)
          return summary.data
        })
      )
      
      setBudgets(budgetsWithSummary)
      setLoading(false)
    } catch (error) {
      console.error('Erreur lors du chargement des données:', error)
      setLoading(false)
    }
  }

  // Calculer les dépenses par catégorie
  const depensesParCategorie = transactions
    .filter(t => t.type === 'depense')
    .reduce((acc, t) => {
      acc[t.categorie] = (acc[t.categorie] || 0) + t.montant
      return acc
    }, {})

  const chartData = Object.entries(depensesParCategorie).map(([name, value]) => ({
    name,
    value: Math.round(value * 100) / 100
  }))

  if (loading) {
    return (
      <div className="card">
        <div style={{ textAlign: 'center', padding: '3rem' }}>
          <div style={{ fontSize: '1.25rem', color: '#6b7280' }}>Chargement des données...</div>
        </div>
      </div>
    )
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '2rem' }}>
        <div>
          <h1>Tableau de bord</h1>
          <p style={{ color: 'rgba(255,255,255,0.8)', fontSize: '1.1rem', marginTop: '0.5rem' }}>
            Vue d'ensemble de vos finances
          </p>
        </div>
        <button 
          className="btn" 
          onClick={loadData}
          style={{ padding: '0.75rem 1.5rem' }}
        >
          Actualiser
        </button>
      </div>
      
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Revenus totaux</h3>
          <div className="stat-value positive">
            {stats.revenus.toFixed(2)} €
          </div>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.5rem' }}>
            Entrées d'argent
          </p>
        </div>
        
        <div className="stat-card">
          <h3>Dépenses totales</h3>
          <div className="stat-value negative">
            {stats.depenses.toFixed(2)} €
          </div>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.5rem' }}>
            Sorties d'argent
          </p>
        </div>
        
        <div className="stat-card">
          <h3>Solde actuel</h3>
          <div className={`stat-value ${stats.solde >= 0 ? 'positive' : 'negative'}`}>
            {stats.solde.toFixed(2)} €
          </div>
          <p style={{ fontSize: '0.875rem', color: '#6b7280', marginTop: '0.5rem' }}>
            {stats.solde >= 0 ? 'Excédent' : 'Déficit'}
          </p>
        </div>
      </div>

      <div className="card">
        <h2>Dépenses par catégorie</h2>
        {chartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <PieChart>
              <Pie
                data={chartData}
                cx="50%"
                cy="50%"
                labelLine={false}
                label={(entry) => `${entry.name}: ${entry.value}€`}
                outerRadius={80}
                fill="#8884d8"
                dataKey="value"
              >
                {chartData.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                ))}
              </Pie>
              <Tooltip />
              <Legend />
            </PieChart>
          </ResponsiveContainer>
        ) : (
          <div className="empty-state">
            <p>Aucune dépense enregistrée</p>
          </div>
        )}
      </div>

      <div className="card">
        <h2>Budgets actifs</h2>
        {budgets.length > 0 ? (
          <div style={{ display: 'grid', gap: '1.5rem' }}>
            {budgets.map(budget => (
              <div key={budget.id} style={{ 
                padding: '1.5rem', 
                background: 'linear-gradient(135deg, #f9fafb 0%, #ffffff 100%)', 
                borderRadius: '12px',
                border: '1px solid #e5e7eb',
                boxShadow: '0 2px 8px rgba(0,0,0,0.05)'
              }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem', alignItems: 'center' }}>
                  <div>
                    <strong style={{ fontSize: '1.125rem', color: '#1f2937' }}>{budget.categorie}</strong>
                    <div style={{ color: '#6b7280', fontSize: '0.875rem', marginTop: '0.25rem' }}>
                      Période: {budget.mois}/{budget.annee}
                    </div>
                  </div>
                  <span className={`badge ${budget.alerte || 'success'}`}>
                    {budget.pourcentage.toFixed(0)}%
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '1rem', fontSize: '0.9rem' }}>
                  <span style={{ color: '#6b7280' }}>
                    Dépensé: <strong style={{ color: '#ef4444' }}>{budget.depense.toFixed(2)} €</strong>
                  </span>
                  <span style={{ color: '#6b7280' }}>
                    Limite: <strong style={{ color: '#374151' }}>{budget.limite.toFixed(2)} €</strong>
                  </span>
                  <span style={{ color: '#6b7280' }}>
                    Restant: <strong style={{ color: budget.restant >= 0 ? '#10b981' : '#ef4444' }}>
                      {budget.restant.toFixed(2)} €
                    </strong>
                  </span>
                </div>
                <div className="progress-bar" style={{ height: '12px' }}>
                  <div 
                    className={`progress-fill ${budget.alerte || 'success'}`}
                    style={{ width: `${Math.min(budget.pourcentage, 100)}%` }}
                  />
                </div>
                {budget.alerte === 'danger' && (
                  <div style={{ 
                    color: '#991b1b', 
                    fontSize: '0.875rem', 
                    marginTop: '0.75rem',
                    padding: '0.5rem',
                    background: '#fee2e2',
                    borderRadius: '6px',
                    fontWeight: '600'
                  }}>
                    Attention: Budget dépassé de {Math.abs(budget.restant).toFixed(2)} €
                  </div>
                )}
                {budget.alerte === 'warning' && (
                  <div style={{ 
                    color: '#92400e', 
                    fontSize: '0.875rem', 
                    marginTop: '0.75rem',
                    padding: '0.5rem',
                    background: '#fef3c7',
                    borderRadius: '6px',
                    fontWeight: '600'
                  }}>
                    Attention: Vous avez utilisé {budget.pourcentage.toFixed(0)}% de votre budget
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <h3>Aucun budget défini</h3>
            <p>Créez votre premier budget pour suivre vos dépenses</p>
          </div>
        )}
      </div>

      <div className="card">
        <h2>Dernières transactions</h2>
        {transactions.length > 0 ? (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Date</th>
                  <th>Catégorie</th>
                  <th>Description</th>
                  <th>Type</th>
                  <th>Montant</th>
                </tr>
              </thead>
              <tbody>
                {transactions.slice(0, 10).map(t => (
                  <tr key={t.id}>
                    <td>{new Date(t.date).toLocaleDateString('fr-FR')}</td>
                    <td>{t.categorie}</td>
                    <td>{t.description || '-'}</td>
                    <td>
                      <span className={`badge ${t.type === 'revenu' ? 'success' : 'danger'}`}>
                        {t.type}
                      </span>
                    </td>
                    <td style={{ color: t.type === 'revenu' ? '#10b981' : '#ef4444', fontWeight: 'bold' }}>
                      {t.type === 'revenu' ? '+' : '-'}{t.montant.toFixed(2)} €
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <p>Aucune transaction enregistrée</p>
          </div>
        )}
      </div>
    </div>
  )
}

export default Dashboard
