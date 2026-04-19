package com.photon.web.endpoints;

import io.javalin.config.JavalinConfig;
import io.javalin.http.Context;

/**
 * Interface representing an API endpoint in the application. Each endpoint must specify its path, HTTP method, and handler function.
 * 
 * @author Niwer
 */
public interface IEndpoint {
    /**
     * Returns the path for this endpoint (e.g., "/api/auth/sign-up"). This path is used to route incoming requests to the appropriate handler.
     * 
     * @return The path for this endpoint
     */
    String path();
    
    /**
     * Returns the HTTP method (GET, POST, PUT, DELETE) that this endpoint responds to.
     * 
     * @return The HTTP method for this endpoint
     */
    HttpMethod method();

    /**
     * Handles incoming requests to this endpoint. The implementation should process the request and set the appropriate response on the provided Context object.
     * 
     * @param handler The Javalin Context object representing the incoming request and response. The handler should use this object to read request data and set the response.
     */
    void handle(Context handler);

    /**
     * Registers the given endpoint class with the provided JavalinConfig. The endpoint class must have a no-argument constructor.
     * 
     * @param cfg The JavalinConfig to register the endpoint with
     * @param endpointClass The class of the endpoint to register
     * @return The instance of the registered endpoint
     */
    public static IEndpoint register(JavalinConfig cfg, Class<? extends IEndpoint> endpointClass) {
        try {
            final IEndpoint END_POINT = endpointClass.getDeclaredConstructor().newInstance();
            switch (END_POINT.method()) {
                case GET -> cfg.routes.get(END_POINT.path(), END_POINT::handle);
                case POST -> cfg.routes.post(END_POINT.path(), END_POINT::handle);
                case PUT -> cfg.routes.put(END_POINT.path(), END_POINT::handle);
                case DELETE -> cfg.routes.delete(END_POINT.path(), END_POINT::handle);
            }
            return END_POINT;
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate endpoint: " + endpointClass.getName(), e);
        }
    }

    public static enum HttpMethod {
        GET, POST, PUT, DELETE
    }
}