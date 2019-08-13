package com.appengine.frame.context;

import javax.servlet.http.HttpServletRequest;

/**
 * @author jolestar
 */
public class ThreadLocalContext extends InheritableThreadLocal<RequestContext> {

    private static final RequestIDGenerator requestIdGenerator = RequestIDGenerator.getInstance();
    private static final ThreadLocalContext instance = new ThreadLocalContext();

    @Override
    protected RequestContext initialValue() {
        return new RequestContext(requestIdGenerator.nextId());
    }

    public static HttpServletRequest getServletRequest() {
        return instance.get().getOriginRequest();
    }

    public static RequestContext getRequestContext() {
        return instance.get();
    }

    public static void clear() {
        instance.remove();
    }
}
