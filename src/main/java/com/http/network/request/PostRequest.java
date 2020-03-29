package com.http.network.request;

import com.http.network.exception.InvalidRequestException;
import com.http.network.parameter.HttpBody;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;


public class PostRequest extends Request {

    private HttpBody body;
    private boolean hasF = false;
    private boolean hasD = false;

    public PostRequest(String[] args, RequestType requestType) throws InvalidRequestException {
        this.setRequestType(requestType);
        body = new HttpBody("", HttpBody.BodyType.INLINE);

        for (int i = 1; i < args.length; i++) {

            if (args[i].equals("-f")) {
                hasF = true;
                body = new HttpBody(getFileData(args[i + 1]), HttpBody.BodyType.FILE);
            }

            if (args[i].equals("-d")) {
                hasD = true;
                body = new HttpBody(getInlineData(args), HttpBody.BodyType.INLINE);
            }

            if (hasD && hasF) {
                throw new InvalidRequestException("Cannot contain both -d and -f");
            }
        }

        this.parseRequest(args);
    }

    @Override
    public String toString() {
        String request = requestType.toString() + " " + this.url + " " + this.version +
                "\r\n" + this.headers.toString() + body.toString();

        return request;
    }

    private String getInlineData(String[] args) {
        String data = "";

        StringBuilder builder = new StringBuilder();
        for (String value : args) {
            builder.append(value);
        }

        String query = builder.toString().replaceAll("([A-Za-z]\\w+)", "\"$1\"");
        int startIndex = query.indexOf("{");
        int lastIndex = query.lastIndexOf("}");

        return query.substring(startIndex + 1, lastIndex);
    }

    private String getFileData(String filePath) {
        String data = "";

        try {
            File fileObj = new File(filePath.replaceAll("'", ""));
            Scanner myReader = new Scanner(fileObj);
            while (myReader.hasNextLine()) {
                data += myReader.nextLine() + "\n";
            }

            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return data;
    }
}
