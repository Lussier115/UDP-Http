package com.http.network.request;


import com.http.network.exception.InvalidRequestException;
import com.http.network.parameter.HttpHeader;

import java.net.MalformedURLException;
import java.net.URL;

public class Request {

    protected String version = "HTTP/1.0";
    protected String url;
    protected String host;
    protected int port;
    protected RequestType requestType;
    protected HttpHeader headers;

    private boolean verbose;
    private boolean fileOutput = false;
    private String outputLocation;

    public enum RequestType {POST, GET}

    public Request() {
    }

    public boolean isValid() {
        if (requestType == null) {
            return false;
        }

        if (!headers.isValid()) {
            return false;
        }

        return true;
    }

    public String toString() {
        String request = requestType.toString() + url + version + "\r\n";
        return request;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setRequestType(RequestType requestType) {
        this.requestType = requestType;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public boolean isFileOutput() {
        return fileOutput;
    }

    public String getOutputLocation() {
        return outputLocation;
    }

    public String getHost() {
        return this.host;
    }

    public int getPort(){
        return this.port;
    }

    protected void parseRequest(String[] args) throws InvalidRequestException {
        this.headers = new HttpHeader();
        int urlOffset = 1;

        for (String value : args) {
            if (value.equals("-o")) {
                urlOffset = 3;
                this.fileOutput = true;
            }
        }

        for (int i = 1; i < args.length; i++) {

            if (args[i].equals("-h")) {
                this.headers.parseLine(args[i + 1]);
            }

            if (args[i].equals("-v")) {
                this.setVerbose(true);
            }

            if (args[i].equals("-o")) {
                this.outputLocation = args[i + 1];
            }
            // Should be the last parameter of the curl
            if (i == (args.length - urlOffset)) {
                try {
                    String tempURL = args[i];

                    if (tempURL.startsWith("www")) {
                        tempURL = "http://".concat(tempURL);
                    }

                    URL url = new URL(tempURL);
                    this.host = url.getHost();
                    this.url = tempURL;
                    this.port = url.getPort();

                } catch (MalformedURLException e) {
                    // it wasn't a URL
                }
            }
        }
    }
}
