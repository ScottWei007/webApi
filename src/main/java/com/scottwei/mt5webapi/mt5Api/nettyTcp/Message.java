package com.scottwei.mt5webapi.mt5Api.nettyTcp;

/**
 * @author Scott Wei
 * @date 2019/7/30 16:37
 **/
public class Message {

    private int bodySize;
    private int serialNumber;
    private int flag;
    private String body;

    public int getBodySize() {
        return bodySize;
    }

    public void setBodySize(int bodySize) {
        this.bodySize = bodySize;
    }

    public int getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(int serialNumber) {
        this.serialNumber = serialNumber;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
