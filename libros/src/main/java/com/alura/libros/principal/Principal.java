package com.alura.libros.principal;

import com.alura.libros.model.Autor;
import com.alura.libros.model.Libro;
import com.alura.libros.repository.AutorRepository;
import com.alura.libros.repository.LibroRepository;
import com.alura.libros.service.ConsumoAPI;
import com.alura.libros.service.ConvierteDatos;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class Principal {

    private static final String URL_BASE = "https://gutendex.com/books/";

    @Autowired
    private ConsumoAPI consumoAPI;

    @Autowired
    private ConvierteDatos conversor;

    @Autowired
    private LibroRepository libroRepository;

    @Autowired
    private AutorRepository autorRepository;

    public final Scanner teclado = new Scanner(System.in);

    public void mostrarMenu() {
        int opcion = -1;

        while (opcion != 0) {
            System.out.println("""
                    Eliga la opcion de su preferencia: 
                    1 - Buscar libro por título
                    2 - Listar libros
                    3 - Listar autores
                    4 - Listar autores vivos en un año determinado
                    5 - Listar libros por idioma
                    0 - Salir
                    """);
            opcion = teclado.nextInt();
            teclado.nextLine();

            switch (opcion) {
                case 1 -> buscarLibroPorTitulo();
                case 2 -> listarLibros();
                case 3 -> listarAutores();
                case 4 -> listarAutoresVivos();
                case 5 -> listarLibrosPorIdioma();
                case 0 -> {
                    System.out.println("Saliendo de la aplicación...");
                    System.exit(0); // Sale del programa
                }

                default -> System.out.println("Opción inválida");
            }
        }
    }

    private void buscarLibroPorTitulo() {
        try {
            System.out.println("Ingrese el título del libro que desea buscar:");
            String terminoBusqueda = teclado.nextLine().toLowerCase(); // Término de búsqueda en minúsculas

            String url = URL_BASE + "?search=" + URLEncoder.encode(terminoBusqueda, StandardCharsets.UTF_8);

            String json = consumoAPI.obtenerDatos(url);
            JsonNode rootNode = conversor.convertirStringAJson(json);
            JsonNode results = rootNode.path("results");

            boolean libroEncontrado = false;

            for (JsonNode libroNode : results) {
                String titulo = libroNode.path("title").asText().toLowerCase();
                if (titulo.contains(terminoBusqueda)) {
                    String autor = libroNode.path("authors").get(0).path("name").asText();
                    String idioma = libroNode.path("languages").get(0).asText();
                    int descargas = libroNode.path("download_count").asInt();

                    System.out.println();
                    System.out.println("***************** LIBRO *****************");
                    System.out.println("Titulo: " + titulo);
                    System.out.println("Autor: " + autor);
                    System.out.println("Idioma: " + idioma);
                    System.out.println("Numero de descargas: " + descargas);
                    System.out.println("*******************************************");
                    System.out.println();

                    libroEncontrado = true;
                    break; // Si deseas mostrar solo el primer resultado relevante, puedes usar break
                }
            }

            if (!libroEncontrado) {
                System.out.println("No se encontraron libros con el título especificado.");
            }
        } catch (Exception e) {
            System.out.println("Error al buscar el libro: " + e.getMessage());
        }
    }

    public void listarLibros() {
        try {
            String json = consumoAPI.obtenerDatos(URL_BASE);
            JsonNode rootNode = conversor.convertirStringAJson(json);
            JsonNode results = rootNode.path("results");

            if (results.size() > 0) {
                System.out.println("Libros encontrados externamente:");
                for (JsonNode libroNode : results) {

                    String titulo = libroNode.path("title").asText();
                    String autor = libroNode.path("authors").get(0).path("name").asText();
                    String idioma = libroNode.path("languages").get(0).asText();
                    int descargas = libroNode.path("download_count").asInt();


                    Libro libro = new Libro(titulo, autor, idioma, descargas);
                    libroRepository.save(libro);

                    System.out.println();
                    System.out.println("*****************LIBRO*****************");
                    System.out.println("Titulo: " + titulo);
                    System.out.println("Autor: " + autor);
                    System.out.println("Idioma: " + idioma);
                    System.out.println("Numero de descargas: " + descargas);
                    System.out.println("***************************************");
                    System.out.println();
                }
            } else {
                System.out.println("No se encontraron libros en la base de datos externa.");
            }
        } catch (Exception e) {
            System.out.println("Error al listar los libros: " + e.getMessage());
        }
    }

    public void listarAutores() {
        try {
            String json = consumoAPI.obtenerDatos(URL_BASE + "?languages=en");
            JsonNode rootNode = conversor.convertirStringAJson(json);
            JsonNode results = rootNode.path("results");

            if (results.size() > 0) {
                Map<String, Autor> autoresMap = new HashMap<>();

                for (JsonNode libroNode : results) {
                    JsonNode authors = libroNode.path("authors");
                    for (JsonNode authorNode : authors) {
                        String nombre = authorNode.path("name").asText();
                        Integer nacimiento = authorNode.path("birth_year").asInt();
                        Integer fallecimiento = authorNode.path("death_year").asInt();

                        Autor autor = autoresMap.getOrDefault(nombre, new Autor(nombre, nacimiento, fallecimiento));

                        String tituloLibro = libroNode.path("title").asText();
                        autor.getLibros().add(tituloLibro);

                        autoresMap.put(nombre, autor);
                    }
                }

                Collection<Autor> autores = autoresMap.values();
                autorRepository.saveAll(autores);

                System.out.println("Autores encontrados:");
                for (Autor autor : autores) {
                    System.out.println();
                    System.out.println("*****************LIBRO*****************");
                    System.out.println("Nombre: " + autor.getNombre());
                    System.out.println("Fecha de nacimiento: " + autor.getNacimiento());
                    System.out.println("Fecha de fallecimiento: " + autor.getFallecimiento());
                    System.out.println("Libros:");
                    autor.getLibros().forEach(libro -> System.out.println("- " + libro));
                    System.out.println("***************************************");
                    System.out.println();
                }
            } else {
                System.out.println("No se encontraron autores en la base de datos externa.");
            }
        } catch (Exception e) {
            System.out.println("Error al listar los autores: " + e.getMessage());
        }
    }

    public void listarAutoresVivos() {
        try {
            System.out.println("Ingrese el año para buscar autores vivos:");
            int año = teclado.nextInt();
            teclado.nextLine(); // Limpiar el buffer de entrada

            String json = consumoAPI.obtenerDatos(URL_BASE + "?languages=en");
            JsonNode rootNode = conversor.convertirStringAJson(json);
            JsonNode results = rootNode.path("results");

            if (results.size() > 0) {
                Map<String, Autor> autoresMap = new HashMap<>();

                for (JsonNode libroNode : results) {
                    JsonNode authors = libroNode.path("authors");
                    for (JsonNode authorNode : authors) {
                        String nombre = authorNode.path("name").asText();
                        Integer nacimiento = authorNode.path("birth_year").isInt() ? authorNode.path("birth_year").asInt() : null;
                        Integer fallecimiento = authorNode.path("death_year").isInt() ? authorNode.path("death_year").asInt() : null;

                        Autor autor = autoresMap.getOrDefault(nombre, new Autor(nombre, nacimiento, fallecimiento));

                        String tituloLibro = libroNode.path("title").asText();
                        autor.getLibros().add(tituloLibro);

                        autoresMap.put(nombre, autor);
                    }
                }

                Collection<Autor> autores = autoresMap.values();
                autorRepository.saveAll(autores);

                List<Autor> autoresVivosEnAño = obtenerAutoresVivosEnAño(autores, año);

                if (autoresVivosEnAño.isEmpty()) {
                    System.out.println("No se encontraron autores vivos en el año especificado.");
                } else {
                    System.out.println("Autores vivos en el año " + año + ":");
                    for (Autor autor : autoresVivosEnAño) {
                        System.out.println();
                        System.out.println("*****************LIBRO*****************");
                        System.out.println("Nombre: " + autor.getNombre());
                        System.out.println("Fecha de nacimiento: " + autor.getNacimiento());
                        System.out.println("Fecha de fallecimiento: " + autor.getFallecimiento());
                        System.out.println("Libros:");
                        autor.getLibros().forEach(libro -> System.out.println("- " + libro));
                        System.out.println("***************************************");
                        System.out.println();
                    }
                }
            } else {
                System.out.println("No se encontraron autores en la base de datos externa.");
            }
        } catch (Exception e) {
            System.out.println("Error al listar los autores: " + e.getMessage());
        }
    }

    private List<Autor> obtenerAutoresVivosEnAño(Collection<Autor> autores, int año) {
        List<Autor> autoresVivosEnAño = new ArrayList<>();

        for (Autor autor : autores) {
            if ((autor.getNacimiento() != null && autor.getNacimiento() <= año) &&
                    (autor.getFallecimiento() == null || autor.getFallecimiento() >= año)) {
                autoresVivosEnAño.add(autor);
            }
        }

        return autoresVivosEnAño;
    }
    public void listarLibrosPorIdioma() {
        try {
            System.out.println("Ingrese el código del idioma " +
                    "(ES- español," +
                    " EN- ingles, " +
                    " FR- frances," +
                    " PT- portuges):");
            String idioma = teclado.nextLine().toLowerCase(); // Convertir a minúsculas para ser insensible a mayúsculas y minúsculas

            String json = consumoAPI.obtenerDatos(URL_BASE + "?languages=" + idioma);
            JsonNode rootNode = conversor.convertirStringAJson(json);
            JsonNode results = rootNode.path("results");

            if (results.size() > 0) {
                System.out.println("Libros en el idioma " + idioma + ":");
                for (JsonNode libroNode : results) {
                    String titulo = libroNode.path("title").asText();
                    String autor = "Autor desconocido";
                    JsonNode authorNode = libroNode.get("authors");
                    if (authorNode != null && authorNode.size() > 0) {
                        autor = authorNode.get(0).path("name").asText();
                    }
                    int descargas = libroNode.path("download_count").asInt();

                    // Imprimir información del libro
                    System.out.println();
                    System.out.println("*****************LIBRO*****************");
                    System.out.println("Título: " + titulo);
                    System.out.println("Autor: " + autor);
                    System.out.println("Idioma: " + idioma);
                    System.out.println("Número de descargas: " + descargas);
                    System.out.println("***************************************");
                    System.out.println();
                }
            } else {
                System.out.println("No se encontraron libros en el idioma especificado.");
            }
        } catch (Exception e) {
            System.out.println("Error al listar libros por idioma: " + e.getMessage());
        }
    }


}


