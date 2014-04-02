import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import static junit.framework.Assert.*;

import org.apache.commons.codec.binary.Hex;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import zmq.ZMQ;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 4/1/14
 * Time: 3:02 PM
 *
 * This test shall setup a netty server on a tcp connection std port , register a protocol parser which asserts on the client message . A client shall be connected to verify the assert messages.
 */
public class ClientCommandExtractorTest {

    private ChannelFuture serverFuture;
    private EventLoopGroup serverLoopGroup = new NioEventLoopGroup() ;
    private final static int SERVER_PORT = 5050;

    String testPayload = "Hello World";
    String testIdentifier = "mktData:testClient@user.com";

    //client side
    BridgeEncoder clientEncoder = new ClientToGatewayEncoder();
    private class ClientHandler extends ChannelInboundHandlerAdapter{

        private Runnable asserter;

        public void setAsserter(Runnable asserter) {
            this.asserter = asserter;
        }

        @Override
        public void channelRead(ChannelHandlerContext context, Object message ) throws UnsupportedEncodingException {
            if(!(message instanceof ClientCommandExtractor.ClientMessage))
                context.fireChannelRead(message);
            ClientCommandExtractor.ClientMessage  clientMessage = (ClientCommandExtractor.ClientMessage)message;
            assertEquals("identifier extraction working",testIdentifier, clientMessage.getClientServiceCommand());
            assertEquals("payload extraction working", testPayload,  new String(clientMessage.getPayload(),"UTF-8"));

        }

    }

    @Before
    public void setup() throws InterruptedException {
        //setup a netty server with the extractor as the decoder and add a protocol handler to it

        ServerBootstrap server = new ServerBootstrap();
        server.group(new NioEventLoopGroup(),new NioEventLoopGroup());
        server.channel(NioServerSocketChannel.class);
        server.childHandler(new ChannelInitializer<io.netty.channel.socket.SocketChannel>() {

            @Override
            protected void initChannel(io.netty.channel.socket.SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new ClientCommandExtractor(),new ClientHandler());
            }
        });

        serverFuture = server.bind(SERVER_PORT).sync();
    }

    @Test
    public void basicClientTest() throws IOException, InterruptedException {
        Socket socket = new Socket(InetAddress.getLocalHost(),SERVER_PORT);

        try {
            assertTrue("Connected to Server", socket.isConnected());
            OutputStream outToServer = socket.getOutputStream();
            ByteBuffer buffer = clientEncoder.encode(testPayload.getBytes(Charset.forName("UTF-8")), ZMQ.ZMQ_SUB,testIdentifier);
            byte[] serverBytes = ((ByteBuffer)buffer.flip()).array();
            System.out.println(Hex.encodeHex(serverBytes));
            outToServer.write(serverBytes);
            outToServer.flush();
            Thread.sleep(1000);
        } finally {
            if(socket !=null)
                socket.close();
        }


    }

    @After
    public void tearDown() {
        // close the netty server
        try {
            serverFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } finally {
            serverLoopGroup.shutdownGracefully();
        }
    }

}
