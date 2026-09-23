package com.application.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "legal_entities")
public class LegalEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 12)
    private String inn;

    @Column(nullable = false, length = 15)
    private String ogrn;

    public LegalEntity() {}

    public LegalEntity(String name, String inn, String ogrn) {
        this.name = name;
        this.inn = inn;
        this.ogrn = ogrn;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInn() {
        return inn;
    }

    public void setInn(String inn) {
        this.inn = inn;
    }

    public String getOgrn() {
        return ogrn;
    }

    public void setOgrn(String ogrn) {
        this.ogrn = ogrn;
    }
}