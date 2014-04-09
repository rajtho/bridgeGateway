
import io.netty.util.internal.StringUtil;
import org.zeromq.ZFrame;
import org.zeromq.ZMQ;

import javax.print.DocFlavor;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 4/5/14
 * Time: 10:12 AM
 * Encapsulates the data of the client sockets and has methods to perform operations on behalf of the client .
 * Has to be operated by the same thread throughout its lifecycle. as the 0mq socket not threadSafe and mayuse threadlocal operations.
 */
public class ClientReverseProxy {

    HashMap<String, List<SocketInfo>> serviceSocketMap = new HashMap<String, List<SocketInfo>>(5);
    boolean isAuthenticated = false;
    String userId = null;
    long lastPingTime;

    //subscribe Socket - sync

    public boolean subscribe (String connectString, String... subscriptionFilters) {
        //lookup for existing socket , if found, add an extra filter, if not create a new one.
        return true;
    }

    //unsubscribe socket - sync

    public boolean unsubscribe(String connectString, String... subscriptionFilters) {
        return true;
    }

    // unscrubscribes all the sockets belonging to a service for ex: marketData, userNotifications etc.
    public boolean unsubscribe(String serviceName) {
        return true;
    }


    // async request socket - creates both dealer socket and a subscribe socket with a user filter

    public ZMQ.Socket createReqSocket(String connectString, String service) {
        // create a dealerSocket if already not present

        //create a subscriber socket on the topic <userid>.responses.*
        return null;
    }


    public boolean submitRequestAsync(String service, String connectString, byte[] payload) {
        // TODO:if args empty return false;

        ZMQ.Socket socket = getSocketByService(service);
        if(socket == null ) socket = createReqSocket(connectString, service);
        socket.send(payload);
        return true;
    }

    private ZMQ.Socket getSocketByService(String service) {
        return serviceSocketMap.get(service).get(0).socket;
    }

    public byte[][] getRelayableDataBackToClient(int maxLength) {
        //iterate through all the sockets its listenning and coalesce all the data until  (maxLength) to be returned out
        return null;
    }


    private static class SocketInfo {
        ZMQ.Socket socket;
        List<String> subscriptionFilters;
        String connectionString;

        void close() {
            if(subscriptionFilters !=null && socket.getType() == zmq.ZMQ.ZMQ_SUB ) {
                for(String filter : subscriptionFilters) {
                    try {
                        socket.unsubscribe(filter.getBytes("UTF-8"));
                    } catch (UnsupportedEncodingException e) {

                    }
                }
            }

            socket.close();
        }
    }


    //called when the channel/connection to the client is closing. This method does the cleanup of system resources
    public void destroy() {
        //unsubscribe the subscription channels
        //close all the 0mq sockets initiated by this client

    }

}
