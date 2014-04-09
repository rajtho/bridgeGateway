import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.nio.NioTask;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.zeromq.ZMQ;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import static zmq.ZMQ.*;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 4/5/14
 * Time: 12:01 PM
 * This class that maintains the state of the gateway and handles business functionality of ws to zmq mapping
 * This class is not designed to be threadsafe yet.
 */

public class GatewayHandler extends ChannelInboundHandlerAdapter{

    private final HashMap<String,String> serviceToConnectionMapping ;
    private final Map<Channel,ClientReverseProxy> channelToClientProxyMap; // niochannel to
    private final Map<String, Channel> userToChannelMap;      // userIdentifier toChannel Map, if guest their entry doesnt go here at all
    private static final Logger log = Logger.getLogger(GatewayHandler.class.getName());

    public GatewayHandler() {
        this.serviceToConnectionMapping = null;// get it from external resource
        channelToClientProxyMap = new HashMap<Channel, ClientReverseProxy>(1000);
        userToChannelMap = new HashMap<String, Channel>(1000);
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object object) {

        if(object instanceof PingWebSocketFrame) {
            handleClientPing(ctx,(PingWebSocketFrame)object);
            return;
        }
        if(!(object instanceof ClientCommandExtractor.ClientMessage)) {
            log.log(Level.WARNING, "unknown object thrown at me to handle, ignoring " + object.toString());
            ctx.fireChannelRead(object);
        }
        ClientCommandExtractor.ClientMessage msg = (ClientCommandExtractor.ClientMessage) object;
        ClientReverseProxy reverseProxy  = null;
        if(channelToClientProxyMap.get(ctx.channel()) == null) {
            reverseProxy = new ClientReverseProxy();
            channelToClientProxyMap.put(ctx.channel(),reverseProxy);
        }

        switch (msg.getClientZMQCommand()) {
            case zmq.ZMQ.ZMQ_SUB :
                //call reverseProxy's subscribe method passing the connection string and the subscriptionfilters by parsing the payload
                break;
            case zmq.ZMQ.ZMQ_UNSUBSCRIBE:
                //call reverseProxy's
                break;
            case zmq.ZMQ.ZMQ_REQ:
            case ZMQ_DEALER:
            case ZMQ_PUSH:
            case ZMQ_ROUTER:
                // all request types
                //call reverseProxy's createRequestmethod
                reverseProxy.submitRequestAsync(msg.getClientServiceCommand(), serviceToConnectionMapping.get(msg.getClientServiceCommand()), msg.payload);
                break;
        }
    }

    public void channelWritabilityChanged(ChannelHandlerContext ctx)

    private void handleClientPing(ChannelHandlerContext ctx, PingWebSocketFrame object) {
        channelToClientProxyMap.get(ctx.channel()).lastPingTime = System.currentTimeMillis(); //TODO: system call expensive, try to cache system time at regular intervals
    }

    NioTask<SocketChannel> nioTask = new NioTask<SocketChannel>() {
        public void channelReady(SocketChannel ch, SelectionKey key) throws Exception {
            ClientReverseProxy clientReverseProxy =  channelToClientProxyMap.get(ch);
            byte[][]data = clientReverseProxy.getRelayableDataBackToClient(ch.socket().getSendBufferSize());
            ch.write(Unpooled.copiedBuffer(data));  //TODO:Issue: ch is a socketChannel here but i need a NIOSocketChannel as i am inside netty.
        }

        public void channelUnregistered(SocketChannel ch, Throwable cause) throws Exception {

        }
    };
}
