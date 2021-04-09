import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class ServerHandlerInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        var pipeline = ch.pipeline();
        pipeline.addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(65536))
                .addLast(new ServerHandler());
    }
}
