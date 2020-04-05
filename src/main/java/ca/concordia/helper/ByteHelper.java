package ca.concordia.helper;

import ca.concordia.UDPClient;
import ca.concordia.network.request.Request;
import ca.concordia.network.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

public class ByteHelper {
    private static final Logger logger = LoggerFactory.getLogger(ByteHelper.class);

    public Request getRequestObject(byte[] array){
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(array);
            ObjectInput in = new ObjectInputStream(bis);
            return (Request) in.readObject();
        } catch (Exception e3){
            logger.error(e3.toString());
        }

        return null;
    }

    public Response getResponseObject(byte[] array){
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(array);
            ObjectInput in = new ObjectInputStream(bis);
            return (Response) in.readObject();
        } catch (Exception e3){
            logger.error(e3.toString());
        }

        return null;
    }

    public byte[] getByteArray(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(obj);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bos.toByteArray();
    }

    public ByteBuffer getMergeBytes(HashMap<Long,byte[]> map) {


        SortedSet<Long> keys = new TreeSet<>(map.keySet()); //Sort by Sequence Number

        //Check total byte length of packets
        int byteLength = 0;
        for (Long key : keys) {
            byteLength += map.get(key).length;
        }

        byte[] requestBytes = new byte[byteLength];
        ByteBuffer buff = ByteBuffer.wrap(requestBytes);

        //Append all byte[] together to form Request Object
        for (Long key : keys) {
            buff.put(map.get(key));
        }

        return buff;
    }
}
