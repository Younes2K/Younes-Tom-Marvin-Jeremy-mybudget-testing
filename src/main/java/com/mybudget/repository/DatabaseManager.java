package com.mybudget.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private final String databaseUrl;

    public DatabaseManager(String databaseUrl) {
        this.databaseUrl = databaseUrl;
        initialiserBase();
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(databaseUrl);
    }

    private void initialiserBase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    categorie TEXT NOT NULL,
                    montant REAL NOT NULL,
                    description TEXT,
                    date TEXT NOT NULL
                )
            """);

            stmt.execute("""
                CREATE TABLE IF NOT EXISTS budgets (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    categorie TEXT NOT NULL,
                    mois INTEGER NOT NULL,
                    annee INTEGER NOT NULL,
                    limite REAL NOT NULL,
                    UNIQUE(categorie, mois, annee)
                )
            """);

        } catch (SQLException e) {
            throw new RuntimeException("Échec de l'initialisation de la base de données", e);
        }
    }
}
