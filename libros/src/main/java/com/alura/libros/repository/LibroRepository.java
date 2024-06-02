package com.alura.libros.repository;

import com.alura.libros.model.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByIdioma(String idioma);
    List<Libro> findByAutorContaining(String autor);
    List<Libro> findByTituloContainingIgnoreCase(String terminoBusqueda);
}

