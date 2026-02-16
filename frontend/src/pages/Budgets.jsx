import { useState, useEffect } from 'react'
import { budgetsAPI } from '../services/api'

function Budgets() {
  const [budgets, setBudgets] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [formData, setFormData] = useState({
    categorie: '',
    montant_limite: '',
    mois: new Date().getMonth() + 1,
    annee: new Date().getFullYear()
  })

  useEffect(() => {
    loadBudgets()
  }, [])

  const loadBudgets = async () => {
    try {
      const res = await budgetsAPI.getAll()
      
      // Charger les résumés pour chaque budget
      const budgetsWithSummary = await Promise.all(
        res.data.map(async (budget) => {
          const summary = await budgetsAPI.getSummary(budget.id)
          return summary.data
        })
      )
      
      setBudgets(budgetsWithSummary)
      setLoading(false)
    } catch (error) {
      console.error('Erreur lors du chargement des budgets:', error)
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await budgetsAPI.create(formData)
      setShowModal(false)
      resetForm()
      loadBudgets()
    } catch (error) {
      console.error('Erreur:', error)
      alert(error.response?.data?.error || 'Erreur lors de la création du budget')
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer ce budget ?')) {
      try {
        await budgetsAPI.delete(id)
        loadBudgets()
      } catch (error) {
        console.error('Erreur:', error)
        alert('Erreur lors de la suppression')
      }
    }
  }

  const resetForm = () => {
    setFormData({
      categorie: '',
      montant_limite: '',
      mois: new Date().getMonth() + 1,
      annee: new Date().getFullYear()
    })
  }

  if (loading) {
    return <div className="card">Chargement...</div>
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Budgets</h1>
        <button className="btn" onClick={() => { resetForm(); setShowModal(true); }}>
          ➕ Nouveau budget
        </button>
      </div>

      {budgets.length > 0 ? (
        <div style={{ display: 'grid', gap: '1.5rem' }}>
          {budgets.map(budget => (
            <div key={budget.id} className="card">
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: '1rem' }}>
                <div>
                  <h2 style={{ marginBottom: '0.5rem' }}>{budget.categorie}</h2>
                  <p style={{ color: '#6b7280' }}>
                    Période: {budget.mois}/{budget.annee}
                  </p>
                </div>
                <button 
                  className="btn btn-danger"
                  style={{ padding: '0.5rem 1rem' }}
                  onClick={() => handleDelete(budget.id)}
                >
                  🗑️ Supprimer
                </button>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: '1rem', marginBottom: '1rem' }}>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Limite</p>
                  <p style={{ fontSize: '1.5rem', fontWeight: 'bold' }}>
                    {budget.montant_limite.toFixed(2)} €
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Dépensé</p>
                  <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: '#ef4444' }}>
                    {budget.depense.toFixed(2)} €
                  </p>
                </div>
                <div>
                  <p style={{ fontSize: '0.875rem', color: '#6b7280', marginBottom: '0.25rem' }}>Restant</p>
                  <p style={{ fontSize: '1.5rem', fontWeight: 'bold', color: budget.restant >= 0 ? '#10b981' : '#ef4444' }}>
                    {budget.restant.toFixed(2)} €
                  </p>
                </div>
              </div>

              <div style={{ marginBottom: '0.5rem', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span style={{ fontSize: '0.875rem', color: '#6b7280' }}>Progression</span>
                <span className={`badge ${budget.alerte || 'success'}`}>
                  {budget.pourcentage.toFixed(0)}%
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
                  marginTop: '1rem', 
                  padding: '0.75rem', 
                  background: '#fee2e2', 
                  color: '#991b1b', 
                  borderRadius: '6px',
                  fontWeight: '500'
                }}>
                  ⚠️ Budget dépassé de {Math.abs(budget.restant).toFixed(2)} € !
                </div>
              )}
              
              {budget.alerte === 'warning' && (
                <div style={{ 
                  marginTop: '1rem', 
                  padding: '0.75rem', 
                  background: '#fef3c7', 
                  color: '#92400e', 
                  borderRadius: '6px',
                  fontWeight: '500'
                }}>
                  ⚠️ Attention, vous avez utilisé {budget.pourcentage.toFixed(0)}% de votre budget
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <div className="card">
          <div className="empty-state">
            <h3>Aucun budget défini</h3>
            <p>Créez votre premier budget pour suivre vos dépenses par catégorie</p>
          </div>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>Nouveau budget</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Catégorie *</label>
                <input
                  type="text"
                  value={formData.categorie}
                  onChange={(e) => setFormData({ ...formData, categorie: e.target.value })}
                  placeholder="Ex: Alimentation, Transport, Loisirs..."
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Montant limite (€) *</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.montant_limite}
                  onChange={(e) => setFormData({ ...formData, montant_limite: e.target.value })}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Mois *</label>
                <select
                  value={formData.mois}
                  onChange={(e) => setFormData({ ...formData, mois: parseInt(e.target.value) })}
                  required
                >
                  {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12].map(m => (
                    <option key={m} value={m}>
                      {new Date(2000, m - 1).toLocaleString('fr-FR', { month: 'long' })}
                    </option>
                  ))}
                </select>
              </div>
              
              <div className="form-group">
                <label>Année *</label>
                <input
                  type="number"
                  value={formData.annee}
                  onChange={(e) => setFormData({ ...formData, annee: parseInt(e.target.value) })}
                  required
                />
              </div>
              
              <div className="modal-actions">
                <button 
                  type="button" 
                  className="btn btn-secondary"
                  onClick={() => setShowModal(false)}
                >
                  Annuler
                </button>
                <button type="submit" className="btn">
                  Créer
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default Budgets
