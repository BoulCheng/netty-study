package com.zlb.nettystudy.websocket.server;

import com.alibaba.fastjson.JSON;
import com.zlb.nettystudy.websocket.entity.ChannelModule;
import com.zlb.nettystudy.websocket.entity.KLinePushModeEnum;
import com.zlb.nettystudy.websocket.entity.ModuleEnum;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FotaChannelHandlerPeer extends SimpleChannelInboundHandler<Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(FotaChannelHandlerPeer.class);

    private static final String URI = "websocket";

    private WebSocketServerHandshaker handshaker ;
//
//    private static KLineManager kLineManager = BeanUtil.getBean(KLineManager.class);
//    private static NotifyInfoManager notifyInfoManager = BeanUtil.getBean(NotifyInfoManager.class);

    public static void sendMessageToAll(String msg) {
        for (Channel channel : GlobalUserUtilPeer.channels) {
            channel.writeAndFlush(new TextWebSocketFrame(msg));
        }
    }

    public static void sendMessageToOne(String msg, Channel channel) {
        channel.writeAndFlush(new TextWebSocketFrame(msg));
    }

    /**
     * 连接上服务器
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【handlerAdded】====>"+ctx.channel().id());
    }

    /**
     * 断开连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【handlerRemoved】====>"+ctx.channel().id());
    }

    /**
     * 连接异常   需要关闭相关资源
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("【系统异常】======>"+cause.toString());
        LOGGER.error("【系统异常】: ",cause);
        ctx.close();
        ctx.channel().close();
    }

    /**
     * 活跃的通道  也可以当作用户连接上客户端进行使用
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【channelActive】=====>"+ctx.channel());
    }

    /**
     * 不活跃的通道  就说明用户失去连接
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LOGGER.info("【channelInactive】=====>"+ctx.channel());

    }

    /**
     * 这里只要完成 flush
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 这里是保持服务器与客户端长连接  进行心跳检测 避免连接断开
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            PingWebSocketFrame ping = new PingWebSocketFrame();
            switch (stateEvent.state()){
                //读空闲（服务器端）
                case READER_IDLE:
                    LOGGER.info("【"+ctx.channel().remoteAddress()+"】读空闲（服务器端）");
                    ctx.writeAndFlush(ping);
                    break;
                //写空闲（客户端）
                case WRITER_IDLE:
                    LOGGER.info("【"+ctx.channel().remoteAddress()+"】写空闲（客户端）");
                    ctx.writeAndFlush(ping);
                    break;
                case ALL_IDLE:
                    LOGGER.info("【"+ctx.channel().remoteAddress()+"】读写空闲");
                    break;
                default:
                    LOGGER.error("【"+ctx.channel().remoteAddress()+"】IdleStateEvent Unknown");
                    break;
            }
        }
    }

    /**
     * 收发消息处理
     * @param ctx
     * @param msg
     * @throws Exception
     */
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            doHandlerHttpRequest(ctx,(HttpRequest) msg);
        }else if(msg instanceof WebSocketFrame){
            doHandlerWebSocketFrame(ctx,(WebSocketFrame) msg);
        }
    }

    /**
     * websocket消息处理
     * @param ctx
     * @param msg
     */
    private void doHandlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame msg) {
        //判断msg 是哪一种类型  分别做出不同的反应
        if(msg instanceof CloseWebSocketFrame){
            LOGGER.info("【关闭】");
            ReferenceCountUtil.retain(msg);
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) msg);
            return ;
        }
        if(msg instanceof PingWebSocketFrame){
            LOGGER.info("【ping】");
//            PongWebSocketFrame pong = new PongWebSocketFrame(msg.content().retain());
//            ctx.channel().writeAndFlush(pong);
            return ;
        }
        if(msg instanceof PongWebSocketFrame){
            LOGGER.info("【pong】");
//            PingWebSocketFrame ping = new PingWebSocketFrame(msg.content().retain());
//            ctx.channel().writeAndFlush(ping);
            return ;
        }
        if(!(msg instanceof TextWebSocketFrame)){
            LOGGER.error("【不支持二进制】");
            throw new UnsupportedOperationException("不支持二进制");
        }

        LOGGER.info("收到客户端请求：{}, localAddress:{}" + ((TextWebSocketFrame) msg).text(), ctx.channel().localAddress());
        ctx.channel().writeAndFlush(new TextWebSocketFrame(((TextWebSocketFrame) msg).text()));
        //ctx.channel().writeAndFlush(msg); //会发生 io.netty.util.IllegalReferenceCountException: refCnt: 0, decrement: 1
    }

    /**
     * wetsocket第一次连接握手
     * @param ctx
     * @param msg
     */
    private void doHandlerHttpRequest(ChannelHandlerContext ctx, HttpRequest msg) {
        // http 解码失败
        if(!msg.getDecoderResult().isSuccess() || (!"websocket".equalsIgnoreCase(msg.headers().get("Upgrade")))){
            LOGGER.error("webSocket doHandlerHttpRequest failed");
            sendHttpResponse(ctx, (FullHttpRequest) msg,new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
        }

        //可以通过url获取其他参数
        WebSocketServerHandshakerFactory factory = new WebSocketServerHandshakerFactory(
                "ws://"+msg.headers().get("Host")+"/"+URI+"",null,false
        );
        handshaker = factory.newHandshaker(msg);
        if(handshaker == null){
            WebSocketServerHandshakerFactory.sendUnsupportedWebSocketVersionResponse(ctx.channel());
        }
        //进行连接
        handshaker.handshake(ctx.channel(), (FullHttpRequest) msg);
        //可以做其他处理
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }

        // 如果是非Keep-Alive，关闭连接
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
            LOGGER.error("asdfasd close ");
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        if(msg instanceof HttpRequest){
            doHandlerHttpRequest(ctx,(HttpRequest) msg);
        }else if(msg instanceof WebSocketFrame){
            doHandlerWebSocketFrame(ctx,(WebSocketFrame) msg);
        }
    }
}
