package com.mybudget.repository;

import com.mybudget.model.Budget;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BudgetRepository {
    private final DatabaseManager databaseManager;

    public BudgetRepository(DatabaseManager databaseManager) {
        this.databaseManager = databaseManager;
    }

    public Budget enregistrer(Budget budget) {
        String sql = "INSERT INTO budgets (categorie, mois, annee, limite) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, budget.getCategorie());
            pstmt.setInt(2, budget.getMois());
            pstmt.setInt(3, budget.getAnnee());
            pstmt.setBigDecimal(4, budget.getLimite());
            
            pstmt.executeUpdate();
            
            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    budget.setId(generatedKeys.getLong(1));
                }
            }
            
            return budget;
        } catch (SQLException e) {
            throw new RuntimeException("Échec de l'enregistrement du budget", e);
        }
    }

    public Optional<Budget> trouverParCategorieEtMoisEtAnnee(String categorie, int mois, int annee) {
        String sql = "SELECT id, categorie, mois, annee, limite FROM budgets WHERE categorie = ? AND mois = ? AND annee = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, categorie);
            pstmt.setInt(2, mois);
            pstmt.setInt(3, annee);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapperVersBudget(rs));
                }
            }
            
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la récupération du budget", e);
        }
    }

    public List<Budget> trouverTout() {
        String sql = "SELECT id, categorie, mois, annee, limite FROM budgets ORDER BY annee DESC, mois DESC";
        List<Budget> budgets = new ArrayList<>();
        
        try (Connection conn = databaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                budgets.add(mapperVersBudget(rs));
            }
            
            return budgets;
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la récupération des budgets", e);
        }
    }

    public void modifier(Budget budget) {
        String sql = "UPDATE budgets SET limite = ? WHERE id = ?";
        
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setBigDecimal(1, budget.getLimite());
            pstmt.setLong(2, budget.getId());
            
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new RuntimeException("Échec de la modification du budget", e);
        }
    }

    private Budget mapperVersBudget(ResultSet rs) throws SQLException {
        return new Budget(
            rs.getLong("id"),
            rs.getString("categorie"),
            rs.getInt("mois"),
            rs.getInt("annee"),
            rs.getBigDecimal("limite")
        );
    }
}
