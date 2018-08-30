package com.zlb.nettystudy.websocket.entity;

/**
 * @author taoyuanming
 * Created on 2018/8/21
 * Description
 */
public enum KLinePushModeEnum {

    /**
     * 首次获取
     */
    REQ(1,"首次获取"),

    /**
     * 后续定时推送
     */
    SUB(2, "后续定时推送");


    private Integer code;

    private String name;

    KLinePushModeEnum(Integer code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }
}
