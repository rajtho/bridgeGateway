import io.netty.channel.nio.NioEventLoop;
import org.zeromq.ZMQ;

import java.awt.datatransfer.StringSelection;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 3/24/14
 * Time: 12:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Pub {
    public static void main(String[] args) throws  Exception {
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket responder = context.socket(ZMQ.REP);
        responder.bind("tcp://*:5555");
        while(!Thread.currentThread().isInterrupted()) {
            byte[] request = responder.recv(0);
            String s = null;
            String resp = null;
            System.out.println(s = new String(request));
            if(s.equals("Hello")) {
                resp = "World";
            } else if( s.equals("Hola")) {
                resp = "Mucho Gusto!";
            } else {
                resp = "Sorry!, Can you repeate again?";
            }

            Thread.sleep(1000);

            responder.send(resp);

        }


        responder.close();
        context.term();
    }
}


