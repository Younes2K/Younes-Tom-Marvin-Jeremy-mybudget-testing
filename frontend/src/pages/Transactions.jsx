import { useState, useEffect } from 'react'
import { transactionsAPI, exportAPI } from '../services/api'

function Transactions() {
  const [transactions, setTransactions] = useState([])
  const [loading, setLoading] = useState(true)
  const [showModal, setShowModal] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [filters, setFilters] = useState({
    categorie: '',
    type: '',
    dateDebut: '',
    dateFin: ''
  })
  const [formData, setFormData] = useState({
    categorie: '',
    montant: '',
    type: 'depense',
    description: '',
    date: new Date().toISOString().split('T')[0]
  })

  useEffect(() => {
    loadTransactions()
  }, [filters])

  const loadTransactions = async () => {
    try {
      const cleanFilters = Object.fromEntries(
        Object.entries(filters).filter(([_, v]) => v !== '')
      )
      const res = await transactionsAPI.getAll(cleanFilters)
      setTransactions(res.data)
      setLoading(false)
    } catch (error) {
      console.error('Erreur lors du chargement des transactions:', error)
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (editingId) {
        await transactionsAPI.update(editingId, formData)
      } else {
        await transactionsAPI.create(formData)
      }
      setShowModal(false)
      setEditingId(null)
      resetForm()
      loadTransactions()
    } catch (error) {
      console.error('Erreur:', error)
      alert(error.response?.data?.error || 'Erreur lors de l\'enregistrement')
    }
  }

  const handleEdit = (transaction) => {
    setFormData({
      categorie: transaction.categorie,
      montant: transaction.montant,
      type: transaction.type,
      description: transaction.description || '',
      date: transaction.date
    })
    setEditingId(transaction.id)
    setShowModal(true)
  }

  const handleDelete = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cette transaction ?')) {
      try {
        await transactionsAPI.delete(id)
        loadTransactions()
      } catch (error) {
        console.error('Erreur:', error)
        alert('Erreur lors de la suppression')
      }
    }
  }

  const resetForm = () => {
    setFormData({
      categorie: '',
      montant: '',
      type: 'depense',
      description: '',
      date: new Date().toISOString().split('T')[0]
    })
  }

  const handleExport = () => {
    const cleanFilters = Object.fromEntries(
      Object.entries(filters).filter(([_, v]) => v !== '')
    )
    exportAPI.downloadCSV(cleanFilters)
  }

  if (loading) {
    return <div className="card">Chargement...</div>
  }

  return (
    <div>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '2rem' }}>
        <h1>Transactions</h1>
        <div style={{ display: 'flex', gap: '1rem' }}>
          <button className="btn btn-secondary" onClick={handleExport}>
            📥 Exporter CSV
          </button>
          <button className="btn" onClick={() => { resetForm(); setShowModal(true); }}>
            ➕ Nouvelle transaction
          </button>
        </div>
      </div>

      <div className="card">
        <h2>Filtres</h2>
        <div className="filters">
          <div className="form-group">
            <label>Catégorie</label>
            <input
              type="text"
              value={filters.categorie}
              onChange={(e) => setFilters({ ...filters, categorie: e.target.value })}
              placeholder="Toutes"
            />
          </div>
          <div className="form-group">
            <label>Type</label>
            <select
              value={filters.type}
              onChange={(e) => setFilters({ ...filters, type: e.target.value })}
            >
              <option value="">Tous</option>
              <option value="revenu">Revenu</option>
              <option value="depense">Dépense</option>
            </select>
          </div>
          <div className="form-group">
            <label>Date début</label>
            <input
              type="date"
              value={filters.dateDebut}
              onChange={(e) => setFilters({ ...filters, dateDebut: e.target.value })}
            />
          </div>
          <div className="form-group">
            <label>Date fin</label>
            <input
              type="date"
              value={filters.dateFin}
              onChange={(e) => setFilters({ ...filters, dateFin: e.target.value })}
            />
          </div>
        </div>
      </div>

      <div className="card">
        <h2>Liste des transactions</h2>
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
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map(t => (
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
                    <td>
                      <button 
                        className="btn btn-secondary" 
                        style={{ marginRight: '0.5rem', padding: '0.5rem 1rem' }}
                        onClick={() => handleEdit(t)}
                      >
                        ✏️
                      </button>
                      <button 
                        className="btn btn-danger"
                        style={{ padding: '0.5rem 1rem' }}
                        onClick={() => handleDelete(t.id)}
                      >
                        🗑️
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : (
          <div className="empty-state">
            <h3>Aucune transaction</h3>
            <p>Ajoutez votre première transaction pour commencer</p>
          </div>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => { setShowModal(false); setEditingId(null); }}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <h2>{editingId ? 'Modifier' : 'Nouvelle'} transaction</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Catégorie *</label>
                <input
                  type="text"
                  value={formData.categorie}
                  onChange={(e) => setFormData({ ...formData, categorie: e.target.value })}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Montant (€) *</label>
                <input
                  type="number"
                  step="0.01"
                  value={formData.montant}
                  onChange={(e) => setFormData({ ...formData, montant: e.target.value })}
                  required
                />
              </div>
              
              <div className="form-group">
                <label>Type *</label>
                <select
                  value={formData.type}
                  onChange={(e) => setFormData({ ...formData, type: e.target.value })}
                  required
                >
                  <option value="depense">Dépense</option>
                  <option value="revenu">Revenu</option>
                </select>
              </div>
              
              <div className="form-group">
                <label>Description</label>
                <textarea
                  value={formData.description}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  rows="3"
                />
              </div>
              
              <div className="form-group">
                <label>Date *</label>
                <input
                  type="date"
                  value={formData.date}
                  onChange={(e) => setFormData({ ...formData, date: e.target.value })}
                  required
                />
              </div>
              
              <div className="modal-actions">
                <button 
                  type="button" 
                  className="btn btn-secondary"
                  onClick={() => { setShowModal(false); setEditingId(null); }}
                >
                  Annuler
                </button>
                <button type="submit" className="btn">
                  {editingId ? 'Modifier' : 'Ajouter'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}

export default Transactions
