package github.zimoyin.mtool.util.net.httpclient;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.*;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.Map.Entry;

/**
 * Description: httpClient工具类
 *
 * @author JourWon
 * @date Created on 2018年4月19日
 */
@Slf4j
public class HttpClientUtils {
    // 编码格式。发送编码格式统一用UTF-8
    private static final String ENCODING = "UTF-8";
    // 设置连接超时时间，单位毫秒。
    private static final int CONNECT_TIMEOUT = 15 * 1000;
    // 请求获取数据的超时时间(即响应时间)，单位毫秒。
    private static final int SOCKET_TIMEOUT = 5 * 60 * 1000;
    /**
     * 是否需要代理
     */
    @Setter
    @Getter
    private static boolean isProxy = true;
    /**
     * 是否需要PAC来判断域名是否需要代理
     */
    @Getter
    @Setter
    private static boolean isPAC = true;

    /**
     * 发送get请求；不带请求头和请求参数
     *
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpClientResult doGet(String url) throws IOException {
        return doGet(url, null, null);
    }

    /**
     * 发送get请求；带请求参数
     *
     * @param url    请求地址
     * @param params 请求参数集合
     * @return
     * @throws Exception
     */
    public static HttpClientResult doGet(String url, Map<String, String> params) throws IOException {
        return doGet(url, null, params);
    }

    /**
     * 发送get请求；带请求头和请求参数
     *
     * @param url     请求地址
     * @param headers 请求头集合
     * @param params  请求参数集合
     * @return
     * @throws Exception
     */
    public static HttpClientResult doGet(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        //构建
        SSLConnectionSocketFactory sslsf = null;
        URI uri = null;
        try {
            //构建SSL
            sslsf = buildSSL();
            //构建URI
            uri = buildURI(url, params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 创建httpClient对象
//        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpHost proxy = getProxy(url);
        log.debug("Get(proxy {}) " + uri, proxy);
        // 创建http对象
        HttpGet httpGet = new HttpGet(uri);
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpGet.setConfig(requestConfig);

//        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36");
        httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/103.0.0.0 Safari/537.36");
        // 设置请求头
        packageHeader(headers, httpGet);

        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = null;

        // 执行请求并获得响应结果
        return getHttpClientResult(httpResponse, httpClient, httpGet);
    }


    /**
     * 发送post请求；不带请求头和请求参数
     *
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpClientResult doPost(String url) throws IOException {
        return doPost(url, null, null, null);
    }

    /**
     * 发送post请求；带请求参数
     *
     * @param url    请求地址
     * @param params 参数集合
     * @return
     * @throws Exception
     */
    public static HttpClientResult doPost(String url, Map<String, String> params) throws IOException {
        return doPost(url, null, params, null);
    }


    /**
     * 发送post请求；带请求参数
     *
     * @param url    请求地址
     * @param params 参数集合
     * @return
     * @throws Exception
     */
    public static HttpClientResult doPost(String url, Map<String, String> headers, Map<String, String> params) throws IOException {
        return doPost(url, headers, params, null);
    }

    /**
     * 发送post请求；带请求头和请求参数
     *
     * @param url     请求地址
     * @param headers 请求头集合
     * @param params  请求参数集合
     * @param body    //文件或字符串等
     *                HttpEntity reqEntity = MultipartEntityBuilder.create().addPart("file", bin).addPart("comment", comment).build();
     *                //字符串 (注意设置请求头)
     *                HttpEntity reqEntity = StringEntity("json/text");
     * @return
     * @throws Exception
     * @body 二进制文件参数，用于上传二进制文件，构建方式<br>
     */
    public static HttpClientResult doPost(String url, Map<String, String> headers, Map<String, String> params, HttpEntity body) throws IOException {

        //构建
        SSLConnectionSocketFactory sslsf = null;
        URI uri = null;
        try {
            //构建SSL
            sslsf = buildSSL();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // 创建httpClient对象
//        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(sslsf).build();

        HttpHost proxy = getProxy(url);
        log.info("Post(proxy {}) {}", proxy, url);
        // 创建http对象
        HttpPost httpPost = new HttpPost(url);
        /**
         * setConnectTimeout：设置连接超时时间，单位毫秒。
         * setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection
         * 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
         * setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
         */
        RequestConfig requestConfig = RequestConfig.custom().setProxy(proxy).setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();

        httpPost.setConfig(requestConfig);

        // 设置请求头
        /*httpPost.setHeader("Cookie", "");
        httpPost.setHeader("Connection", "keep-alive");
        httpPost.setHeader("Accept", "application/json");
        httpPost.setHeader("Accept-Language", "zh-CN,zh;q=0.9");
        httpPost.setHeader("Accept-Encoding", "gzip, deflate, br");*/
        httpPost.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/97.0.4692.71 Safari/537.36");
        packageHeader(headers, httpPost);

        // 封装请求参数
        packageParam(params, httpPost);

        //封装body
        if (body != null) httpPost.setEntity(body);

        // 创建httpResponse对象
        CloseableHttpResponse httpResponse = null;

        // 执行请求并获得响应结果
        return getHttpClientResult(httpResponse, httpClient, httpPost);
    }

    /**
     * 发送put请求；不带请求参数
     *
     * @param url 请求地址
     * @return
     * @throws Exception
     */
    public static HttpClientResult doPut(String url) throws IOException {
        return doPut(url);
    }

    /**
     * 发送put请求；带请求参数
     *
     * @param url    请求地址
     * @param params 参数集合
     * @return
     * @throws Exception
     */
    public static HttpClientResult doPut(String url, Map<String, String> params) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPut httpPut = new HttpPut(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpPut.setConfig(requestConfig);

        packageParam(params, httpPut);

        CloseableHttpResponse httpResponse = null;

        return getHttpClientResult(httpResponse, httpClient, httpPut);
    }

    /**
     * 发送delete请求；不带请求参数
     *
     * @param url 请求地址
     * @return
     * @throws IOException
     */
    public static HttpClientResult doDelete(String url) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        httpDelete.setConfig(requestConfig);

        CloseableHttpResponse httpResponse = null;
        return getHttpClientResult(httpResponse, httpClient, httpDelete);
    }

    /**
     * 发送delete请求；带请求参数
     *
     * @param url    请求地址
     * @param params 参数集合
     * @return
     * @throws IOException
     */
    public static HttpClientResult doDelete(String url, Map<String, String> params) throws IOException {
        if (params == null) {
            params = new HashMap<String, String>();
        }

        params.put("_method", "delete");
        return doPost(url, params);
    }

    /**
     * Description: 封装请求头
     *
     * @param params
     * @param httpMethod
     */
    public static void packageHeader(Map<String, String> params, HttpRequestBase httpMethod) {
        // 封装请求头
        if (params != null) {
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                // 设置到请求头到HttpRequestBase对象中
                httpMethod.setHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Description: 封装请求参数
     *
     * @param params
     * @param httpMethod
     * @throws UnsupportedEncodingException
     */
    public static void packageParam(Map<String, String> params, HttpEntityEnclosingRequestBase httpMethod) throws UnsupportedEncodingException {
        // 封装请求参数
        if (params != null) {
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }

            // 设置到请求的http对象中
            httpMethod.setEntity(new UrlEncodedFormEntity(nvps, ENCODING));
        }
    }


    /**
     * 设置代理
     */
    private static HttpHost getProxy(String url) {
        ProxyHost proxySource = null;
        if (isProxy) {
            if (isPAC) proxySource = ProxyHostPool.getInstance().getAnyProxyOrThrower(url);
            else proxySource = ProxyHostPool.getInstance().getAny();

            if (proxySource == null) {
                if (isPAC) return null;
                else throw new NullPointerException("没有可用的代理");
            }
            return new HttpHost(proxySource.getHost(), proxySource.getPort(), proxySource.getScheme());
        }
        return null;
    }

    /**
     * 设置代理
     *
     * @return
     */
    private static void addProxy(ProxyHost proxy) {
        ProxyHostPool.getInstance().add(proxy);
    }

    /**
     * Description: 获得响应结果
     *
     * @param httpResponse
     * @param httpClient
     * @param httpMethod
     * @return
     * @throws Exception
     */
    public static HttpClientResult getHttpClientResult(CloseableHttpResponse httpResponse, CloseableHttpClient httpClient, HttpRequestBase httpMethod) throws IOException {
        boolean isRelease = false;
        try {
            // 执行请求
            httpResponse = httpClient.execute(httpMethod);
            InputStream input = null;

            // 获取返回结果
            if (httpResponse != null && httpResponse.getStatusLine() != null) {
                HttpClientResult httpClientResult = new HttpClientResult(httpResponse.getStatusLine().getStatusCode(), httpResponse, httpClient);
                httpClientResult.setURI(httpMethod.getURI());
                return httpClientResult;
            }
            //此时是出错了
            isRelease = true;
            HttpClientResult httpClientResult = new HttpClientResult(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            httpClientResult.setURI(httpMethod.getURI());
            return httpClientResult;
        } finally {
            //释放出错的链接
            if (isRelease) release(httpResponse, httpClient);
        }
    }

    /**
     * Description: 释放资源
     *
     * @param httpResponse
     * @param httpClient
     * @throws IOException
     */
    public static void release(CloseableHttpResponse httpResponse, CloseableHttpClient httpClient) throws IOException {
        // 释放资源
        if (httpResponse != null) {
            httpResponse.close();
        }
        if (httpClient != null) {
            httpClient.close();
        }
    }

    /**
     * get调用接口
     *
     * @param url
     * @return 字符串
     */
    public static String httpGet(String url) throws IOException {
        return httpGet(url, "utf-8");
    }

    /**
     * get调用接口
     *
     * @param url
     * @param charsetName
     * @return 字符串
     */
    public static String httpGet(String url, String charsetName) throws IOException {
        //创建HttpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        StringBuffer stringBuffer = new StringBuffer();

        InputStream inputStream = null;
        try {
            //创建get方法连接实例，在get方法中传入待连接地址
            HttpGet httpGet = new HttpGet(url);
            //发起请求，并返回请求响应
            HttpResponse httpResponse = httpClient.execute(httpGet);
            //得到响应实体
            HttpEntity entity = httpResponse.getEntity();
            //得到实体中文件
            inputStream = entity.getContent();

            //读取返回内容
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byte[] b = new byte[6666];
            int code = 0;
            while ((code = inputStream.read(b)) != -1) {
                byteArrayOutputStream.write(b, 0, code);
            }

            return byteArrayOutputStream.toString(charsetName);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            httpClient.getConnectionManager().shutdown();
        }
//        return null;  删除可用： throws IOException
    }

    /**
     * 构建SSL
     *
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    private static SSLConnectionSocketFactory buildSSL() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        //使用 loadTrustMaterial() 方法实现一个信任策略，信任所有证书
        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            // 信任所有
            public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                return true;
            }
        }).build();
        //NoopHostnameVerifier类:  作为主机名验证工具，实质上关闭了主机名验证，它接受任何
        //有效的SSL会话并匹配到目标主机。
        HostnameVerifier hostnameVerifier = NoopHostnameVerifier.INSTANCE;
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        return sslsf;
    }

    /**
     * 构建请求地址
     *
     * @throws URISyntaxException
     */
    private static URI buildURI(String url, Map<String, String> params) throws URISyntaxException {
        // 创建访问的地址
        URIBuilder uriBuilder = new URIBuilder(url);
        if (params != null) {
            Set<Entry<String, String>> entrySet = params.entrySet();
            for (Entry<String, String> entry : entrySet) {
                uriBuilder.setParameter(entry.getKey(), entry.getValue());
            }
        }
        URI uri = uriBuilder.build();
        return uri;
    }
}