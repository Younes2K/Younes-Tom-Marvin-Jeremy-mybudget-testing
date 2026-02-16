package com.mybudget.repository;

import com.mybudget.model.Transaction;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepository {
    private final DatabaseManager databaseManager;

    public TransactionRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Transaction enregistrer(Transaction transaction) {
        String sql = "INSERT INTO transactions (categorie, montant, description, date) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, transaction.getCategorie());
            pstmt.setBigDecimal(2, transaction.getMontant());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getDate().toString());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    transaction.setId(generatedKeys.getLong(1));
                }
            }
            
            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException("Échec de l'enregistrement de la transaction", e);
        }
    }

    public List<Transaction> trouverTout() {
        String sql = "SELECT id, categorie, montant, description, date FROM transactions ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                transactions.add(mapperVersTransaction(rs));
            }
            
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la récupération des transactions", e);
        }
    }

    public List<Transaction> trouverParCategorie(String categorie) {
        String sql = "SELECT id, categorie, montant, description, date FROM transactions WHERE categorie = ? ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, categorie);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapperVersTransaction(rs));
                }
            }
            
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la récupération des transactions par catégorie", e);
        }
    }

    public List<Transaction> trouverParMoisEtAnnee(int mois, int annee) {
        String sql = "SELECT id, categorie, montant, description, date FROM transactions WHERE strftime('%m', date) = ? AND strftime('%Y', date) = ? ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, String.format("%02d", mois));
            pstmt.setString(2, String.valueOf(annee));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapperVersTransaction(rs));
                }
            }
            
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la récupération des transactions par mois", e);
        }
    }

    public List<Transaction> trouverParCategorieEtMoisEtAnnee(String categorie, int mois, int annee) {
        String sql = "SELECT id, categorie, montant, description, date FROM transactions WHERE categorie = ? AND strftime('%m', date) = ? AND strftime('%Y', date) = ? ORDER BY date DESC";
        List<Transaction> transactions = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, categorie);
            pstmt.setString(2, String.format("%02d", mois));
            pstmt.setString(3, String.valueOf(annee));
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(mapperVersTransaction(rs));
                }
            }
            
            return transactions;
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la récupération des transactions", e);
        }
    }

    public void supprimerParId(Long id) {
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la suppression de la transaction", e);
        }
    }

    public void modifier(Transaction transaction) {
        String sql = "UPDATE transactions SET categorie = ?, montant = ?, description = ?, date = ? WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, transaction.getCategorie());
            pstmt.setBigDecimal(2, transaction.getMontant());
            pstmt.setString(3, transaction.getDescription());
            pstmt.setString(4, transaction.getDate().toString());
            pstmt.setLong(5, transaction.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la modification de la transaction", e);
        }
    }

    private Transaction mapperVersTransaction(ResultSet rs) throws SQLException {
        return new Transaction(
            rs.getLong("id"),
            rs.getString("categorie"),
            rs.getBigDecimal("montant"),
            rs.getString("description"),
            LocalDate.parse(rs.getString("date"))
        );
    }
}
