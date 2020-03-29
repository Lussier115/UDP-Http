package com.http.httpc;

import com.http.network.exception.InvalidRequestException;
import com.http.network.request.*;
import com.http.network.request.Request.RequestType;
import com.http.network.response.Response;

public class httpc {


    public static void main(String[] args) {
        Request request = null;
        RequestHandler requestHandler = new RequestHandler();
        RequestType requestType = null;

        try {

            /**
             * Help Command
             */
            if (args[0].toLowerCase().equals("help")) {
                if (args[1].toLowerCase().equals("post")) {
                    help("post");
                } else if (args[1].toLowerCase().equals("get")) {
                    help("get");
                } else {
                    help("help");
                }
            }

            /**
             * Curl Command
             */
            if (args[0].toLowerCase().equals("post")) {
                requestType = RequestType.POST;
                request = new PostRequest(args, requestType);

            } else if (args[0].toLowerCase().equals("get")) {
                requestType = RequestType.GET;
                request = new GetRequest(args, requestType);
            }

            /**
             * Send Request
             */

            if (request != null) {
                Response response = requestHandler.send(request);
                response.display();
            }

        } catch (InvalidRequestException e) {

            if (requestType == RequestType.GET) {
                help("get");
            } else if (requestType == RequestType.POST) {
                help("post");
            }

        } catch (Exception e2) {

            System.out.println("Error: Invalid Request");

            if (requestType == RequestType.GET) {
                help("get");
            } else if (requestType == RequestType.POST) {
                help("post");
            } else {
                help("help");
            }
        }
    }


    public static void help(String type) {
        String helpText = "";

        switch (type) {
            case "get":
                helpText = "\nUsage: httpc get [-v] [-h key:value] URL\n"
                        + "\nGet executes a HTTP GET request for a given URL.\n"
                        + "\t-v             Prints the detail of the response such as protocol, status and headers.\n"
                        + "\t-h key:value   Associates headers to HTTP Request with the format 'key:value'.";
                break;
            case "post":
                helpText = "\nUsage: httpc post [-v] [-h key:value] [-d inline-data] [-f file] URL \n"
                        + "\nPost executes a HTTP POST request for a given URL with inline data or from file. \n"
                        + "-v               Prints the detail of the response such as protocol, status and headers. \n"
                        + "-h key:value     Associates headers to HTTP Request with the format 'key:value'.\n"
                        + "-d string        Associates an inline data to the body HTTP POST request.\n"
                        + "-f file          Associates the content of a file to the body HTTP POST request.\n"
                        + "\nEither [-d] or [-f] can be used but not both.";
                break;
            case "help":
                helpText = "\nUsage: \n\thttpc command [arguments]"
                        + "\nThe commands are:\n"
                        + "\tget \texecutes a HTTP GET request and prints the response. \n"
                        + "\tpost\texecutes a HTTP POST request and prints the response. \n"
                        + "\thelp\tprints this screen. \n"
                        + "\nUse \"httpc help [command]\" for more information about a command.";
                break;
        }

        System.out.println(helpText);
    }
}
