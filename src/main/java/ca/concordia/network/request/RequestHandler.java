package ca.concordia.network.request;

import ca.concordia.UDPClient;
import ca.concordia.network.response.Response;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
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

        if (!request.isValid())
            throw new Exception();

        // Open Socket
        SocketAddress routerAddress = new InetSocketAddress(routerHost, routerPort);
        InetSocketAddress serverAddress = new InetSocketAddress(serverHost, serverPort);

        //Request object to Byte
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(request);


        //Create UDP CLient();
        udpClient = new UDPClient();
        udpClient.setMessage(baos.toByteArray());
        udpClient.runClient(routerAddress, serverAddress);

        return udpClient.getResponse();
    }
}