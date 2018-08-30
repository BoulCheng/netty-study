package com.zlb.nettystudy.websocket.entity;

import java.io.Serializable;

/**
 * @author Tao Yuanming
 * Created on 2018/8/29
 * Description
 */
public class ChannelModule implements Serializable {
    private static final long serialVersionUID = -4592362814003403028L;

    /**
     * 请求的模块类型
     */
    private Integer reqType;
    /**
     * 1 usdt 2 合约， 非空  （K线推送复用）
     */
    private int type;
    /**
     * usdt 或 contract ID， 可空
     */
    private Integer id;
    /**
     * 对应的其他参数
     */
    private String param;

    private Long userId;

    private String sessionId;

    public ChannelModule() {
    }

    public ChannelModule(Long userId) {
        this.userId = userId;
    }

    public ChannelModule(String sessionId) {
        this.sessionId = sessionId;
    }

    public Integer getReqType() {
        return reqType;
    }

    public void setReqType(Integer reqType) {
        this.reqType = reqType;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
