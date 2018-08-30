package com.zlb.nettystudy.websocket.util;

import com.corundumstudio.socketio.SocketIOClient;
import com.zlb.nettystudy.websocket.server.GlobalUserUtil;
import io.netty.channel.Channel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Tao Yuanming
 * Created on 2018/8/29
 * Description
 */
public class WsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(WsUtils.class);

//    private static RedisCache redisCache = BeanUtil.getBean(RedisCache.class);

    public static Boolean checkLogin(SocketIOClient client) {
        Long userId = GlobalUserUtil.getUserIdByClient(client);
        String sessionId = client.getSessionId().toString();
        if (userId != null && StringUtils.isNotBlank(sessionId)) {
//            return redisCache.checkExist(userId, sessionId);
        } else {
            LOGGER.error("webSocket checkLogin error! SocketIOClient:{}, userId:{}, sessionId:{}", client, userId, sessionId);
        }
        return false;
    }
}
