package com.shopcore.api;

/** Generic 404 for anything addressed in the URL: /orders/{number}, /customers/{id}/... */
public class ResourceNotFoundException extends RuntimeException {

    private final String resource;
    private final String id;

    public ResourceNotFoundException(String resource, Object id) {
        super(resource + " '" + id + "' not found");
        this.resource = resource;
        this.id = String.valueOf(id);
    }

    public String getResource() { return resource; }
    public String getId() { return id; }
}