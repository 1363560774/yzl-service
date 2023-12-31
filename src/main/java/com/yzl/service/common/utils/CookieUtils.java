package com.yzl.service.common.utils;

import com.yzl.service.common.Constant;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * Cookie 工具类
 * @author zhaokai
 */
@Slf4j
public final class CookieUtils {

    /**
     * 得到Cookie的值, 不编码
     *
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName) {
        return getCookieValue(request, cookieName, null);
    }

    /**
     * 得到Cookie的值,
     *
     * @param request
     * @param cookieName
     * @return
     */
    public static String getCookieValue(HttpServletRequest request, String cookieName, String charset) {
        Cookie[] cookieList = request.getCookies();
        if (cookieList == null || cookieName == null) {
            return null;
        }
        String retValue = null;
        try {
            for (Cookie cookie : cookieList) {
                if (cookie.getName().equals(cookieName)) {
                    if (charset != null && charset.length() > Constant.ZERO) {
                        retValue = URLDecoder.decode(cookie.getValue(), charset);
                    } else {
                        retValue = cookie.getValue();
                    }
                    break;
                }
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Cookie Decode Error.", e);
        }
        return retValue;
    }

    public static CookieBuilder newBuilder() {
        return new CookieBuilder();
    }

    public static class CookieBuilder {
        private HttpServletRequest request;
        private HttpServletResponse response;
        private Integer maxAge;
        private String charset;
        private boolean httpOnly = false;
        private String domain;
        private String path = "/";
        private String name;
        private String value;

        public CookieBuilder() {

        }

        public CookieBuilder request(HttpServletRequest request) {
            this.request = request;
            return this;
        }
        public CookieBuilder response(HttpServletResponse response) {
            this.response = response;
            return this;
        }

        public CookieBuilder maxAge(int maxAge) {
            this.maxAge = maxAge;
            return this;
        }

        public CookieBuilder charset(String charset) {
            this.charset = charset;
            return this;
        }
        public CookieBuilder domain(String domain) {
            this.domain = domain;
            return this;
        }
        public CookieBuilder path(String path) {
            this.path = path;
            return this;
        }
        public CookieBuilder value(String value) {
            this.value = value;
            return this;
        }
        public CookieBuilder name(String name) {
            this.name = name;
            return this;
        }

        public CookieBuilder httpOnly(boolean httpOnly) {
            this.httpOnly = httpOnly;
            return this;
        }

        public void build() {
            try {
                if (StringUtils.isBlank(charset)) {
                    charset = "utf-8";
                }
                if(StringUtils.isBlank(name)||StringUtils.isBlank(value)){
                    throw new RuntimeException("cookie名称和值不能为空！");
                }
                if (StringUtils.isNotBlank(charset)) {
                    value = URLEncoder.encode(value, charset);
                }
                Cookie cookie = new Cookie(name, value);
                if (maxAge != null && maxAge > Constant.ZERO) {
                    cookie.setMaxAge(maxAge);
                }

                if(StringUtils.isNotBlank(domain)){
                    cookie.setDomain(domain);
                }else if (null != request) {
                    // 设置域名的cookie
//                    cookie.setDomain(getDomainName(request));
                    cookie.setDomain(request.getServerName());
                }
                // 设置path
                cookie.setPath("/");
                if(StringUtils.isNotBlank(path)){
                    cookie.setPath(path);
                }
                cookie.setHttpOnly(httpOnly);
                response.addCookie(cookie);
            } catch (Exception e) {
                log.error("Cookie Encode Error.", e);
            }
        }

        /**
         * 得到cookie的域名
         */
        private String getDomainName(HttpServletRequest request) {
            String domainName = null;

            String serverName = request.getRequestURL().toString();
            if (serverName == null || serverName.equals("")) {
                domainName = "";
            } else {
                serverName = serverName.toLowerCase();
                serverName = serverName.substring(7);
                final int end = serverName.indexOf("/");
                serverName = serverName.substring(Constant.ZERO, end);
                final String[] domains = serverName.split("\\.");
                int len = domains.length;
                if (len > 3) {
                    // www.xxx.com.cn
                    domainName = domains[len - 3] + "." + domains[len - 2] + "." + domains[len - 1];
                } else if (len <= 3 && len > 1) {
                    // xxx.com or xxx.cn
                    domainName = domains[len - 2] + "." + domains[len - 1];
                } else {
                    domainName = serverName;
                }
            }

            if (domainName != null && domainName.indexOf(":") > Constant.ZERO) {
                String[] ary = domainName.split("\\:");
                domainName = ary[0];
            }
            return domainName;
        }
    }

    public static String loadIp(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        } if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
