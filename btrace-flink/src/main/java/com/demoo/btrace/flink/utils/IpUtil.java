package com.demoo.btrace.flink.utils;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Enumeration;
import java.util.regex.Pattern;

/**
 * @author zhxy
 * @Date 2021/7/1 4:32 下午
 */

@Slf4j
public class IpUtil {

    private static String ip = null;
    private static final String LOCALHOST = "127.0.0.1";
    private static final String ANYHOST = "0.0.0.0";
    private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");

    public static String getIp() {
        if (ip == null) {
            ip = getIpByInt();
        }
        return ip;
    }

    private static boolean isValidAddress(InetAddress address) {
        if (address == null || address.isLoopbackAddress())
            return false;
        String name = address.getHostAddress();
        return (name != null
                && !ANYHOST.equals(name)
                && !LOCALHOST.equals(name)
                && IP_PATTERN.matcher(name).matches());
    }

    /**
     * 遍历本地网卡，返回第一个合理的IP。
     *
     * @return 本地网卡IP
     */
    private static InetAddress getLocalAddress() {
        InetAddress localAddress = null;
        try {
            localAddress = InetAddress.getLocalHost();
            if (isValidAddress(localAddress)) {
                return localAddress;
            }
        } catch (Throwable e) {
            log.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            if (interfaces != null) {
                while (interfaces.hasMoreElements()) {
                    try {
                        NetworkInterface network = interfaces.nextElement();
                        Enumeration<InetAddress> addresses = network.getInetAddresses();
                        if (addresses != null) {
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    log.warn("Failed to retriving ip address, " + e.getMessage(), e);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        log.warn("Failed to retriving ip address, " + e.getMessage(), e);
                    }
                }
            }
        } catch (Throwable e) {
            log.warn("Failed to retriving ip address, " + e.getMessage(), e);
        }
        log.error("Could not get local host ip address, will use 127.0.0.1 instead.");
        return localAddress;
    }

    public static String getIpByInt() {
        InetAddress address = getLocalAddress();
        byte[] bytes = address.getAddress();
        int ipInt = 0;
        if (bytes.length == 4) {
            ipInt = ByteBuffer.wrap(bytes).getInt();
        }
        return getIpByInt(ipInt);
    }

    public static String getIpByInt(int ipInt) {
        int[] src = new int[4];
        src[0] = ((ipInt & 0xFF000000) >>> 24);
        src[1] = ((ipInt & 0x00FF0000) >>> 16);
        src[2] = ((ipInt & 0x0000FF00) >>> 8);
        src[3] = (ipInt & 0x000000FF);
        return String.valueOf(src[0]) + "." + String.valueOf(src[1]) + "." + String.valueOf(src[2]) + "." + String.valueOf(src[3]);
    }
}
