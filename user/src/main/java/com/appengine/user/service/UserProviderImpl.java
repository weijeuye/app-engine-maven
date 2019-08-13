package com.appengine.user.service;

import com.appengine.auth.model.AuthRequest;
import com.appengine.auth.provider.UserProvider;
import com.appengine.user.domain.User;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-02 22:10.
 */
@Service
public class UserProviderImpl implements UserProvider {
    @Resource
    private UserService userService;

    @Override
    public boolean isValidUser(long uid) {
        return userService.get(uid) != null;
    }

    @Override
    public boolean checkCanAccess(AuthRequest request, long uid) {
        return true;
    }

    @Override
    public long authUser(String loginName, String password) {
        User user = userService.login(loginName, password);
        return user != null ? user.getUid() : 0;
    }
}
