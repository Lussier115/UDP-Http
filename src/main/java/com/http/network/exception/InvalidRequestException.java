package com.http.network.exception;

public class InvalidRequestException extends Exception {
    public InvalidRequestException() {
        super("Invalid request.");
    }

    public InvalidRequestException(String message) {
        super("Invalid request: " + message);
    }

    @Override
    public String toString() {
        return this.getMessage();
    }
}
