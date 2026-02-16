package com.mybudget.model;

import java.math.BigDecimal;
import java.util.Objects;

public class Budget {
    private Long id;
    private String categorie;
    private int mois;
    private int annee;
    private BigDecimal limite;

    public Budget() {
    }

    public Budget(String categorie, int mois, int annee, BigDecimal limite) {
        this.categorie = categorie;
        this.mois = mois;
        this.annee = annee;
        this.limite = limite;
    }

    public Budget(Long id, String categorie, int mois, int annee, BigDecimal limite) {
        this.id = id;
        this.categorie = categorie;
        this.mois = mois;
        this.annee = annee;
        this.limite = limite;
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

    public int getMois() {
        return mois;
    }

    public void setMois(int mois) {
        this.mois = mois;
    }

    public int getAnnee() {
        return annee;
    }

    public void setAnnee(int annee) {
        this.annee = annee;
    }

    public BigDecimal getLimite() {
        return limite;
    }

    public void setLimite(BigDecimal limite) {
        this.limite = limite;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Budget budget = (Budget) o;
        return Objects.equals(id, budget.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", categorie='" + categorie + '\'' +
                ", mois=" + mois +
                ", annee=" + annee +
                ", limite=" + limite +
                '}';
    }
}
