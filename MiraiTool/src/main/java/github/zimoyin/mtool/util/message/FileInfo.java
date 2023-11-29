package github.zimoyin.mtool.util.message;


import github.zimoyin.mtool.util.net.httpclient.HttpClientResult;
import github.zimoyin.mtool.util.net.httpclient.HttpClientUtils;
import kotlin.coroutines.Continuation;
import net.mamoe.mirai.contact.file.AbsoluteFile;
import net.mamoe.mirai.utils.ProgressionCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.UUID;


/**
 * 构建一个文件描述,以此用于FileUtil 上传文件
 */
@Deprecated
public class FileInfo {
    private static Logger logger = LoggerFactory.getLogger(FileInfo.class);//日志
    private HttpClientResult results;

    /**
     * 文件名称
     */
    private String name;

    /**
     * 本地文件的位置
     */
    private String nativeFilePath;

    /**
     * 网络文件的位置
     */
    private URL netFileURL;
    /**
     * 默认给一个UUID作为一个文件名称
     * QQ文件目录中的位置 /root/1.txt
     * QQ目录只能最多存在二级目录
     * 当目录不存在时会尝试创建
     */
    private String QQServerFilePath;
    /**
     * 回调函数
     * 默认空实现，等着用户传值，覆盖实现
     */
    private ProgressionCallback<AbsoluteFile, Long> callback;
    private Continuation<AbsoluteFile> listener;
    /**
     * 本地/网络输入流
     */
    private InputStream inputStream;

    public FileInfo() {
    }

    public FileInfo(URL netFileURL) {
        this.netFileURL = netFileURL;
    }

    public FileInfo(String nativeFilePath) {
        this.nativeFilePath = nativeFilePath;
    }

    public FileInfo(String nativeFilePath, String QQServerFilePath) {
        this.nativeFilePath = nativeFilePath;
        this.QQServerFilePath = QQServerFilePath;
    }

    public FileInfo(URL netFileURL, String QQServerFilePath) {
        this.netFileURL = netFileURL;
        this.QQServerFilePath = QQServerFilePath;
    }

    public FileInfo(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public FileInfo(String QQServerFilePath, InputStream inputStream) {
        this.QQServerFilePath = QQServerFilePath;
        this.inputStream = inputStream;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (QQServerFilePath == null) QQServerFilePath = name;
        this.name = name;
    }

    public String getNativeFilePath() {
        return nativeFilePath;
    }

    public void setNativeFilePath(String nativeFilePath) {
        this.nativeFilePath = nativeFilePath;
    }

    public URL getNetFileURL() {
        return netFileURL;
    }

    public void setNetFileURL(URL netFileURL) {
        netFileURL = netFileURL;
    }

    public String getQQServerFilePath() {
        if (QQServerFilePath == null) return UUID.randomUUID().toString();
        return QQServerFilePath;
    }

    public void setQQServerFilePath(String QQServerFilePath) {
        if (name == null) {
            String[] split = QQServerFilePath.split("/");
            name = split[split.length - 1];
        }
        this.QQServerFilePath = QQServerFilePath;
    }

    public ProgressionCallback<AbsoluteFile, Long> getCallback() {
        return callback;
    }

    public void setCallback(ProgressionCallback<AbsoluteFile, Long> callback) {
        this.callback = callback;
    }

    public Continuation<AbsoluteFile> getListener() {
        return listener;
    }

    public void setListener(Continuation<AbsoluteFile> listener) {
        this.listener = listener;
    }


    /**
     * 当 InputStream 为 null 时会尝试自动获取创建一个 InputStream
     *
     * @return
     */
    public InputStream getInputStream() {
        if (inputStream == null) {
            try {
                setInputStream();
            } catch (Exception e) {
                logger.error("无法获取文件输入流", e);
            }
        }
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    private void setInputStream() throws Exception {
        if (this.nativeFilePath != null) {
            this.inputStream = new FileInputStream(nativeFilePath);
        } else if (this.netFileURL != null) {
            this.results = HttpClientUtils.doGet(netFileURL.toString());
            this.inputStream = this.results.getInputStream();
        }
    }

    public void release() throws IOException {
        if (inputStream != null) {
            inputStream.close();
        }

        if (results != null) {
            results.release();
        }
    }
}
