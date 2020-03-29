package ca.concordia;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.charset.StandardCharsets.UTF_8;

public class UDPClient {

    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private byte[] message;
    private String response;

    private final static int DATA = 0;

    // Handshake
    private final static int SYN = 1; //initial sequence number. The sequence number of the actual first data byte and the acknowledged number in the corresponding ACK are then this sequence number plus 1
    private final static int SYN_ACK = 2;

    // Packet
    private final static int ACK = 3;
    private final static int NACK = 4;
    private final static int FIN = 5; //Done sending all packets

    private HashMap<Long, Packet> packets;
    private ArrayList<Packet> ackPackets; //Ack Packets

    public void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {
            packets = new HashMap<Long, Packet>();

            long sequence = handShake(channel, routerAddr, serverAddr);

            //TODO Payload (message) must be managed so Packet Length it no exceeded.
            Packet p = new Packet.Builder()
                    .setType(DATA)
                    .setSequenceNumber(sequence)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(this.message)
                    .create();

            packets.put(p.getSequenceNumber(), p);


            // Send all Packets in HashMap
            for (Map.Entry<Long, Packet> packet : packets.entrySet()) {
                //Send Packet
                channel.send(packet.getValue().toBuffer(), routerAddr);

                //Receive Packet
                Packet responsePacket = receive(channel);

                if (responsePacket.getType() == ACK) {
                    ackPackets.add(responsePacket);
                }
            }

            // Check if all packets have been acknowledged, if not resend specific packet.
            logger.info("Client : Check acknowledged packets");

            /*
            while (ackPackets.size() != packets.size()) {
                for (Map.Entry<Long, Packet> packet : packets.entrySet()) {

                    //Check if packet has been ack
                    if (!ackPackets.contains(packet.getValue())) {
                        //Send Packet
                        channel.send(packet.getValue().toBuffer(), routerAddr);

                        //Receive Packet
                        Packet responsePacket = receive(channel);

                        if (responsePacket.getType() == ACK) {
                            ackPackets.add(responsePacket);
                        }
                    }
                }
            }
             */

            logger.info("Client : Sending FIN");

            String msgFIN = "Request sent";
            Packet pFIN = new Packet.Builder()
                    .setType(FIN)
                    .setSequenceNumber(sequence)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msgFIN.getBytes())
                    .create();

            channel.send(p.toBuffer(), routerAddr);
            Packet responseFin = receive(channel);

            //TODO Handle response
        }
    }

    private long handShake(DatagramChannel channel, SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
        logger.info("Handshake: SYN request");

        String payload = "Connection request";
        Packet packet = new Packet.Builder()
                .setType(SYN)
                .setSequenceNumber(1L)
                .setPortNumber(serverAddr.getPort())
                .setPeerAddress(serverAddr.getAddress())
                .setPayload(payload.getBytes())
                .create();

        //Send Packet
        channel.send(packet.toBuffer(), routerAddr);

        //Receive Packet
        Packet responsePacket = receive(channel);

        if(responsePacket.getType() == SYN_ACK) {
            // send ACK
            Packet ackPacket = new Packet.Builder()
                    .setType(ACK)
                    .setSequenceNumber(responsePacket.getSequenceNumber() + 1)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(payload.getBytes())
                    .create();

            // send SYN
            channel.send(ackPacket.toBuffer(), routerAddr);

            logger.info("Handshake: Connection ACK, handshake complete");

            return ackPacket.getSequenceNumber();
        } else {

            logger.error("Handshake: Connection Error, handshake incomplete");
            return -1;
        }

    }

    private Packet receive(DatagramChannel channel) throws IOException { ;

        // Try to receive a packet within timeout.
        channel.configureBlocking(false);
        Selector selector = Selector.open();
        channel.register(selector, OP_READ);
        selector.select(5000);

        Set<SelectionKey> keys = selector.selectedKeys();
        if (keys.isEmpty()) {
            logger.error("No response after timeout");
            return null;
        }

        // We just want a single response.
        ByteBuffer buf = ByteBuffer.allocate(Packet.MAX_LEN);
        SocketAddress router = channel.receive(buf);
        buf.flip();
        Packet responsePacket = Packet.fromBuffer(buf);
        keys.clear();

        String payload = new String(responsePacket.getPayload(), StandardCharsets.UTF_8);

        logger.info("Packet: {}", responsePacket);
        logger.info("Router: {}", router);
        logger.info("Payload: {}\n", payload.trim());

        return responsePacket;
    }

    public byte[] getMessage() {
        return this.message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public void setMessage(String message) {
        logger.info("Message set: {}", message);

        this.message = message.getBytes();
    }

    public String getResponse() {
        return this.response;
    }
}

