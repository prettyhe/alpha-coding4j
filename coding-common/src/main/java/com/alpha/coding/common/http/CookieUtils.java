package com.alpha.coding.common.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * CookieUtils
 *
 * @version 1.0
 * Date: 2020-02-21
 */
public class CookieUtils {

    public static String getCookieValue(HttpServletRequest request, String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(key)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static void addCookie(HttpServletResponse response, Cookie cookie) {
        response.addCookie(cookie);
    }

    public static String genCookieDomain(HttpServletRequest request) {
        String domain = HttpUtils.getDomain(request);
        if (domain.startsWith("http://")) {
            domain = domain.replace("http://", "");
        } else if (domain.startsWith("https://")) {
            domain = domain.replace("https://", "");
        }
        return domain.split(":")[0];
    }

}
