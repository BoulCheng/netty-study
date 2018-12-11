package com.zlb.nettystudy.websocket.server;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.zlb.nettystudy.websocket.entity.ChannelModule;
import com.zlb.nettystudy.websocket.entity.ModuleEnum;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GlobalUserUtilPeer {
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalUserUtilPeer.class);

    /**
     * 保存全局的  连接上服务器的客户
     */
    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static ConcurrentHashMap<Channel, Map<Integer, ChannelModule>> channelMap = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<Long, List<Channel>> userMap = new ConcurrentHashMap<>();

    /**
     * 登陆状态的用户已提示过保证金不足的记录
     */
    public static ConcurrentHashMap<Long, String> marginWarnedMap = new ConcurrentHashMap<>();

    public static void addUser(Long userId, Channel channel) {
        synchronized (userMap) {
            if (!userMap.containsKey(userId)) {
                List<Channel> channelList = new LinkedList<>();
                channelList.add(channel);
                userMap.put(userId, channelList);
            } else {
                List<Channel> channelList = userMap.get(userId);
                if (channelList != null) {
                    if (!channelList.contains(channel)) {
                        channelList.add(channel);
                    }
                } else {
                    channelList = new LinkedList<>();
                    channelList.add(channel);
                    userMap.put(userId, channelList);
                }
            }
        }
    }

    public static void removeUser(Channel channel) {
        synchronized (userMap) {
            Map<Integer, ChannelModule> channelModuleMap = channelMap.get(channel);
            ChannelModule channelModule = channelModuleMap.get(ModuleEnum.USER_ID.getCode());
            Long userId = channelModule.getUserId();
            List<Channel> channelList = userMap.get(userId);
            channelList.remove(channel);
        }
    }

    public static List<Channel> getChannelsByUserId(Long userId) {
        return userMap.get(userId);
    }

    public static Long getUserIdByChannel(Channel channel) {
        Map<Integer, ChannelModule> channelModuleMap = channelMap.get(channel);
        if (channelModuleMap != null) {
            ChannelModule channelModule = channelModuleMap.get(ModuleEnum.USER_ID.getCode());
            if (channelModule != null) {
                Long userId = channelModule.getUserId();
                if (userId != null) {
                    return userId;
                } else {
                    LOGGER.error("getUserIdByChannel error, channelModule not exist userId, channel :{}", JSON.toJSONString(channel));
                }
            } else {
                LOGGER.error("getUserIdByChannel error, channelModuleMap not exist userId channelModule, channel:{}", JSON.toJSONString(channel));
            }
        } else {
            LOGGER.error("getUserIdByChannel error, channelMap not exist channel:{}", JSON.toJSONString(channel));
        }
        return 0L;
    }

    public static String getSessionIdByChannel(Channel channel) {
        Map<Integer, ChannelModule> channelModuleMap = channelMap.get(channel);
        if (channelModuleMap != null) {
            ChannelModule channelModule = channelModuleMap.get(ModuleEnum.SESSION_ID.getCode());
            if (channelModule != null) {
                String sessionId = channelModule.getSessionId();
                if (StringUtils.isNotBlank(sessionId)) {
                    return sessionId;
                } else {
                    LOGGER.error("getSessionIdByChannel error, channelModule not exist userId, channel:{}", JSON.toJSONString(channel));
                }
            } else {
                LOGGER.error("getSessionIdByChannel error, channelModuleMap not exist sessionId channelModule, channel:{}", JSON.toJSONString(channel));
            }
        } else {
            LOGGER.error("getSessionIdByChannel error, channelMap not exist channel:{}", JSON.toJSONString(channel));
        }
        return "";
    }

    public static ChannelModule getChannelModule(Channel channel, ModuleEnum type) {
        if (null == type) {
            return null;
        }
        if (null == channel) {
            return null;
        }
        Map<Integer, ChannelModule> channelModuleMap = channelMap.get(channel);
        if (null == channelModuleMap) {
            return null;
        }

        return channelModuleMap.get(type.getCode());
    }

    /**
     * 用户登出后移除用户已提示过保证金不足的记录
     * @param channel
     */
    public static void removeMarginWarnned(Channel channel) {
        Long userId = getUserIdByChannel(channel);
        if (null != userId) {
            marginWarnedMap.remove(userId);
        }
    }
}
