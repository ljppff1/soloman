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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

/**
 * 请求参数设置类 requests made from an {@link AsyncHttpClient} instance.
 * <p>
 * For example:
 * <p>
 * 
 * <pre>
 * RequestParams params = new RequestParams();
 * params.put(&quot;username&quot;, &quot;james&quot;);
 * params.put(&quot;password&quot;, &quot;123456&quot;);
 * params.put(&quot;email&quot;, &quot;my@email.com&quot;);
 * params.put(&quot;profile_picture&quot;, new File(&quot;pic.jpg&quot;)); // Upload a File
 * params.put(&quot;profile_picture2&quot;, someInputStream); // Upload an InputStream
 * params.put(&quot;profile_picture3&quot;, new ByteArrayInputStream(someBytes)); // Upload
 *                                                                      // some
 *                                                                      // bytes
 * 
 * AsyncHttpClient client = new AsyncHttpClient();
 * client.post(&quot;http://myendpoint.com&quot;, params, responseHandler);
 * </pre>
 */
public class RequestParams
{
    private static String ENCODING = "UTF-8";

    protected ConcurrentHashMap<String, String> urlParams;

    protected Map<String, FileWrapper> fileParams;

    /**
     * Constructs a new empty <code>RequestParams</code> instance.
     */
    public RequestParams()
    {
        init();
    }

    /**
     * Constructs a new RequestParams instance containing the key/value string
     * params from the specified map.
     * 
     * @param source
     *            the source key/value string map to add.
     */
    public RequestParams(Map<String, String> source)
    {
        init();

        for (Map.Entry<String, String> entry : source.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Constructs a new RequestParams instance and populate it with a single
     * initial key/value string param.
     * 
     * @param key
     *            the key name for the intial param.
     * @param value
     *            the value string for the initial param.
     */
    public RequestParams(String key, String value)
    {
        init();

        put(key, value);
    }

    /**
     * Constructs a new RequestParams instance and populate it with multiple
     * initial key/value string param.
     * 
     * @param keysAndValues
     *            a sequence of keys and values. Objects are automatically
     *            converted to Strings (including the value {@code null}).
     * @throws IllegalArgumentException
     *             if the number of arguments isn't even.
     */
    public RequestParams(Object... keysAndValues)
    {
        init();
        int len = keysAndValues.length;
        if (len % 2 != 0)
            throw new IllegalArgumentException(
                    "Supplied arguments must be even");
        for (int i = 0; i < len; i += 2)
        {
            String key = String.valueOf(keysAndValues[i]);
            String val = String.valueOf(keysAndValues[i + 1]);
            put(key, val);
        }
    }

    /**
     * Adds a key/value string pair to the request.
     * 
     * @param key
     *            the key name for the new param.
     * @param value
     *            the value string for the new param.
     */
    public void put(String key, String value)
    {
        if (value == null)
        {
            value = "";
        }
        if (key != null && value != null)
        {
            urlParams.put(key, value);
        }
    }

    /**
     * Adds a file to the request.
     * 
     * @param key
     *            the key name for the new param.
     * @param file
     *            the file to add.
     */
    public void put(String key, File file) throws FileNotFoundException
    {
        put(key, new FileInputStream(file), file.getName());
    }

    /**
     * Adds an input stream to the request.
     * 
     * @param key
     *            the key name for the new param.
     * @param stream
     *            the input stream to add.
     */
    public void put(String key, InputStream stream)
    {
        put(key, stream, null);
    }

    /**
     * Adds an input stream to the request.
     * 
     * @param key
     *            the key name for the new param.
     * @param stream
     *            the input stream to add.
     * @param fileName
     *            the name of the file.
     */
    public void put(String key, InputStream stream, String fileName)
    {
        put(key, stream, fileName, null);
    }

    /**
     * Adds an input stream to the request.
     * 
     * @param key
     *            the key name for the new param.
     * @param stream
     *            the input stream to add.
     * @param fileName
     *            the name of the file.
     * @param contentType
     *            the content type of the file, eg. application/json
     */
    public void put(String key, InputStream stream, String fileName,
            String contentType)
    {
        if (key != null && stream != null)
        {
            fileParams.put(key, new FileWrapper(stream, fileName, contentType));
        }
    }

    /**
     * Removes a parameter from the request.
     * 
     * @param key
     *            the key name for the parameter to remove.
     */
    public void remove(String key)
    {
        urlParams.remove(key);
        fileParams.remove(key);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        for (ConcurrentHashMap.Entry<String, String> entry : urlParams
                .entrySet())
        {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append(entry.getValue());
        }

        for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams
                .entrySet())
        {
            if (result.length() > 0)
                result.append("&");

            result.append(entry.getKey());
            result.append("=");
            result.append("FILE");
        }

        return result.toString();
    }

    /**
     * 返回一个entity包含所有的返回参数 Returns an HttpEntity containing all request
     * parameters
     */
    public HttpEntity getEntity()
    {
        HttpEntity entity = null;

        // 文件上传
        if (!fileParams.isEmpty())
        {
            SimpleMultipartEntity multipartEntity = new SimpleMultipartEntity();
            if (!urlParams.isEmpty())
            {
                for (ConcurrentHashMap.Entry<String, String> entry : urlParams
                        .entrySet())
                {
                    String key = entry.getKey();
                    String prame = entry.getValue();
                    multipartEntity.addParame(key, prame);
                }
            }

            // Add string params
            for (ConcurrentHashMap.Entry<String, String> entry : urlParams
                    .entrySet())
            {
                multipartEntity.addPart(entry.getKey(), entry.getValue());
            }

            // Add file params
            int currentIndex = 0;
            int lastIndex = fileParams.entrySet().size() - 1;
            for (ConcurrentHashMap.Entry<String, FileWrapper> entry : fileParams
                    .entrySet())
            {
                FileWrapper file = entry.getValue();
                if (file.inputStream != null)
                {
                    boolean isLast = currentIndex == lastIndex;
                    if (file.contentType != null)
                    {
                        multipartEntity.addPart(entry.getKey(),
                                file.getFileName(), file.inputStream,
                                file.contentType, isLast);
                    }
                    else
                    {
                        multipartEntity.addPart(entry.getKey(),
                                file.getFileName(), file.inputStream, isLast);
                    }
                }
                currentIndex++;
            }
            entity = multipartEntity;

            // 请求参数
        }
        else
        {
            try
            {
                entity = new UrlEncodedFormEntity(getParamsList(), ENCODING);
            }
            catch(UnsupportedEncodingException e)
            {
                e.printStackTrace();
            }
        }
        return entity;
    }

    private void init()
    {
        urlParams = new ConcurrentHashMap<String, String>();
        fileParams = Collections
                .synchronizedMap(new LinkedHashMap<String, FileWrapper>());
        // fileParams = new ConcurrentHashMap<String, FileWrapper>();
    }

    protected List<BasicNameValuePair> getParamsList()
    {
        List<BasicNameValuePair> lparams = new LinkedList<BasicNameValuePair>();

        for (ConcurrentHashMap.Entry<String, String> entry : urlParams
                .entrySet())
        {
            lparams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        return lparams;
    }

    /**
     * http请求参数转换
     * 
     * @return
     */
    protected String getParamString()
    {
        return URLEncodedUtils.format(getParamsList(), HTTP.UTF_8);
    }

    /**
     * 文件上传参数
     * 
     * @author zhao
     */
    private static class FileWrapper
    {
        public InputStream inputStream;

        public String fileName;

        public String contentType;

        public FileWrapper(InputStream inputStream, String fileName,
                String contentType)
        {
            this.inputStream = inputStream;
            this.fileName = fileName;
            this.contentType = contentType;
        }

        public String getFileName()
        {
            if (fileName != null)
            {
                return fileName;
            }
            else
            {
                return "nofilename";
            }
        }
    }
}