package github.zimoyin.mtool.util.net.httpclient;

import github.zimoyin.mtool.util.IO;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.util.function.Consumer;

/**
 * Description: 封装httpClient响应结果
 *
 * @author JourWon
 * @date Created on 2018年4月19日
 */
public class HttpClientResult implements Serializable, AutoCloseable {

    private static final long serialVersionUID = 2168152194164783950L;

    /**
     * 响应状态码
     */
    private int code;
    private java.net.URI URI;

    private boolean isInputStreamClose = false;

    private String content;
    private byte[] contentByte;

    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;


    public HttpClientResult() {
    }

    public HttpClientResult(int code) {
        this.code = code;
    }

    public HttpClientResult(int code, CloseableHttpResponse response, CloseableHttpClient httpClient) {
        this.response = response;
        this.httpClient = httpClient;
        this.code = code;
    }

    public byte[] getBytes() throws IOException {
        try {
            contentByte = EntityUtils.toByteArray(response.getEntity());
            return contentByte;
        } finally {
            release();
        }
    }

    public int getCode() {
        return code;
    }

    public String getContent() throws IOException {
        try {
            content = EntityUtils.toString(response.getEntity(), "UTF-8");
            return content;
        } finally {
            release();
        }
    }


    public java.net.URI getURI() {
        return URI;
    }

    public void setURI(java.net.URI URI) {
        this.URI = URI;
    }


    /**
     * 获取网络输入流，注意：InputStream.close() 方法无法关闭输入流或者socket，请使用该工具类的release()方法关闭socket等其他资源
     *
     * @return
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (isInputStreamClose) return null;
        return response.getEntity().getContent();
    }

    /**
     * 获取response header中Content-Disposition中的filename值
     *
     * @return
     */
    public String getFileName() {
        Header contentHeader = response.getFirstHeader("Content-Disposition");
        String filename = null;
        if (contentHeader != null) {
            HeaderElement[] values = contentHeader.getElements();
            if (values.length == 1) {
                NameValuePair param = values[0].getParameterByName("filename");
                if (param != null) {
                    //filename = new String(param.getValue().toString().getBytes(), "utf-8");
                    //filename=URLDecoder.decode(param.getValue(),"utf-8");
                    filename = param.getValue();
                }
            }
        }
        return filename;
    }

    public CloseableHttpResponse getResponse() {
        return response;
    }

    /**
     * 保存到文件中
     *
     * @param filePath
     */
    public void toFile(String filePath) throws IOException {
        IO.toFile(getInputStream(), filePath);
        release();
    }


    /**
     * Description: 释放资源
     *
     * @throws IOException
     */
    public void release() throws IOException {
        // 释放资源
        HttpClientUtils.release(response, httpClient);
        isInputStreamClose = true;
    }

    /**
     * EventSource 是服务器推送的一个网络事件接口。一个 EventSource 实例会对 HTTP 服务开启一个持久化的连接，以text/event-stream 格式发送事件，会一直保持开启直到被要求关闭。
     *
     * @param source
     * @link <a href="https://developer.mozilla.org/zh-CN/docs/Web/API/EventSource">文档</a>
     */
    public void eventSource(Consumer<String> source) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(this.getInputStream()))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                source.accept(line);
            }
        } finally {
            release();
        }

    }

    @Override
    public void close() throws Exception {
        release();
    }
}
