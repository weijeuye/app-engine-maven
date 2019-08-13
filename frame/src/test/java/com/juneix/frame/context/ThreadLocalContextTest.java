package com.juneix.frame.context;

import com.appengine.frame.context.RequestContext;
import com.appengine.frame.context.ThreadLocalContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Authors: sofn
 * Version: 1.0  Created at 15-6-29 23:58.
 */
public class ThreadLocalContextTest {

    @Test
    public void testThreadLocalContext() {
        RequestContext context = ThreadLocalContext.getRequestContext();
        System.out.println(context.getRequestId());
        ThreadLocalContext.clear();
        RequestContext context2 = ThreadLocalContext.getRequestContext();
        System.out.println(context2.getRequestId());
        Assert.assertNotSame(context.getRequestId(), context2.getRequestId());
    }

    @Test
    public void testThreadLocalContextMultiThread() {
        final RequestContext context = ThreadLocalContext.getRequestContext();
        System.out.println(context.getRequestId());
        Thread thread = new Thread(() -> {
            RequestContext context2 = ThreadLocalContext.getRequestContext();
            System.out.println(context2.getRequestId());
            Assert.assertEquals(context.getRequestId(), context2.getRequestId());
        });
        thread.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }

    @Test
    public void testThreadLocalContextThreadPool() throws InterruptedException, ExecutionException {
        final RequestContext context = ThreadLocalContext.getRequestContext();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Future<RequestContext> future = executor.submit(() -> {
            Thread.sleep(1000);
            return ThreadLocalContext.getRequestContext();
        });
        ThreadLocalContext.clear();

        RequestContext context2 = ThreadLocalContext.getRequestContext();

        Future<RequestContext> future2 = executor.submit(() -> {
            Thread.sleep(1000);
            return ThreadLocalContext.getRequestContext();
        });
        Future<RequestContext> future3 = executor.submit(() -> {
            Thread.sleep(1000);
            return ThreadLocalContext.getRequestContext();
        });
        RequestContext context3 = future.get();
        RequestContext context4 = future2.get();
        RequestContext context5 = future3.get();
        System.out.println(context.getRequestId() + " " + System.identityHashCode(context));
        System.out.println(context2.getRequestId() + " " + System.identityHashCode(context2));
        System.out.println(context3.getRequestId() + " " + System.identityHashCode(context3));
        System.out.println(context4.getRequestId() + " " + System.identityHashCode(context4));
        System.out.println(context5.getRequestId() + " " + System.identityHashCode(context5));
    }
}
