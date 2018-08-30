package com.zlb.nettystudy.websocket.entity;

import java.io.Serializable;

/**
 * @author taoyuanming
 * Created on 2018/8/21
 * Description K线ws参数
 */
public class KLineParam implements Serializable {

    private static final long serialVersionUID = 6394403680008053473L;
    /**
     * 1-合约 2-usdk
     */
    private Integer type;

    /**
     * 1-1分钟K线 15-15分钟K线 60-1小时K线 D-1天K线
     */
    private String resolution;

    /**
     * 合约类型唯一id(type=1) 或 usdk标的物唯一id(type=2)
     */
    private Long klineId;

    /**
     * 首次获取数量
     */
    private Integer limit;

    /**
     * 截止时间
     */
    private Long endTime;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public Long getKlineId() {
        return klineId;
    }

    public void setKlineId(Long klineId) {
        this.klineId = klineId;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return "KLineParam{" +
                "type=" + type +
                ", resolution='" + resolution + '\'' +
                ", klineId=" + klineId +
                ", limit=" + limit +
                ", endTime=" + endTime +
                '}';
    }
}
