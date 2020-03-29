package com.http.network.exception;

public class RedirectException extends Exception {

    public RedirectException(String url) {
        super(url);
    }

    public String getRedirectURL() {
        return this.getMessage();
    }
}
