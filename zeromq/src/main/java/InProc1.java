import org.zeromq.ZMQ;
import org.zeromq.EmbeddedLibraryTools;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 3/25/14
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class InProc1 {

    private final ZMQ.Context context = ZMQ.context(1);

    private class Chatter extends Thread {
        public void run() {
            ZMQ.Socket requester = context.socket(ZMQ.REQ);
            try {
                requester.connect("inproc://localChatRoom");
                requester.send("Hello");

                String response = requester.recvStr();
                System.out.println(currentThread().getName() + ": " + response);
            } finally {
                requester.close();
            }

        }
    }

    public void startChitChat() {

        ZMQ.Socket moderator = context.socket(ZMQ.REP);

        for(int i=0; i<5; i++) {
            new Chatter().start();
        }

        try {
            moderator.bind("inproc://localChatRoom");
            while(!Thread.currentThread().isInterrupted()) {
                String s = moderator.recvStr();
                moderator.send("Welcome to the ChatRoom");
            }

        } finally {
            moderator.close();
            context.term();
        }


    }

    public static void main(String[] args) {
        new InProc1().startChitChat();
    }
}
