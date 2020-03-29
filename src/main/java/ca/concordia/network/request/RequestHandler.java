package ca.concordia.network.request;

import ca.concordia.UDPClient;
import ca.concordia.network.response.Response;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class RequestHandler {

    private static Response response;
    private static UDPClient udpClient;

    protected int routerPort = 3000;
    protected int serverPort = 8007;
    protected String routerHost = "localhost";
    protected String serverHost = "localhost";


    public RequestHandler() {
    }

    public Response send(Request request) throws Exception {

        //Temp response
        response = new Response(request);


        if (!request.isValid())
            throw new Exception();


        // Open Socket
        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);

        //Create UDP CLient();
        udpClient = new UDPClient();
        udpClient.setMessage(request.toString());
        udpClient.runClient(routerAddress, serverAddress);


        //TODO Read response from udpClient
        /*
        try {
            response = new Response(udpClient.getResponse(), request);
        } catch (RedirectException e) {
            request.setUrl(e.getRedirectURL());
            this.send(request);
        }
        */

        return response;
    }
}