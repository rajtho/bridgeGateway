import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioTask;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 4/3/14
 * Time: 1:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class GatewayBridgeServer {
    EventLoopGroup bossGroup = new NioEventLoopGroup(1);
    EventLoopGroup inboundHandlingGroup = new NioEventLoopGroup(1);
//    EventLoopGroup outboundHandlingGroup = new NioEventLoopGroup(1);
    public void setupServer() {
        ServerBootstrap server = new ServerBootstrap().group(bossGroup,inboundHandlingGroup);
        server.channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<NioSocketChannel>() {
            @Override
            protected void initChannel(NioSocketChannel ch) throws Exception {
                GatewayHandler gatewayHandler = new GatewayHandler();
                ch.pipeline().addLast("http-codec", new HttpServerCodec())
                        .addLast("http-aggregator", new HttpObjectAggregator(65536))
                        .addLast("ws-handler",new WebSocketServerHandler())
                        .addLast("zeromq-request-handler", gatewayHandler) ;
//                        .addLast(outboundHandlingGroup,"zeromq-response-handler", new ZeroMQResponseHandler());
//                outboundHandlingGroup.next().register()

                //register the channel to an eventLoop so my inbound handlers are called
                inboundHandlingGroup.register(ch);

                //task that scourges through the channels zmq sockets and retrieves data to be relayed back to the client
                NioTask<SocketChannel> task = gatewayHandler.nioTask;

                //register the task with the eventQ so it is triggered whenever the channel is write ready.
                ((NioEventLoop)(inboundHandlingGroup.next())).register(ch,SelectionKey.OP_WRITE,task);
                //TODO:ROADBLOCK: now this ch channel is a NioSocketChannel and is not a Selectable Channel !!
            }
        });

    }
    public static void main(String[] args) {

    }
}
