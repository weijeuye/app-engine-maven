package com.appengine.auth.spi;


import com.appengine.auth.model.AuthException;
import com.appengine.auth.model.AuthRequest;

/**
 * @author sofn
 */
public interface AuthSpi {

    String AUTH_HEADER = "Authorization";
    String COOKIE_NAME = "AUTH_COOKIE";

    String getName();

    boolean canAuth(AuthRequest request);

    /**
     * 验证失败则抛出异常
     *
     * @param request 上下文
     * @return uid
     * @throws AuthException
     */
    long auth(AuthRequest request) throws AuthException;

    /**
     * 认证后的额外检查
     */
    void afterAuth(long uid, AuthRequest request) throws AuthException;
}
