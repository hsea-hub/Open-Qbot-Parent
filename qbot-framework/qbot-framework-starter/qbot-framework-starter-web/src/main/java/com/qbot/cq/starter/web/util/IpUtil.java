package com.qbot.cq.starter.web.util;

import jakarta.servlet.http.HttpServletRequest;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public final class IpUtil {

    /** 常见的反向代理 IP 头（顺序由优到次） */
    private static final List<String> IP_HEADER_CANDIDATES = List.of(
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_CLIENT_IP",
            "HTTP_X_FORWARDED_FOR"
    );

    private IpUtil() { /* 工具类禁止实例化 */ }

    // ---------- 1. 取客户端真实 IP（Servlet 场景） ----------
    public static String getClientIp(HttpServletRequest request) {
        for (var header : IP_HEADER_CANDIDATES) {
            var ipLine = request.getHeader(header);
            if (ipLine != null && !ipLine.isBlank() && !"unknown".equalsIgnoreCase(ipLine)) {
                // X‑Forwarded‑For 可能返回 “client, proxy1, proxy2…”
                return ipLine.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    // ---------- 2. 取本机首个非回环/虚拟的 IPv4 ----------
    public static Optional<String> firstNonLoopbackIpv4() {
        try {
            return NetworkInterface.networkInterfaces()             // Stream<NetworkInterface>
                    .flatMap(NetworkInterface::inetAddresses)        // Stream<InetAddress>
                    .filter(addr -> !addr.isLoopbackAddress() && addr instanceof Inet4Address)
                    .map(InetAddress::getHostAddress)
                    .findFirst();
        } catch (SocketException e) {
            return Optional.empty();
        }
    }

    // ---------- 3. 取本机首个非回环/虚拟的 IPv6 ----------
    public static Optional<String> firstNonLoopbackIpv6() {
        try {
            return NetworkInterface.networkInterfaces()
                    .flatMap(NetworkInterface::inetAddresses)
                    .filter(addr -> !addr.isLoopbackAddress() && addr instanceof Inet6Address)
                    .map(InetAddress::getHostAddress)
                    .findFirst();
        } catch (SocketException e) {
            return Optional.empty();
        }
    }

    // ---------- 4. 在线查询公网 IP（api.ipify.org，超时可配置） ----------
    public static Optional<String> publicIp(Duration timeout) {
        var client = HttpClient.newBuilder()
                .connectTimeout(timeout)
                .build();

        var request = HttpRequest.newBuilder(URI.create("https://api.ipify.org"))
                .timeout(timeout)
                .GET()
                .build();

        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200 && !response.body().isBlank()) {
                return Optional.of(response.body().trim());
            }
        } catch (Exception ignored) { /* 网络异常时返回 empty */ }
        return Optional.empty();
    }
}
