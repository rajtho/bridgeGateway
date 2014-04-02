
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 3/29/14
 * Time: 8:28 AM
 * Interface contract which the app code to zeromQ-Like bridge follows. This contract will be used for the client code to interact with the WS gateway.
 * The output of the encoder will be further wrapped up into a WebSocket context and pushed over the wire for the Bridge Decoder to act and
 * extract the payload to be transfered over to zeroMQ.
 */
public interface BridgeEncoder {

    ByteBuffer encode(byte[] payload, int zmqSocketType, String appCommandIdentifier);

}
