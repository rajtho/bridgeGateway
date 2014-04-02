import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.Charset;

/**
 * Created by IntelliJ IDEA.
 * User: Raj
 * Date: 4/1/14
 * Time: 2:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class ByteBufExperiment {
    public static void main(String[] args) {

        byte[] baseArray = "Hello World".getBytes(Charset.forName("UTF-8"));

        ByteBuf buf = Unpooled.buffer(baseArray.length);
        buf.writeBytes(baseArray);

        byte[] out = buf.readBytes(5).array();

        System.out.println(out == baseArray);
    }
}
