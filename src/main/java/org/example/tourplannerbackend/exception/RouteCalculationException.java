package org.example.tourplannerbackend.exception;

public class RouteCalculationException extends RuntimeException {

    public RouteCalculationException(String message) {
        super(message);
    }

    public RouteCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}