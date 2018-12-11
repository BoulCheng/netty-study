package com.zlb.nettystudy.websocket.server;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.zlb.nettystudy.websocket.entity.ChannelModule;
import com.zlb.nettystudy.websocket.entity.KLinePushModeEnum;
import com.zlb.nettystudy.websocket.entity.ModuleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Tao Yuanming
 * Created on 2018/8/30
 * Description  SocketIo
 */
@Service
public class SocketIoServer {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
//
//    private static KLineManager kLineManager = BeanUtil.getBean(KLineManager.class);
//    private static NotifyInfoManager notifyInfoManager = BeanUtil.getBean(NotifyInfoManager.class);

    private static SocketIOServer server;
    private static final String eventType = "fota";

    public SocketIoServer() {
        ThreadFactory nameFactory = new SocketIoThreadFactory();
        ThreadPoolExecutor singleThreadPool = new ThreadPoolExecutor(1, 1,
                0L, TimeUnit.MILLISECONDS, new LinkedBlockingDeque<>(16), nameFactory);
        singleThreadPool.execute(() -> {
            try {
                startServer();
                logger.info("SocketIOServer启动成功!");
            } catch (Exception e) {
                logger.error("SocketIOServer启动失败!", e);
            }

        });
    }

    static class SocketIoThreadFactory implements ThreadFactory {
        private static AtomicLong id = new AtomicLong(0);
        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "SocketIoServer-thread-pool-" + id.addAndGet(1));
        }
    }


    public void startServer() {
        Configuration config = new Configuration();
        // 服务器主机ip
        config.setHostname("localhost");
        // 端口
        config.setPort(9092);
        config.setMaxFramePayloadLength(1024 * 1024);
        config.setMaxHttpContentLength(1024 * 1024);
        server = new SocketIOServer(config);
        /**
         * 接受客户端消息
         */
        server.addEventListener(eventType, String.class, (SocketIOClient client, String data, AckRequest ackRequest) -> {
            // 客户端推送advert_info事件时，onData接受数据，这里是string类型的json数据，还可以为Byte[],object其他类型
            String sa = client.getRemoteAddress().toString();
            String clientIp = sa.substring(1, sa.indexOf(":"));

            System.out.println(clientIp + "：客户端：************" + data);

            ChannelModule channelModule = null;
            try {
                channelModule = JSON.parseObject(data, ChannelModule.class);
            } catch (Exception e) {
                logger.error("onData parseObject exception, msg:{}", data);
            }

            Long userId = channelModule.getUserId();
            if (userId != null) {
                GlobalUserUtil.addUserClient(userId, client);
            }
            Integer reqType = channelModule.getReqType();
            /**
             * K线ws直接响应首次请求
             */
            if (((Integer) ModuleEnum.KLINE.getCode()).equals(reqType) && KLinePushModeEnum.REQ.getCode().equals(channelModule.getType())) {
                //移除切换前的K线推送
                Map<Integer, ChannelModule> channelMap = GlobalUserUtil.clientMap.get(client);
                if (null != channelMap) {
                    channelMap.remove(reqType);
                }

//                    synchronized (ctx.channel()) {
//                        Map<Integer, ChannelModule> channelMap = GlobalUserUtil.channelMap.get(ctx.channel());
//                        if (null != channelMap) {
//                            channelMap.remove(reqType);
//                            GlobalUserUtil.channelMap.put(ctx.channel(), channelMap);
//                        }
//                    }

                try {
//                    SocketIoServer.sendMessageToOne(kLineManager.getWsKlineData(channelModule), client);
                } catch (Exception e) {
                    logger.error("pushKlineReqMessage message error", e);
                }
                return;
            }

            /**
             * 历史K线ws推送
             */
            if (((Integer) ModuleEnum.HISTORY_KLINE.getCode()).equals(reqType)) {
//                SocketIoServer.sendMessageToOne(kLineManager.getWsKlineData(channelModule), client);
                return;
            }

            /**
             * 保证金提醒和其他提醒登陆就连接
             */
            if (((Integer) ModuleEnum.MARGIN_NOTICE.getCode()).equals(reqType) && userId != null) {
//                notifyInfoManager.pushLoginNotifyInfo(userId);
            }

            Map<Integer, ChannelModule> channelModuleMap = GlobalUserUtil.clientMap.get(client);
            channelModuleMap.put(channelModule.getReqType(), channelModule);
            if (userId != null && !channelModuleMap.containsKey(ModuleEnum.USER_ID.getCode())) {
                channelModuleMap.put(ModuleEnum.USER_ID.getCode(), new ChannelModule(userId));
            }
        });

        /**
         * 添加客户端连接事件
         */
        server.addConnectListener((SocketIOClient client) -> {
            String sa = client.getRemoteAddress().toString();
            String clientIp = sa.substring(1, sa.indexOf(":"));
            System.out.println(clientIp + "-------------------------" + "客户端已连接");
            // Map params = client.getHandshakeData().getUrlParams();
            // 给客户端发送消息
            client.sendEvent(eventType, clientIp + "客户端你好，我是服务端，有什么能帮助你的？");
            Boolean bo = GlobalUserUtil.clientMap.containsKey(client);
            if (bo) {
                System.out.println("yes!");
            } else {
                System.out.println("no");
                GlobalUserUtil.clientMap.put(client, new ConcurrentHashMap<>());
            }
        });
        /**
         * 添加客户端断开连接事件
         */
        server.addDisconnectListener((SocketIOClient client) -> {
            String sa = client.getRemoteAddress().toString();
            String clientIp = sa.substring(1, sa.indexOf(":"));
            GlobalUserUtil.clientMap.remove(client);
            GlobalUserUtil.removeUserClient(client);
            GlobalUserUtil.removeMarginWarnned(client);
            System.out.println(clientIp + "-------------------------" + "客户端已断开连接");
            // 给客户端发送消息
            client.sendEvent(eventType, clientIp + "客户端你好，我是服务端，期待下次和你见面");
        });
        server.start();
    }

    public void stopServer() {
        if (server != null) {
            server.stop();
            server = null;
        }
    }

    /**
     * 发送消息
     *
     * @param client
     * @param message
     */
    public static void sendMessageToOne(String message, SocketIOClient client) {
        client.sendEvent(eventType, message);
    }


    /**
     *
     * @param eventType
     * @param message
     */
    public void sendMessageToAllClient(String eventType, String message) {
        Collection<SocketIOClient> clients = server.getAllClients();
        for (SocketIOClient client : clients) {
            client.sendEvent(eventType, message);
        }
    }

    public static SocketIOServer getServer() {
        return server;
    }

}