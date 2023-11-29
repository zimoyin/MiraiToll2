package github.zimoyin.mtool.config.global;


import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import github.zimoyin.mtool.util.OSinfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 初始化配置文件，将配置文件模板扔到外面
 */
public final class InitConfig {
    private static Logger logger = LoggerFactory.getLogger(InitConfig.class);

    //将jar内的配置文件扔到外面
    public static void init() throws IOException {
        logger.info("MiraiTool 正在启动中....." + "\n" +
                "   __   ____    ___    ___    ____   _  __  _____\n" +
                "  / /  / __ \\  / _ |  / _ \\  /  _/  / |/ / / ___/\n" +
                " / /__/ /_/ / / __ | / // / _/ /   /    / / (_ / \n" +
                "/____/\\____/ /_/ |_|/____/ /___/  /_/|_/  \\___/  \n" +
                "                                                 \n\n");
        logger.info("温馨提示： device.json文件是你虚拟设备信息，这意味着你可以靠他来在不同的设备上登录，而不需要进行设备验证等...");
        logger.info("温馨提示： 第一次运行会生成 .\\data\\config\\gloval\\* 配置文件，请打开看看它");
        logger.info("JAVA_VERSION： " + System.getProperty("java.version"));
        logger.info("OS_NAME： " + OSinfo.getOSname());

        //创建配置文件的路径文件夹
        boolean mkdirs = new File("./data/config/gloval/").mkdirs();
        logger.info("配置文件文件夹地址({})：{}", mkdirs, "./data/config/gloval/");

        //将配置文件扔到外面
        InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("config/config.json");//加载配置文件模板注册信息
        Config configs = JSON.parseObject(resourceAsStream, Config.class);//实例化文件模板注册信息
        if (configs == null) throw new IllegalStateException("[严重] 全局配置注册表无法正常被加载");
        for (JSONObject json : configs.getConfig()) {//遍历所有的注册信息
            json.entrySet().forEach(entry -> {
                String key = entry.getKey();
                Object value = entry.getValue();
                //将配置文件扔到外面
                init0(new File("./data/config/gloval/" + key), false);
            });
        }
        logger.info("安全检测： 配置文件检测完毕\n=====================================================================");
    }


    /**
     * 创建外部配置文件，jar包内的文件不方便
     *
     * @param configFile
     * @param c          如果c为true就在执行完方法后退出程序，让用户处理配置文件
     */
    private static void init0(File configFile, boolean c) {
        boolean file1 = configFile.exists();
        logger.info("检测配置文件: {} [{}]", configFile.getName(), file1 ? "存在" : "缺失");
        //没有配置文件就创建配置文件
        if (!file1) {
//            logger.warn("检测到缺失外部配置文件： " + configFile.getName());
            //配置文件模板输入流
            InputStream resourceAsStream = Thread.currentThread().
                    getContextClassLoader().getResourceAsStream("config/" + configFile.getName());
            //判断
            if (resourceAsStream == null) {
                logger.error("无法加载到Jar中资源文件：{}{}", "./config/", configFile.getName());
            }
            //配置文件模板输出流
            FileOutputStream fileWriter = null;
            try {
                fileWriter = new FileOutputStream(configFile.getPath());
                byte[] b = new byte[1024];
                int code = 0;
                while ((code = resourceAsStream.read(b)) != -1) {
                    fileWriter.write(b, 0, code);
                }
                fileWriter.flush();

                logger.info("配置文件创建完成");
                logger.info("请对 config.properties 等配置文件进行设置，在里面写入账号密码来登录");
                logger.warn("完成后请重新运行机器人");
                if (c) System.exit(0);//退出
            } catch (IOException e) {
                if (resourceAsStream != null) {
                    try {
                        resourceAsStream.close();
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                }

                if (fileWriter != null) {
                    try {
                        fileWriter.close();
                    } catch (IOException ioException) {
                        throw new RuntimeException(ioException);
                    }
                }

                logger.error("]严重] 在系统外部创建配置文件失败", e);
                System.exit(0);
            }
        }

        try {
            logger.info("配置文件地址: " + configFile.getCanonicalPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
