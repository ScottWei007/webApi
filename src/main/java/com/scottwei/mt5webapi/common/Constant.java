package com.scottwei.mt5webapi.common;

/**
 * @author Scott Wei
 * @date 2019/8/1 14:21
 **/
public final class Constant {

    public static final String AUTH_START = "AUTH_START|VERSION=458|AGENT=MT5WEB|LOGIN=%s|TYPE=MANAGER|CRYPT_METHOD=AES256OFB|\\r\\n";

    public static final String AUTH_ANSWER = "AUTH_ANSWER|SRV_RAND_ANSWER=%s|CLI_RAND=%s|\\r\\n";

}
