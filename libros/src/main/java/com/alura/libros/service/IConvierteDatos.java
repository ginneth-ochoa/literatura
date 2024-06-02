package com.alura.libros.service;

import com.fasterxml.jackson.databind.JsonNode;

public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
    JsonNode convertirStringAJson(String json);
}

