import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import zmq.ZMQ;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 4/5/14
 * Time: 10:03 AM
 * To change this template use File | Settings | File Templates.
 */
public class ClientMessageDecoder extends MessageToMessageDecoder<WebSocketFrame>{

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame msg, List<Object> out) throws Exception {
        //extract clientMessage out of Binary WebSocketFrame

        ClientCommandExtractor.ClientMessage clientMessage = new ClientCommandExtractor.ClientMessage();
        clientMessage.setPayload("Hello World".getBytes("UTF-8"));
        clientMessage.setZmqCommand(ZMQ.ZMQ_PUSH);
        clientMessage.setServiceCommand("RefDataSearch:naga2054");

        out.add(clientMessage);
    }
}
