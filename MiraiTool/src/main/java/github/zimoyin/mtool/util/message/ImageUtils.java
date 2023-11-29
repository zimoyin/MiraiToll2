package github.zimoyin.mtool.util.message;

import github.zimoyin.mtool.util.net.httpclient.HttpClientResult;
import github.zimoyin.mtool.util.net.httpclient.HttpClientUtils;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.utils.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Objects;


/**
 * 图片工具：用来格式化可发送图片和发送图片
 * 官方注释摘录：
 * [Image] 提供 [Builder] 构建方式, 可以指定 [width], [height] 等额外参数. 请尽可能提供这些参数以提升图片发送的成功率和 [Image.isUploaded] 的准确性.
 */
public class ImageUtils {
    private static final Logger logger = LoggerFactory.getLogger(ImageUtils.class);//日志
//    private static Logger logger = LoggerExp.getLogger(ImageUtils.class);

    private ImageUtils() {
    }

    /**
     * 发送图片
     *
     * @param inputStream 图片流
     * @param contact     联系人对象
     */
    public static void sendImage(InputStream inputStream, Contact contact) {
        long s = System.currentTimeMillis();
        contact.sendMessage(Objects.requireNonNull(getImage(inputStream, contact)));
        long e = System.currentTimeMillis();
        logger.debug(" :[系统日志]" + contact.getId() + " <--发送图片用时: " + ((e - s) / 1000) + " s");
    }

    /**
     * 发送图片
     *
     * @param url     图片地址
     * @param contact 联系人对象
     */
    public static void sendImage(String url, Contact contact) throws IOException {
        long s = System.currentTimeMillis();
        contact.sendMessage(Objects.requireNonNull(getImage(url, contact)));
        long e = System.currentTimeMillis();
        logger.debug(" :[系统日志]" + contact.getId() + " <--发送图片用时: " + ((e - s) / 1000) + " s");
    }

    public static Image getImage(InputStream stream) {
        return getImage(stream, getFriend());
    }

    /**
     * 返回可发送的图片
     *
     * @param inputStream 图片流
     * @param contact     联系人对象
     * @return Image对象
     */
    public static Image getImage(InputStream inputStream, Contact contact) {
        ExternalResource externalResource;
        try {
            //创建外部资源
            externalResource = ExternalResource.create(inputStream);
            return getImage0(externalResource, contact);
        } catch (Exception e) {
            logger.error("获取图片时发生了异常", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    logger.error("关闭图片输入流时发生了异常", e);
                }
            }
        }
        return null;
    }

    public static Image getImage(URL url) {
        return getImage(url.toString(), getFriend());
    }

    /**
     * 返回可发送的图片（构建一个图片对象）
     *
     * @param url     图片地址
     * @param contact 联系人对象
     * @return Image对象
     */
    public static Image getImage(String url, Contact contact) {
        HttpClientResult result = null;
        try {
            result = HttpClientUtils.doGet(url);
            InputStream inputStream = result.getInputStream();
            Image image = getImage(inputStream, contact);

        } catch (Exception e) {
            logger.error("上传图片失败", e);
        } finally {
            try {
                if (result != null) result.release();
            } catch (IOException e) {
                logger.error("释放资源失败", e);
            }
        }
        return null;
    }


    public static Image getImage(byte[] bytes) {
        return getImage(bytes, getFriend());
    }

    /**
     * 返回可发送的图片对象
     *
     * @param b       图片二进制字节
     * @param contact 联系人对象
     * @return Image对象
     */
    public static Image getImage(byte[] b, Contact contact) {
        //创建外部资源
        ExternalResource externalResource = ExternalResource.create(b);
        return getImage0(externalResource, contact);
    }

    public static Image getImage(File file) {
        return getImage(file, getFriend());
    }

    public static Image getImage(File file, Contact contact) {
        try {
            return getImage(new FileInputStream(file), contact);
        } catch (FileNotFoundException e) {
            logger.error("读取文件", e);
        }
        return null;
    }

    /**
     * 通过图片的ID来构建图片对象
     */
    public static Image getImage(String id) {
        return Image.fromId(id);
    }


    public static Image getImage0(ExternalResource resource, Contact contact) {
        try {
            long s = System.currentTimeMillis();
            //创建外部资源
            Image image1 = contact.uploadImage(resource);
            long e = System.currentTimeMillis();
            logger.debug(" :[系统日志]" + image1.getImageId() + " (ID)<--获取格式化的图片(流)用时: " + ((e - s) / 1000) + " s");
            return image1;
        } catch (Exception e) {
            logger.error("获取图片时发生了异常", e);
        } finally {
            try {
                if (resource != null) {
                    resource.close();
                }
            } catch (Exception e) {
                logger.error("关闭图片输入流时发生了异常", e);
            }
        }
        return null;
    }

    /**
     * 返回图片的URL
     *
     * @param image 图片对象
     * @return Image url
     */
    public static String getURL(Image image) {
        return Image.queryUrl(image);
    }

    /**
     * 获取图片的InputStream流
     *
     * @param image 图片对象
     * @return Image 的流
     */
    public static InputStream getImageInputStream(Image image) throws IOException {
//        return HttpDownload.getDownloadInputStream(getURL(image));
        return HttpClientUtils.doGet(getURL(image)).getInputStream();
    }

    /**
     * 获取图片的二进制数组
     *
     * @param image 图片对象
     * @return Image 的字节
     */
    public static byte[] getImageByBytes(Image image) throws IOException {
//        InputStream downloadInputStream = HttpDownload.getDownloadInputStream(getURL(image));
        InputStream downloadInputStream = getImageInputStream(image);

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int code = 0;
        byte[] b = new byte[2 * 1024];
        while ((code = downloadInputStream.read(b)) != -1) {
            byteArrayOutputStream.write(b, 0, code);
        }

        return byteArrayOutputStream.toByteArray();
    }

    //随机获取个BOT的Contact
    private static Friend getFriend() {
        List<Bot> list = Bot.getInstances();
        if (list.size() >= 1) {
            return list.get(0).getAsFriend();
        }
        return null;
    }

    public Image.Builder getImageBuilder(String id) {
        return Image.newBuilder(id);
    }
}
