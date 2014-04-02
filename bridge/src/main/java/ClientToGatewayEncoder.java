import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 3/31/14
 * Time: 1:12 PM
 *
 * Presently supports encoding only a single zmq Frame. appCommandIdentifier cannot be more than 256 characters and should be US-ASCII charset.
 */
public class ClientToGatewayEncoder implements  BridgeEncoder{
    public ByteBuffer encode(byte[] payload, int zmqSocketType, String appCommandIdentifier) {

        //buffer size calculate = signature + revision + socketType + (identity = flag + octet + identifier)
        int bufferSize =  10 + 1+ 1 +1+ 1+ appCommandIdentifier.length() + 9 + payload.length;

        ByteBuffer buffer = ByteBuffer.allocate(bufferSize);
        insertGreeting(buffer, zmqSocketType, appCommandIdentifier);
        insertFinalFrame(buffer,  payload);
        return buffer;

    }

    private static byte[] getSignature() {
        byte[] signature = new byte[10];
        signature[0] = (byte)0xff;
        for(int i=1; i<9;i++){
            signature[i] = (byte)0x00;
        }
        signature[9] = 0x7f;
        return signature;
    }

    private static byte getRevision() {
        return 0x01;
    }

    private ByteBuffer insertGreeting(ByteBuffer buffer,int zmqSocketType,  String appCommandIdentifier) {
        buffer.put(getSignature()).put(getRevision());

        byte[] identifierBytes = appCommandIdentifier.getBytes(Charset.forName("US-ASCII"));
        //socket flag length(1byte) identifier
        buffer.put((byte)zmqSocketType).put((byte)0x00).put((byte)identifierBytes.length).put(identifierBytes);

        return buffer;

    }

    private ByteBuffer insertFinalFrame(ByteBuffer buffer, byte[] payload) {
        //frame - flag +  long unsigned int + payload

        byte flag = 0x02; //send long payload always and its a final frame

        buffer.put(flag).putLong(payload.length).put(payload);
        return buffer;
    }






}
