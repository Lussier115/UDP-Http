package ca.concordia;

import ca.concordia.helper.ByteHelper;
import ca.concordia.network.exception.RedirectException;
import ca.concordia.network.request.Request;
import ca.concordia.network.response.Response;
import ch.qos.logback.core.encoder.ByteArrayUtil;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;
import ca.concordia.httpfs.httpfs;

public class UDPServer {

    private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
    private static final ByteHelper helper = new ByteHelper();

    private static final int DATA = 0;
    private static final int SYN = 1;
    private static final int SYNACK = 2;
    private static final int ACK = 3;
    private static final int NACK = 4;
    private static final int FIN = 5;

    private HashMap<Long, byte[]> requestPackets = new HashMap<Long, byte[]>();
    private HashMap<Long, Packet> responsePackets = new HashMap<>();
    private boolean sendResponse = false;

    private SocketAddress routerAddress;
    private DatagramChannel channel;

    private void listenAndServe(int port) throws IOException {

        try (DatagramChannel channelX = DatagramChannel.open()) {
            this.setDatagramChanel(channelX);

            channel.bind(new InetSocketAddress(port));

            logger.info("Server is listening at {}", channel.getLocalAddress());
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {

                buf.clear();
                SocketAddress router = channel.receive(buf);
                this.setRouterAddress(router);

                HashMap<Long, Packet> packetMap = readPackets(buf);

                if (packetMap != null) {
                    if (!sendResponse)
                        for (Map.Entry<Long, Packet> packet : packetMap.entrySet()) {

                            if (packet.getValue().getType() == SYN) {
                                logger.info("SYN received");
                            } else if (packet.getValue().getType() == DATA) {

                                //TODO LINK TO httpfs
                                String mainPath = System.getProperty("user.dir") + "/src/main/java/ca/concordia/httpfs/httpfs";
                                httpfs server = new httpfs(8007, mainPath);
//                                server.start();

                                String payload = new String(packet.getValue().getPayload(), UTF_8);
                                String[] line = payload.split(" ");
                                String filePath = "";

                                try {
                                    filePath = new URL(line[1]).getPath();
                                } catch (MalformedURLException e) {
                                    // it wasn't a URL
                                }

                                if (line[0].toLowerCase().contains("get")) {
                                    if ((filePath.contentEquals("")) || (filePath.contentEquals("/"))) {
                                        /* PART 2: QUESTION 1*/
                                        //TODO How to get Writer?
                                        server.readAllFiles(writer);
                                    } else {
                                        /* PART 2: QUESTION 2*/
                                        //TODO How to get Writer?
                                        server.readFile(filePath, writer);
                                    }
                                } else if (line[0].toLowerCase().contains("post")) {
                                    /* PART 2: QUESTION 3 */
                                    //TODO How to get Writer and Reader?
                                    server.postFile(filePath, writer, reader);
                                }

                                /**
                                 * Lines 79 to 105 are from httpfs.java
                                 * Do we just replace all the code from line 85 to 105 by:
                                 * server.start() ? on line 74
                                 * or we can transfer that code in this file?
                                 */

                                logger.info("Packet: {}", packet);
                                logger.info("Router: {}", router);
                                logger.info("Payload: {}", payload);

                            } else if (packet.getValue().getType() == ACK) {
                                String payload = new String(packet.getValue().getPayload(), UTF_8);
                                logger.info("Payload: {}", payload);
                            }

                            this.sendPacket(packet.getValue());
                        } else {
                            logger.info("Server : Verify packets");
                            while (responsePackets.size() != packetMap.size()) {
                                for (Map.Entry<Long, Packet> packet : packetMap.entrySet()) {

                                    //Check if packet was requested
                                    if (!responsePackets.containsValue(packet.getValue())) {
                                        //Send Packet
                                        sendPacket(packet.getValue());

                                        if (packet.getValue().getType() == FIN) {
                                            handleFINPacket(packet.getValue());
                                        }
                                    }
                                }
                            }
                        }
                }
            }
        }
    }

    private void setDatagramChanel(DatagramChannel channel) {
        this.channel = channel;
    }

    private void setRouterAddress(SocketAddress router) {
        this.routerAddress = router;
    }

    private void sendPacket(Packet packet) {
        try {
            logger.info("Sending Packet: {}", packet.getSequenceNumber());
            channel.send(packet.toBuffer(), routerAddress);
            logger.info("Sent Packet: {}", packet.getSequenceNumber());

        } catch (Exception e) {
            logger.error(e.toString());
        }
    }

    private HashMap<Long, Packet> readPackets(ByteBuffer buffer) throws IOException {
        Packet packet;

        buffer.flip();
        packet = Packet.fromBuffer(buffer);
        buffer.flip();

        if (packet.getType() == SYN) {
            logger.info("Handshake: SYN request");
            return handleHandShake(packet);

        } else if (packet.getType() == DATA) {
            logger.info("Data Packet");
            return handleDataPacket(packet);

        } else if (packet.getType() == FIN) {
            logger.info("Fin Packet: Received all Packets");
            return handleFINPacket(packet);

        } else if (packet.getType() == ACK) {
            logger.info("Handshake: ACK received");
        }

        return null;
    }

    private HashMap<Long, Packet> handleDataPacket(Packet packet) {

        //Add requestPackets to a map so later they can be joined and read.
        requestPackets.put(packet.getSequenceNumber(), packet.getPayload());
        Packet dataPacket = packet.toBuilder()
                .setSequenceNumber(packet.getSequenceNumber())
                .setType(ACK)
                .create();

        HashMap<Long, Packet> data = new HashMap<>();
        data.put(dataPacket.getSequenceNumber(), dataPacket);

        return data;
    }

    private HashMap<Long, Packet> handleHandShake(Packet packet) {
        HashMap<Long, Packet> handshake = new HashMap<>();

        String message = "SYNACK";
        Packet synPacket = packet.toBuilder()
                .setType(SYNACK)
                .setSequenceNumber(1L)
                .setPayload(message.getBytes())
                .create();

        handshake.put(synPacket.getSequenceNumber(), synPacket);
        return handshake;
    }


    private HashMap<Long, Packet> handleFINPacket(Packet packet) throws IOException {
        Response response = null;

        ByteBuffer buff = helper.getMergeBytes(requestPackets);

        try {
            Request request = helper.getRequestObject(buff.array());
            logger.info("Client Request: {}", request.toString());
            response = handleRequest(request);
        } catch (Exception e) {
            logger.error(e.toString());
        }

        sendResponse = true;
        return handleResponse(response, packet);
    }

    private HashMap<Long, Packet> handleResponse(Response response, Packet packet) {
//        HashMap<Long, Packet> responsePackets = new HashMap<>();

        logger.info("Response: {}", response.toString());
        byte[] payload = helper.getByteArray(response);

        if (payload.length < Packet.MAX_PAYLOAD) {
            logger.info("Payload size: {}", payload.length);
            logger.info("Sending Response Packet");

            Packet responsePacket = packet.toBuilder()
                    .setPayload(payload)
                    .setSequenceNumber(packet.getSequenceNumber())
                    .setType(FIN)
                    .create();

            responsePackets.put(responsePacket.getSequenceNumber(), responsePacket);
        } else {
            logger.info("Payload size: {} > Packet max payload: 1013 ", payload.length);

            return breakDownPackets(packet, payload);
        }


        return responsePackets;
    }

    private HashMap<Long, Packet> breakDownPackets(Packet packet, byte[] payload) {
//        HashMap<Long, Packet> responsePackets = new HashMap<>();

        int numberOfPackets = (Math.floorDiv(payload.length, Packet.MAX_PAYLOAD) + 1); // Number of packets needed to send the Payload.
        int offset = 0;

        for (int x = 0; x < numberOfPackets; x++) {
            byte[] newPayload = Arrays.copyOfRange(payload, offset, (offset + Packet.MAX_PAYLOAD));

            long seqNum = packet.getSequenceNumber();
            seqNum++;
            Packet responsePacket = null;
            logger.info("Response Packet: #{}", x);


            responsePacket = packet.toBuilder()
                    .setPayload(newPayload)
                    .setSequenceNumber(seqNum)
                    .setType(DATA)
                    .create();

            responsePackets.put(responsePacket.getSequenceNumber(), responsePacket);
            offset += Packet.MAX_PAYLOAD;
        }

        return responsePackets;
    }


    private Response handleRequest(Request request) throws Exception {
        Response response = null;

        if (!request.isValid())
            throw new Exception();

        // Open Socket
        int port = 80; //default port

        if (request.getPort() != -1) {
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
            // Redirection Handling
            request.setUrl(e.getRedirectURL());
            this.handleRequest(request);
        }

        out.close();
        in.close();
        socket.close();

        return response;
    }

    public static void main(String[] args) throws IOException {

        OptionParser parser = new OptionParser();
        parser.acceptsAll(asList("port", "p"), "Listening port")
                .withOptionalArg()
                .defaultsTo("8007");

        OptionSet opts = parser.parse(args);
        int port = Integer.parseInt((String) opts.valueOf("port"));
        UDPServer server = new UDPServer();
        server.listenAndServe(port);
    }
}