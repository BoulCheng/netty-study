package com.zlb.nettystudy.websocket.entity;

import lombok.Getter;
import lombok.Setter;

public enum ModuleEnum {

    /**
     * sessionId
     */
    SESSION_ID("sessionId", -1),

    /**
     * 用户userId
     */
    USER_ID("userId", 0),

    /**
     * 深度
     */
    RNTRUST_DEPTH("depth", 1),

    /**
     * 成交数据
     */
    MATCH_LIST("match", 2),

    /**
     * 跑马灯
     */
    HORSE_LAMP("scrolling", 3),

    /**
     * USDT/合约信息
     */
    SYMBOL_INFO("goods", 4),

    /**
     * 委托
     */
    ORDER("order", 5),

    /**
     * 资产
     */
    ASSET("asset", 6),

    /**
     * K线ws推送
     */
    KLINE("kline", 7),

    /**
     * 历史K线ws推送
     */
    HISTORY_KLINE("hkline", 8),

    /**
     * 用户消息通知推送
     */
    NOTIFY_INFO("notice", 9),

    /**
     * 卡片
     */
    CARD_REQ("card", 10),

    /**
     * 行情
     */
    MARKET_REQ("market", 11),

    /**
     * 保证金通知
     */
    MARGIN_NOTICE("margin", 12);

    @Getter@Setter
    private String name;
    @Getter@Setter
    private int code;

    ModuleEnum(String name, int code) {
        this.name = name;
        this.code = code;
    }
}
