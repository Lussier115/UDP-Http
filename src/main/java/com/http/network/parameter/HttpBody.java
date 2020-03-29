package com.http.network.parameter;

public class HttpBody {

    String body;
    BodyType type;

    public enum BodyType {INLINE, FILE}

    public HttpBody(String body, BodyType type) {
        this.body = body;
        this.type = type;
    }

    public String toString() {
        return this.getContentLength() + "\r\n" + this.getBody();
    }

    public String fsToString() { return this.getContentLength() + this.getBody(); }

    public String getContentLength() {
        return "Content-Length: " + this.getBody().length() + "\r\n";
    }

    public String getBody() {

        if (type == BodyType.INLINE) {
            return "{" + body + "}";
        }

        return body;
    }
}
