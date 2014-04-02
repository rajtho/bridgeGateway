import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 3/31/14
 * Time: 3:14 PM
 * Used to decode the client zmq message to extract the serviceType command masked into the identifier field of zmq message
 * <p/>
 * Try to use zmq code if possible to extract the frames and message parts
 */
public class ClientCommandExtractor extends ReplayingDecoder<ClientCommandExtractor.State> {

    public static enum State {SIGNATURE_SEARCHING, PARSE_GREETING, PARSING_FRAME}

    private ClientMessage clientMessage = new ClientMessage();
    private ZMQSignatureDetector signatureDetector = new ZMQSignatureDetector();
    private Charset charset = Charset.forName("UTF-8");

    public ClientCommandExtractor() {
        super(State.SIGNATURE_SEARCHING);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // keep looking for the zmq signature, if not found, skip the byte
        // once found, extract from the greeting - zmq command and identifier
        // extract the payload making sure that its a final frame , if not log it and skip it and only extract the final frame.

        switch (state()) {
            case SIGNATURE_SEARCHING:
                // we assume that the signature is followed by atleast 10 bytes
                if (in.forEachByte(signatureDetector) > -1) {
                    // we found the signature,// skip by signature.length
                    in.skipBytes(signatureDetector.signature.length);
                    checkpoint(State.PARSE_GREETING);
                    //now read revision
                }
            case PARSE_GREETING:
                // read the version
                in.readByte(); // dont care for revision
                clientMessage.zmqCommand = in.readUnsignedByte();
                //flag length byte identifier
                //ignore the flag
                in.readByte();
                int lengthToRead = in.readUnsignedByte();
                clientMessage.serviceCommand = in.readBytes(lengthToRead).toString(charset);
//                        in.toString(in.readerIndex(), lengthToRead, charset);
                checkpoint(State.PARSING_FRAME);
            case PARSING_FRAME:
                //max 10 frames supported
                int f = 0;
                while (++f<=10) {
                    byte flag = in.readByte();

                    if ((flag & 0x01) == 0x01) {
                        // theres a more flag - skip the frame
                        parseFrame(in, flag, true);
                        checkpoint(State.PARSING_FRAME);
                    } else {
                        ByteBuf payloadBuf = parseFrame(in, flag, false);
                        try {
                            if (payloadBuf == null) {
                                //TODO: raise/log error
                                System.out.println("ERROR parseing a frame - ditching and finding new signature for fresh message");
                                //search for next message
                                state(State.SIGNATURE_SEARCHING);
                                return;
                            } else {
                                clientMessage.payload = payloadBuf.array();

                                //message parsing completed.
                                out.add(clientMessage);
                                clientMessage = new ClientMessage();
                                state(State.SIGNATURE_SEARCHING);
                                break;//end of parsing a message
                            }
                        } finally {
                            if(payloadBuf !=null ) {
                                payloadBuf.release();
                            }
                        }

                    }
                }


        }
    }

    private ByteBuf parseFrame(ByteBuf in, byte flag, boolean skip) {
        //skip more frames and process only final frame
        //flag long/short payload
        ByteBuf buf = null;

        if ((flag & 0x02) == 0x00) {
            // Bit 1 - is not turned on - length field is a single byte
            if (skip) {
                in.skipBytes(in.readUnsignedByte());
            } else {
                buf = in.readBytes(in.readUnsignedByte());
            }
        } else {
            // length field is an unsigned long
            long length = in.readLong();
            if (length > Integer.MAX_VALUE) {
                for (long l = 0; l < length; ) {
                    int bytesToSkip = (int) Math.min(length - l, Integer.MAX_VALUE);
                    in.skipBytes(bytesToSkip);
                    l += bytesToSkip;

                }
            } else {
                buf = in.readBytes((int) length);
            }
        }
        if (skip) return null;
        else return buf;


    }

    private class ZMQSignatureDetector implements ByteBufProcessor {
        int matchedBytes = 0; //maintains the state of matched bytes
        byte[] signature = getSignature();


        public boolean process(byte value) throws Exception {
            if (value == signature[0]) {
                // if match already started and is not the length of signature, reset the match once again
                // if the match did not start, start the match
                if (matchedBytes > 0) {
                    matchedBytes = 0;
                }
                matchedBytes++;
                checkpoint(State.SIGNATURE_SEARCHING);
                return true;
            } else {
                // if the match already started , match the new byte with the signature. if success, increment the matchBytes and check for the length to be 10. if failure, reset the matchBytes and the checkpoint
                if (matchedBytes > 0) {
                    if (value == signature[matchedBytes]) {
                        ++matchedBytes;
                        if (matchedBytes == signature.length) {
                            return false;//signature matched
                        } else {
                            return true;
                        }
                    } else {
                        matchedBytes = 0;
                        checkpoint(State.SIGNATURE_SEARCHING);
                        return true;
                    }
                } else {
                    // if the match hasnt started yet, reset the checkpoint.
                    checkpoint(State.SIGNATURE_SEARCHING);
                    return true;
                }
            }
        }

        private byte[] getSignature() {
            byte[] signature = new byte[10];
            signature[0] = (byte) 0xff;
            for (int i = 1; i < 9; i++) {
                signature[i] = (byte) 0x00;
            }
            signature[9] = 0x7f;
            return signature;
        }

    }

    public static class ClientMessage implements BridgeDecoder.ClientCommandInterpreter {
        int zmqCommand;
        String serviceCommand;
        byte[] payload;
        List<String> subscriptionFilters;

        public int getClientZMQCommand() {
            return zmqCommand;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public String getClientServiceCommand() {
            return serviceCommand;  //To change body of implemented methods use File | Settings | File Templates.
        }

        public byte[] getPayload() {
            return payload;
        }

        public List<String> getSubscriptionFilters() {
            return subscriptionFilters;
        }

        public void setZmqCommand(int zmqCommand) {
            this.zmqCommand = zmqCommand;
        }

        public void setServiceCommand(String serviceCommand) {
            this.serviceCommand = serviceCommand;
        }

        public void setPayload(byte[] payload) {
            this.payload = payload;
        }

        public void setSubscriptionFilters(List<String> subscriptionFilters) {
            this.subscriptionFilters = subscriptionFilters;
        }
    }

}

