package com.juneix.auth;

import com.appengine.auth.spi.MAuthSpi;
import org.junit.Test;

/**
 * Authors: sofn
 * Version: 1.0  Created at 2015-10-04 22:45.
 */
public class MauthAuthSpiTest {
    @Test
    public void testCreate() {
        String mauth = MAuthSpi.generateMauth(1000);
        System.out.println(mauth);
    }
}
