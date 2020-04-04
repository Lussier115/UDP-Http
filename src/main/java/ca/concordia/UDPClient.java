package ca.concordia;

import ca.concordia.helper.ByteHelper;
import ca.concordia.network.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.nio.channels.SelectionKey.OP_READ;

public class UDPClient {

    private static final Logger logger = LoggerFactory.getLogger(UDPClient.class);
    private static final ByteHelper helper = new ByteHelper();

    private byte[] message;
    private Response response;

    private final static int DATA = 0;

    // Handshake
    private final static int SYN = 1; //initial sequence number. The sequence number of the actual first data byte and the acknowledged number in the corresponding ACK are then this sequence number plus 1
    private final static int SYN_ACK = 2;
    private static boolean handshakeIsSuccessful = false;

    // Packet
    private final static int ACK = 3;
    private final static int NACK = 4;
    private final static int FIN = 5; //Done sending all packets

    private HashMap<Long, Packet> packets;
    private ArrayList<Packet> ackPackets; //Ack Packets
    private HashMap<Long, byte[]> responsePayload;

    public void runClient(SocketAddress routerAddr, InetSocketAddress serverAddr) throws IOException {
        try (DatagramChannel channel = DatagramChannel.open()) {
            packets = new HashMap<Long, Packet>();
            ackPackets = new ArrayList<Packet>();

            long sequence = handShake(channel, routerAddr, serverAddr);

            if(handshakeIsSuccessful) {
                Packet p = new Packet.Builder()
                        .setType(DATA)
                        .setSequenceNumber(sequence)
                        .setPortNumber(serverAddr.getPort())
                        .setPeerAddress(serverAddr.getAddress())
                        .setPayload(this.message)
                        .create();

                //If packet is small enough
                if (this.getMessage().length <= Packet.MAX_PAYLOAD) {
                    packets.put(p.getSequenceNumber(), p);
                } else {
                    //If packet is too large, break it down and add to HashMap packets
                    this.breakDownPackets(p);
                }
            }

            // Send all Packets in HashMap
            for (Map.Entry<Long, Packet> packet : packets.entrySet()) {
                //Send Packet
                logger.info("Sending Packet: {}", packet.getValue().getSequenceNumber());
                channel.send(packet.getValue().toBuffer(), routerAddr);

                //Receive Packet
                Packet responsePacket = receive(channel);

                if (responsePacket.getType() == ACK) {
                    ackPackets.add(responsePacket);
                }
            }

            // Check if all packets have been acknowledged, if not resend specific packet.
            logger.info("Client : Check acknowledged packets");
            while (ackPackets.size() != packets.size()) {
                for (Map.Entry<Long, Packet> packet : packets.entrySet()) {

                    //Check if packet has been ack
                    if (!ackPackets.contains(packet.getValue())) {
                        //Send Packet
                        channel.send(packet.getValue().toBuffer(), routerAddr);

                        //Receive Packet
                        Packet receivedPacket = receive(channel);

                        if (receivedPacket.getType() == ACK) {
                            ackPackets.add(receivedPacket);
                        }
                    }
                }
            }

            //FIN packet
            logger.info("Client : Sending FIN");
            String msgFIN = "Request sent";
            Packet pFIN = new Packet.Builder()
                    .setType(FIN)
                    .setSequenceNumber(sequence)
                    .setPortNumber(serverAddr.getPort())
                    .setPeerAddress(serverAddr.getAddress())
                    .setPayload(msgFIN.getBytes())
                    .create();

            channel.send(pFIN.toBuffer(), routerAddr);
            Packet receivedFINPacket = receive(channel);
            ackPackets.add(receivedFINPacket);

            //Response packets
            responsePayload = new HashMap<Long, byte[]>();
            for(Packet packet: ackPackets){
                responsePayload.put(packet.getSequenceNumber(), packet.getPayload());
            }

            ByteBuffer buffer = helper.getMergeBytes(responsePayload);
            Response response = helper.getResponseObject(buffer.array());
            this.setResponse(response);

            logger.info("UDP Client finished");
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

        if (responsePacket.getType() == SYN_ACK) {
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

            handshakeIsSuccessful = true;
            return ackPacket.getSequenceNumber();
        } else {

            logger.error("Handshake: Connection Error, handshake incomplete");
            return -1;
        }

    }

    private Packet receive(DatagramChannel channel) throws IOException {

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
        channel.receive(buf);
        buf.flip();
        Packet responsePacket = Packet.fromBuffer(buf);
        logger.info("Packet: {}", responsePacket);
        logger.info("Router: {}", channel.receive(buf));
        String payload = new String(responsePacket.getPayload(), StandardCharsets.UTF_8);
        logger.info("Payload: {}",  payload);
        keys.clear();

        return responsePacket;
    }

    private void breakDownPackets(Packet packet) {
        long sequenceNumber = 0;

        if(packet.getPayload().length <= packet.MAX_PAYLOAD) {
            sequenceNumber = packet.getSequenceNumber();
            sequenceNumber ++;

            Packet nextPacket = new Packet(packet.getType(),
                    sequenceNumber,
                    packet.getPeerAddress(),
                    packet.getPeerPort(),
                    Arrays.copyOf(packet.getPayload(), packet.MAX_PAYLOAD));

            packets.put(nextPacket.getSequenceNumber(), nextPacket);
        }
        else {
            sequenceNumber = packet.getSequenceNumber();
            sequenceNumber ++;

            Packet nextPacket = new Packet(packet.getType(),
                    sequenceNumber,
                    packet.getPeerAddress(),
                    packet.getPeerPort(),
                    Arrays.copyOf(packet.getPayload(), packet.MAX_PAYLOAD));
            packets.put(nextPacket.getSequenceNumber(), nextPacket);

            sequenceNumber ++;
            Packet nextNextPacket = new Packet(packet.getType(),
                    sequenceNumber,
                    packet.getPeerAddress(),
                    packet.getPeerPort(),
                    Arrays.copyOfRange(packet.getPayload(),packet.MAX_PAYLOAD, packet.getPayload().length));

            //Recursive method
            breakDownPackets(nextNextPacket);
        }
    }

    public byte[] getMessage() {
        return this.message;
    }

    public void setMessage(byte[] message) {
        this.message = message;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Response getResponse() {
        return this.response;
    }
}


