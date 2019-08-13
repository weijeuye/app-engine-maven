package com.appengine.auth.service;

import com.appengine.auth.annotation.AuthType;
import com.appengine.auth.annotation.BaseInfo;
import com.appengine.auth.model.AuthExcepFactor;
import com.appengine.auth.model.AuthException;
import com.appengine.auth.model.AuthRequest;
import com.appengine.auth.model.AuthResponse;
import com.appengine.auth.provider.DefaultUserProvider;
import com.appengine.auth.provider.UserProvider;
import com.appengine.auth.spi.AuthSpi;
import com.appengine.auth.spi.BasicAuthSpi;
import com.appengine.auth.spi.GuestAuthSpi;
import com.appengine.auth.spi.NullAuthSpi;
import com.appengine.common.context.ClientVersion;
import com.appengine.common.exception.EngineExceptionHelper;
import com.appengine.frame.spring.ApplicationContextHolder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Authors: sofn
 * Version: 1.0  Created at 15-8-30 00:22.
 */
@Service
public class DefaultAuthService implements AuthService, ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultAuthService.class);

    private ApplicationContext context;
    private List<AuthSpi> authSpis = new ArrayList<>();
    private Map<String, AuthSpi> authSpiMap = new HashMap<>();

    @Override
    public AuthResponse auth(AuthRequest request, Optional<BaseInfo> baseInfo) {
        AuthType type;
        if (baseInfo.isPresent()) {
            type = baseInfo.get().needAuth();
        } else {
            type = AuthType.REQUIRED;
        }
        AuthSpi spi = this.findAuthServiceSpi(request, type);
        long uid = 0;
        try {
            uid = spi.auth(request);
            Optional<UserProvider> provider = getUserProvider();
            if (!StringUtils.equals(spi.getName(), BasicAuthSpi.SPI_NAME)
                    && !StringUtils.equals(spi.getName(), GuestAuthSpi.SPI_NAME)
                    && (provider.isPresent() && !provider.get().isValidUser(uid))) {
                uid = 0;
                LOGGER.warn("auth passed,but uid not found: " + uid + " authType: " + spi.getName());
                throw EngineExceptionHelper.localException(AuthExcepFactor.E_USER_AUTHFAIL);
            }

            if (uid <= 0 && type.authFailThrowException() && !StringUtils.equals(spi.getName(), GuestAuthSpi.SPI_NAME)) {
                throw EngineExceptionHelper.localException(AuthExcepFactor.E_USER_AUTHFAIL);
            }
        } catch (AuthException e) {
            if (type.authFailThrowException() && (type != AuthType.OUTER || request.getFrom() != AuthRequest.RequestFrom.INNER)) {
                throw e;
            }
        }

        spi.afterAuth(uid, request);
        String remoteIp = request.getHeader(ENGINE_REMOTEIP_HEADER);
        if (remoteIp == null) {
            remoteIp = request.getRemoteIp();
        }

        String authType = spi.getName();
        int appId = NumberUtils.toInt(request.getHeader(ENGINE_APPID_HEADER));
        ClientVersion clientVersion = ClientVersion.valueOf(request.getHeader(ClientVersion.VERSION_HEADER));

        AuthResponse response = new AuthResponse((String) request.getAttribute("platform"), uid,
                remoteIp, appId, authType, clientVersion);

        try {
            LOGGER.info(String.format("auth %s %s %s %s %s", request.getRequestURI(), uid,
                    response.getAuthedBy(), remoteIp, response.getAppId()));
        } catch (Exception e) {
            LOGGER.warn("DefaultAuthServer", e);
        }
        return response;
    }

    private AuthSpi findAuthServiceSpi(AuthRequest request, AuthType type) {
        AuthSpi authSpi = null;

        if (type == AuthType.GUEST) {
            authSpi = this.getAuthSpi(GuestAuthSpi.SPI_NAME);
        } else {
            for (AuthSpi spi : this.authSpis) {
                if (spi.canAuth(request)) {
                    authSpi = spi;
                    break;
                }
            }
        }

        //默认NullAuthSpi
        if (authSpi == null) {
            authSpi = this.getAuthSpi(NullAuthSpi.SPI_NAME);
        }

        return authSpi;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, AuthSpi> spis = this.context.getBeansOfType(AuthSpi.class);

        for (AuthSpi spi : spis.values()) {
            if (spi.getClass() != NullAuthSpi.class && spi.getClass() != GuestAuthSpi.class) {
                authSpis.add(spi);
            }
            authSpiMap.put(StringUtils.lowerCase(spi.getName()), spi);
        }

    }

    @SuppressWarnings("unchecked")
    public <T extends AuthSpi> T getAuthSpi(String name) {
        return (T) this.authSpiMap.get(name.toLowerCase());
    }

    public static Optional<UserProvider> getUserProvider() {
        List<UserProvider> beans = ApplicationContextHolder.getBeans(UserProvider.class);
        if (beans.size() == 1) {
            return Optional.ofNullable(beans.get(0));
        } else if (beans.size() > 1) {
            return beans.stream().filter(b -> !(b instanceof DefaultUserProvider)).findFirst();
        } else {
            return Optional.empty();
        }

    }
}
