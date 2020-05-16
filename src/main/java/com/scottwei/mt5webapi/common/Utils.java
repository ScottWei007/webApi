package com.scottwei.mt5webapi.common;

import org.springframework.util.StringUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;

/**
 * @author Scott Wei
 * @date 2019/8/1 15:42
 **/
public final class Utils {

    private static final byte[] webAPI = "WebAPI".getBytes(StandardCharsets.UTF_8);

    public static Optional<String> getHexStr(byte[] source) throws Exception {
        if(source == null) {
            return Optional.empty();
        }
        StringBuilder sb = new StringBuilder(32);
        for (byte b: source) {
            sb.append(String.format("%02x", b));
        }
        return Optional.of(sb.toString());
    }

    public static Optional<byte[]> getMD5(String source) throws Exception {
        if(StringUtils.isEmpty(source)) {
            Optional.empty();
        }
        return getMD5(source.getBytes());
    }

    public static Optional<byte[]> getMD5(byte[] source) throws Exception {
        if(source == null) {
            Optional.empty();
        }
        return Optional.of(MessageDigest.getInstance("MD5").digest(source));
    }

    public static Optional<String> getMD5Password(String password, String rand) throws Exception {
        if(StringUtils.isEmpty(password) || StringUtils.isEmpty(rand)) {
            return Optional.empty();
        }
        byte[] unicodePassword = password.getBytes(StandardCharsets.UTF_16LE);//little-endian byte order,掉坑了，不是UTF-8
        byte[] sum1 = getSumBytes(getMD5(unicodePassword).get(), webAPI);
        byte[] r = new BigInteger(rand,16).toByteArray();
        byte[] sum2 = getSumBytes(getMD5(sum1).get(), r);
        return getHexStr(getMD5(sum2).get());
    }

    private static byte[] getSumBytes(byte[] b1, byte[] b2) {
        byte[] sum = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, sum, 0, b1.length);
        System.arraycopy(b2, 0, sum, b1.length, b2.length);
        return sum;
    }

    public static Optional<String> getCrypt(String password, String rand) throws Exception {
        if(StringUtils.isEmpty(password) || StringUtils.isEmpty(rand)) {
            return Optional.empty();
        }
        byte[] unicodePassword = password.getBytes(StandardCharsets.UTF_16LE);//little-endian byte order,掉坑了，不是UTF-8
        byte[] sum = getSumBytes(getMD5(unicodePassword).get(), webAPI);
        byte[] initMD5 = getMD5(sum).get();
        String[] rands = rand.split("\n");
        StringBuilder sb = new StringBuilder(rands.length);
        for (String str : rands) {
            byte[] r = new BigInteger(str.trim(),16).toByteArray();
            byte[] rMD5 =  getMD5(getSumBytes(r, initMD5)).get();
            sb.append(getHexStr(rMD5).get() + "\n");
            initMD5 = rMD5;
        }
        return Optional.of(sb.toString());
    }

    public static void main(String[] args) throws Exception{
        String s = getMD5Password("Password1","4db98fec17aab4dc5a240bdc659e8395").get();
        String ss = "000102030405060708090a0b0c0d0e0f\n" +
                "101112131415161718191a1b1c1d1e1f\n" +
                "202122232425262728292a2b2c2d2e2f\n" +
                "303132333435363738393a3b3c3d3e3f";
        String sss = getCrypt("Password1",ss).get();
    }
}
