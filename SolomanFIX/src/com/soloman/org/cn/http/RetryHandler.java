package com.soloman.org.cn.http;

/*
 Android Asynchronous Http Client
 Copyright (c) 2011 James Smith <james@loopj.com>
 http://loopj.com

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*
 Some of the retry logic in this class is heavily borrowed from the
 fantastic droid-fu project: https://github.com/donnfelker/droid-fu
 */

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.HashSet;

import javax.net.ssl.SSLHandshakeException;

import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;

import android.os.SystemClock;

/**
 * 请求重连机制
 * 
 */
class RetryHandler implements HttpRequestRetryHandler
{

    private static final int RETRY_SLEEP_TIME_MILLIS = 1500;

    private static HashSet<Class<?>> exceptionWhitelist = new HashSet<Class<?>>();

    private static HashSet<Class<?>> exceptionBlacklist = new HashSet<Class<?>>();

    static
    {
        // Retry if the server dropped connection on us
        exceptionWhitelist.add(NoHttpResponseException.class);
        // retry-this, since it may happens as part of a Wi-Fi to 3G failover
        exceptionWhitelist.add(UnknownHostException.class);
        // retry-this, since it may happens as part of a Wi-Fi to 3G failover
        exceptionWhitelist.add(SocketException.class);

        // never retry timeouts
        exceptionBlacklist.add(InterruptedIOException.class);
        // never retry SSL handshake failures
        exceptionBlacklist.add(SSLHandshakeException.class);
    }

    private final int maxRetries;// 最大重试次数

    /**
     * @param maxRetries
     *            重新连接最大次数
     */
    public RetryHandler(int maxRetries)
    {
        this.maxRetries = maxRetries;
    }

    /**
     * 当http执行过程中发送错误，进行重新连接尝试 返回true将继续重试
     */
    public boolean retryRequest(IOException exception, int executionCount,
            HttpContext context)
    {
        boolean retry = true;

        Boolean b = (Boolean) context
                .getAttribute(ExecutionContext.HTTP_REQ_SENT);

        boolean sent = (b != null && b.booleanValue());

        if (executionCount > maxRetries)
        {
            // Do not retry if over max retry count
            retry = false;
        }
        else if (exceptionBlacklist.contains(exception.getClass()))
        {
            // immediately cancel retry if the error is blacklisted
            retry = false;
        }
        else if (exceptionWhitelist.contains(exception.getClass()))
        {
            // immediately retry if error is whitelisted
            retry = true;
        }
        else if (!sent)
        {
            // for most other errors, retry only if request hasn't been fully
            // sent yet
            retry = true;
        }

        if (retry)
        {
            // resend all idempotent requests
            HttpUriRequest currentReq = (HttpUriRequest) context
                    .getAttribute(ExecutionContext.HTTP_REQUEST);
            if (currentReq == null)
            {
                retry = false;
            }
            else
            {
                retry = true;
                // String requestType = currentReq.getMethod();
                // retry = requestType.equals("POST");
            }
        }

        if (retry)
        {
            SystemClock.sleep(RETRY_SLEEP_TIME_MILLIS);
        }
        else
        {
            exception.printStackTrace();
        }

        return retry;
    }
}