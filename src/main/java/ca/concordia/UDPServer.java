package ca.concordia;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Arrays.asList;

public class UDPServer {

    private static final int DATA = 0;
    private static final int SYN = 1;
    private static final int SYNACK = 2;
    private static final int ACK = 3;
    private static final int NACK = 4;
    private static final int FIN = 5;

    private static final Logger logger = LoggerFactory.getLogger(UDPServer.class);
    private HashMap<Long, String> requestPackets = new HashMap<Long, String>();

    private void listenAndServe(int port) throws IOException {

        try (DatagramChannel channel = DatagramChannel.open()) {
            channel.bind(new InetSocketAddress(port));
            logger.info("EchoServer is listening at {}", channel.getLocalAddress());
            ByteBuffer buf = ByteBuffer
                    .allocate(Packet.MAX_LEN)
                    .order(ByteOrder.BIG_ENDIAN);

            for (; ; ) {

                buf.clear();
                SocketAddress router = channel.receive(buf);

                //TODO Read Packets
                readPackets(buf, router, channel);

                //TODO Send Response
                /**
                 * type = DATA, Send ACK packets
                 * type = FIN, Send response Packets
                 */


                /*
                // Parse a packet from the received raw data.
                buf.flip();
                Packet packet = Packet.fromBuffer(buf);
                buf.flip();

                String payload = new String(packet.getPayload(), UTF_8);
                logger.info("Packet: {}", packet);
                logger.info("Payload: {}", payload);
                logger.info("Router: {}", router);

                // Send the response to the router not the client.
                // The peer address of the packet is the address of the client already.
                // We can use toBuilder to copy properties of the current packet.
                // This demonstrate how to create a new packet from an existing packet.
                Packet resp = packet.toBuilder()
                        .setPayload(payload.getBytes())
                        .create();
                channel.send(resp.toBuffer(), router);
                 */

            }
        }
    }

    private HashMap<Long, Packet> readPackets(ByteBuffer buffer, SocketAddress router, DatagramChannel channel) throws IOException {
        Packet packet;

        buffer.flip();
        packet = Packet.fromBuffer(buffer);
        buffer.flip();
        String payload = new String(packet.getPayload(), StandardCharsets.UTF_8);


        if(packet.getType() == SYN) {
            logger.info("Handshake: SYN request");

            //TODO Handle handshake
            return handleHandShake(packet);

        } else if(packet.getType() == DATA) {
            //TODO Handle packet data: (1) Add packet data to requestPackets, (2) send ACK Packet

        }else if(packet.getType() == FIN) {
            //TODO Handle FIN packet: (1) Create Request, (2) Handle Request

        } else if(packet.getType() == ACK) {
            logger.info("Handshake: ACK received");
        }

        return null;
    }

    private HashMap<Long, Packet> handleHandShake(Packet packet) {

        //TODO

        return null;
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