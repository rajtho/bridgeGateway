import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 3/31/14
 * Time: 8:29 AM
 * Interface Contract used by the bridge to decode the incoming client messages, strip the appCommand part and relay the rest into zeroMq
 */
public interface BridgeDecoder {

    public ClientCommandInterpreter decode(ByteBuf buffer);

    public static interface ClientCommandInterpreter {
        public int getClientZMQCommand(); // ZMQ : PUB, SUB etc

        public String getClientServiceCommand(); // passes the string in the identity tag of zmq protocol
    }


}
