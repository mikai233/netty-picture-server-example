import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class Server {
    final static String PIC_PATH = "D:\\Project\\IdeaProjects\\lolicon\\images";
    final static int PORT = 8989;

    public static void main(String[] args) {
        var boosGroup = new NioEventLoopGroup();
        var workGroup = new NioEventLoopGroup();
        var serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(boosGroup, workGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ServerHandlerInitializer());
        try {
            var channel = serverBootstrap.bind(PORT).sync().channel();
            channel.closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            boosGroup.shutdownGracefully();
            workGroup.shutdownGracefully();
        }
    }
}
