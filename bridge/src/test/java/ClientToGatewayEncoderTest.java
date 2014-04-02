import org.apache.commons.codec.binary.Hex;
import org.junit.Before;
import org.junit.Test;
import zmq.ZMQ;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 3/31/14
 * Time: 2:43 PM
 * To change this template use File | Settings | File Templates.
 */
public class ClientToGatewayEncoderTest {
    ClientToGatewayEncoder encoder ;
    String testPayload = "Hello World";
    String testIdentifier = "mktData:testClient@user.com";

    @Before
    public void setUp() {
        encoder = new ClientToGatewayEncoder();

    }


    @Test
    public void testEncodingBasic() throws Exception{
        String s = "Hello World";
        ByteBuffer buffer = encoder.encode(s.getBytes("US-ASCII"), ZMQ.ZMQ_SUB,"initialHandShake:clien101" );
        System.out.println(Hex.encodeHexString(buffer.array()));
    }

    @Test
    public void testEncoding2() throws Exception {
        ByteBuffer buffer = encoder.encode(testPayload.getBytes(Charset.forName("UTF-8")), ZMQ.ZMQ_SUB,testIdentifier);
        byte[] serverBytes = ((ByteBuffer)buffer.flip()).array();

        for(int i=0;i < serverBytes.length; i++) {
            System.out.println("byte[" + i + "]" + " = " + String.format("%02X",serverBytes[i]) + " " + serverBytes[i]);
        }

    }



}
