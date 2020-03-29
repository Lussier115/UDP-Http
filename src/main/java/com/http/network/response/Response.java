package com.http.network.response;

import com.http.network.exception.RedirectException;
import com.http.network.request.Request;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Scanner;

public class Response {

    private ArrayList<String> headers = new ArrayList<>();
    private ArrayList<String> body = new ArrayList<>();

    private boolean redirect = false;

    Request request;

    public Response(Scanner in, Request request) throws RedirectException {

        boolean isHeaderContent = true;

        while (in.hasNextLine()) {
            String content = in.nextLine();
            if (content.length() < 1) {
                isHeaderContent = false;
            }

            if (isHeaderContent) {

                if (content.split(" ")[1].matches("3\\d\\d")) {
                    redirect = true;
                }

                if (content.contains("Location") && redirect) {
                    String urlRedirect = content.split(":", 2)[1];
                    throw new RedirectException(urlRedirect);
                }

                this.headers.add(content);
            }
            if (!isHeaderContent) {
                if (content.length() > 1) {
                    this.body.add(content);
                }
            }
        }

        this.request = request;
    }

    public void display() {

        if (!request.isFileOutput()) {
            System.out.print("\r\n");

            if (request.isVerbose()) {
                for (String content : headers) {
                    System.out.println(content);
                }
                System.out.print("\r\n");
            }

            for (String content : body) {
                System.out.println(content);
            }

            System.out.println();
        } else {
            this.writeToFile();
        }
    }

    private void writeToFile() {
        try {
            PrintWriter writer = new PrintWriter(request.getOutputLocation(), "UTF-8");
            if (request.isVerbose()) {
                for (String content : headers) {
                    writer.println(content);
                }
                writer.print("\r\n");
            }

            for (String content : body) {
                writer.println(content);
            }
            writer.close();
            System.out.println("Response printed to: " + request.getOutputLocation());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
