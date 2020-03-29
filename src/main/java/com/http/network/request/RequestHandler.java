package com.http.network.request;

import com.http.network.exception.RedirectException;
import com.http.network.response.Response;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class RequestHandler {

    private final int PORT = 80;
    private static Response response;

    public RequestHandler() {
    }

    public Response send(Request request) throws Exception {

        if (!request.isValid())
            throw new Exception();

        // Open Socket
        int port = PORT;

        if(request.getPort() != -1){
            port = request.getPort();
        }

        InetAddress addressIp = InetAddress.getByName(request.getHost());
        Socket socket = new Socket(addressIp, port);

        PrintWriter out = new PrintWriter(socket.getOutputStream());
        Scanner in = new Scanner(socket.getInputStream());

        // Send request
        out.write(request.toString());
        out.flush();

        try {
            response = new Response(in, request);
        } catch (RedirectException e) {
            request.setUrl(e.getRedirectURL());
            this.send(request);
        }

        out.close();
        in.close();
        socket.close();

        return response;
    }
}