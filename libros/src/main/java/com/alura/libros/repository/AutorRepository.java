package com.alura.libros.repository;


import com.alura.libros.model.Autor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AutorRepository extends JpaRepository<Autor, Long> {
    @Query("SELECT a FROM Autor a WHERE (a.nacimiento <= ?1 AND a.fallecimiento > ?1) OR (a.nacimiento <= ?1 AND a.fallecimiento IS NULL)")
    List<Autor> findAutoresVivos(int año);
}
