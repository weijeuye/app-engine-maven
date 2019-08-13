package com.appengine.frame.help.resources;

import com.appengine.auth.annotation.AuthType;
import com.appengine.auth.annotation.BaseInfo;
import com.appengine.auth.spi.CookieAuthSpi;
import com.appengine.frame.context.RequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * 解决网页认证问题，set cookie
 * <p>
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-06 22:50.
 */
@Controller
@RequestMapping("/proxy")
public class ProxyResource {

    private static final Logger logger = LoggerFactory.getLogger(ProxyResource.class);

    @BaseInfo(desc = "代理url，种cookie", needAuth = AuthType.REQUIRED)
    @RequestMapping(value = "", method = RequestMethod.GET)
    public String proxy(RequestContext rc, @RequestParam String url, HttpServletResponse response) {
        Cookie cookie = new Cookie(CookieAuthSpi.COOKIE_NAME, CookieAuthSpi.generateCookie(rc.getCurrentUid()));
        cookie.setMaxAge((int) CookieAuthSpi.COOKIE_EXPIRES_TIME);
        response.addCookie(cookie);
        logger.info("proxy: " + url);
        return "redirect:" + url;
    }

}
