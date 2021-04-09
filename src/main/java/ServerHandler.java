import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderNames.LOCATION;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;

public class ServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        if (!msg.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, msg, new DefaultFullHttpResponse(msg.protocolVersion(), BAD_REQUEST));
            return;
        }
        if (!GET.equals(msg.method())) {
            sendHttpResponse(ctx, msg, new DefaultFullHttpResponse(msg.protocolVersion(), FORBIDDEN));
            return;
        }
        if ("/".equals(msg.uri())) {
            var picFile = readPicture();
            if (picFile == null) {
                var content = Unpooled.copiedBuffer("获取图片失败", StandardCharsets.UTF_8);
                var response = new DefaultFullHttpResponse(msg.protocolVersion(), OK, content);
                response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
                HttpUtil.setContentLength(response, content.readableBytes());
                sendHttpResponse(ctx, msg, response);
            } else {
                var bytes = new FileInputStream(picFile).readAllBytes();
                var content = Unpooled.copiedBuffer(bytes);
                var response = new DefaultFullHttpResponse(msg.protocolVersion(), OK, content);
                var fileName = picFile.getName();
                var extension = fileName.substring(fileName.lastIndexOf(".") + 1);
                if (extension.equals("jpg") || extension.equals("jpeg")) {
                    response.headers().set(CONTENT_TYPE, "image/jpeg");
                } else {
                    response.headers().set(CONTENT_TYPE, "image/png");
                }
                HttpUtil.setContentLength(response, content.readableBytes());
                sendHttpResponse(ctx, msg, response);
            }
        } else {
            var response = new DefaultFullHttpResponse(msg.protocolVersion(), FOUND);
            response.headers().set(LOCATION, "/");
            sendHttpResponse(ctx, msg, response);
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        var keepAlive = HttpUtil.isKeepAlive(request) && response.status().code() == 200;
        HttpUtil.setKeepAlive(response, keepAlive);
        var future = ctx.writeAndFlush(response);
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static File readPicture() {
        var file = new File(Server.PIC_PATH);
        var pic = file.listFiles(f -> {
            var fileName = f.getName();
            var extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            var isPic = extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg");
            return f.isFile() && isPic;
        });
        if (pic == null || pic.length == 0) {
            return null;
        }
        var random = new Random(System.currentTimeMillis());
        var index = random.nextInt(pic.length);
        return pic[index];
    }
}
