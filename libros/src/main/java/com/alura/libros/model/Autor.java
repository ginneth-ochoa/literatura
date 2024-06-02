package com.alura.libros.model;

import jakarta.persistence.*;
import java.util.Set;
import java.util.HashSet;


@Entity
public class Autor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Integer nacimiento;
    private Integer fallecimiento;

    @ElementCollection
    private Set<String> libros = new HashSet<>();

    public Autor() {
    }

    public Autor(String nombre, Integer nacimiento, Integer fallecimiento) {
        this.nombre = nombre;
        this.nacimiento = nacimiento;
        this.fallecimiento = fallecimiento;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Integer getNacimiento() {
        return nacimiento;
    }

    public Integer getFallecimiento() {
        return fallecimiento;
    }

    public Set<String> getLibros() {
        return libros;
    }

    public void setLibros(Set<String> libros) {
        this.libros = libros;
    }
}


