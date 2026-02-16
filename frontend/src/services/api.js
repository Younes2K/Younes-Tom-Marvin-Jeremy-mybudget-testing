import axios from 'axios';

const API_BASE_URL = 'http://localhost:3001/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Transactions
export const transactionsAPI = {
  getAll: (filters = {}) => api.get('/transactions', { params: filters }),
  getById: (id) => api.get(`/transactions/${id}`),
  create: (data) => api.post('/transactions', data),
  update: (id, data) => api.put(`/transactions/${id}`, data),
  delete: (id) => api.delete(`/transactions/${id}`),
  getStats: () => api.get('/transactions/stats/summary'),
};

// Budgets
export const budgetsAPI = {
  getAll: (filters = {}) => api.get('/budgets', { params: filters }),
  getById: (id) => api.get(`/budgets/${id}`),
  create: (data) => api.post('/budgets', data),
  update: (id, data) => api.put(`/budgets/${id}`, data),
  delete: (id) => api.delete(`/budgets/${id}`),
  getSummary: (id) => api.get(`/budgets/${id}/summary`),
};

// Export
export const exportAPI = {
  downloadCSV: (filters = {}) => {
    const params = new URLSearchParams(filters);
    window.open(`${API_BASE_URL}/export/csv?${params.toString()}`, '_blank');
  },
};

export default api;
