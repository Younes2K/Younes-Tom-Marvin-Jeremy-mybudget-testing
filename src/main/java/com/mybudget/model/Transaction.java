package com.mybudget.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class Transaction {
    private Long id;
    private String categorie;
    private BigDecimal montant;
    private String description;
    private LocalDate date;

    public Transaction() {
    }

    public Transaction(String categorie, BigDecimal montant, String description, LocalDate date) {
        this.categorie = categorie;
        this.montant = montant;
        this.description = description;
        this.date = date;
    }

    public Transaction(Long id, String categorie, BigDecimal montant, String description, LocalDate date) {
        this.id = id;
        this.categorie = categorie;
        this.montant = montant;
        this.description = description;
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public BigDecimal getMontant() {
        return montant;
    }

    public void setMontant(BigDecimal montant) {
        this.montant = montant;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Transaction that = (Transaction) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", categorie='" + categorie + '\'' +
                ", montant=" + montant +
                ", description='" + description + '\'' +
                ", date=" + date +
                '}';
    }
}
