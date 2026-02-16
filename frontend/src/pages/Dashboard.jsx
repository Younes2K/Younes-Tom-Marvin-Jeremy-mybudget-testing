import { useState, useEffect } from 'react'
import { transactionsAPI, budgetsAPI } from '../services/api'
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts'

const COLORS = ['#667eea', '#764ba2', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6'];

function Dashboard() {
  const [stats, setStats] = useState({ revenus: 0, depenses: 0, solde: 0 })
  const [transactions, setTransactions] = useState([])
  const [budgets, setBudgets] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    loadData()
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
    return <div className="card">Chargement...</div>
  }

  return (
    <div>
      <h1 style={{ marginBottom: '2rem' }}>Tableau de bord</h1>
      
      <div className="stats-grid">
        <div className="stat-card">
          <h3>Revenus</h3>
          <div className="stat-value positive">
            {stats.revenus.toFixed(2)} €
          </div>
        </div>
        
        <div className="stat-card">
          <h3>Dépenses</h3>
          <div className="stat-value negative">
            {stats.depenses.toFixed(2)} €
          </div>
        </div>
        
        <div className="stat-card">
          <h3>Solde</h3>
          <div className={`stat-value ${stats.solde >= 0 ? 'positive' : 'negative'}`}>
            {stats.solde.toFixed(2)} €
          </div>
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
          <div style={{ display: 'grid', gap: '1rem' }}>
            {budgets.map(budget => (
              <div key={budget.id} style={{ padding: '1rem', background: '#f9fafb', borderRadius: '8px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                  <strong>{budget.categorie}</strong>
                  <span style={{ color: '#6b7280' }}>
                    {budget.mois}/{budget.annee}
                  </span>
                </div>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '0.5rem' }}>
                  <span>{budget.depense.toFixed(2)} € / {budget.montant_limite.toFixed(2)} €</span>
                  <span className={`badge ${budget.alerte || 'success'}`}>
                    {budget.pourcentage.toFixed(0)}%
                  </span>
                </div>
                <div className="progress-bar">
                  <div 
                    className={`progress-fill ${budget.alerte || 'success'}`}
                    style={{ width: `${Math.min(budget.pourcentage, 100)}%` }}
                  />
                </div>
                {budget.alerte === 'danger' && (
                  <div style={{ color: '#ef4444', fontSize: '0.875rem', marginTop: '0.5rem' }}>
                    ⚠️ Budget dépassé !
                  </div>
                )}
                {budget.alerte === 'warning' && (
                  <div style={{ color: '#f59e0b', fontSize: '0.875rem', marginTop: '0.5rem' }}>
                    ⚠️ Budget bientôt atteint
                  </div>
                )}
              </div>
            ))}
          </div>
        ) : (
          <div className="empty-state">
            <p>Aucun budget défini</p>
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
