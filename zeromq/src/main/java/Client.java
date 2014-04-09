import org.zeromq.ZMQ;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 3/24/14
 * Time: 1:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class Client {

        public static void main(String[] args) {
        ZMQ.Context context = ZMQ.context(1);

        //  Socket to talk to server
        System.out.println("Connecting to hello world server…");

        ZMQ.Socket requester = context.socket(ZMQ.REQ);
        requester.connect("tcp://localhost:5555");

        for (int requestNbr = 0; requestNbr != 10; requestNbr++) {

            String request = args.length>0? args[0]:"Hello";
            System.out.println("Sending  " + request + " " + requestNbr);
            requester.send(request.getBytes(), 0,ZMQ.NOBLOCK);
            requester.send(request.getBytes(), 0,ZMQ.NOBLOCK);

            byte[] reply = requester.recv(0);
            System.out.println("Received " + new String(reply) + " " + requestNbr);
        }
        requester.close();
        context.term();
    }
}
