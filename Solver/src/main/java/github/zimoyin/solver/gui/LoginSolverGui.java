package github.zimoyin.solver.gui;

import github.zimoyin.solver.communication.CommunicationChannelOfTest;
import github.zimoyin.solver.communication.CommunicationChannelOfTicket;
import github.zimoyin.solver.communication.CommunicationChannelOfURL;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.FlowPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.BotOnlineEvent;

import java.io.File;
import java.util.function.Consumer;

@Slf4j
public class LoginSolverGui extends Application {
    private volatile WebEngine webEngine;
    private volatile boolean isRunning = true;
    private volatile String Ticket = "";
    private volatile Stage primaryStage;
    private Listener<? extends Event> tempListener;
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        //监听成功登录后，关闭窗口

        tempListener = GlobalEventChannel.INSTANCE.subscribeAlways(BotOnlineEvent.class, new Consumer<BotOnlineEvent>() {
            @Override
            public void accept(BotOnlineEvent botOnlineEvent) {
                log.info("登录成功，窗口即将被关闭");
                try {
                    stop();
                } catch (Exception e) {
                    log.warn("关闭窗口时出现了异常", e);
                }
            }
        });
        //浏览器
        WebView browser = new WebView();
        webEngine = browser.getEngine();
        webEngine.setJavaScriptEnabled(true);
//        webEngine.setUserAgent("Mozilla/5.0 (Windows NT 6.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.41 Safari/535.1 QQBrowser/6.9.11079.201");
        //QQ内置:UAgent
        webEngine.setUserAgent("Mozilla/5.0 (Linux; Android 5.1; OPPO R9tm Build/LMY47I; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/53.0.2785.49 Mobile MQQBrowser/6.2 TBS/043128 Safari/537.36 V1_AND_SQ_7.0.0_676_YYB_D PA QQ/7.0.0.3135 NetType/4G WebP/0.3.0 Pixel/1080");
        webEngine.load(String.valueOf(new File("./cache/login.png").toURI().toURL()));
        //按钮
        Button button = new Button("我已经完成了验证(Get Ticket)");
        Button button2 = new Button("验证码输入完毕");
        //文本框
        TextField textField = new TextField("请从这里输入短信验证码，如果有的话");

        //布局
        FlowPane flowPane = new FlowPane();
        flowPane.getChildren().addAll(browser, button, textField, button2);
        //窗体
        primaryStage.setScene(new Scene(flowPane));

        primaryStage.setTitle("内置登录验证浏览器");
        primaryStage.getIcons().add(new Image("http://q1.qlogo.cn/g?b=qq&nk=2556608754&s=640"));
        primaryStage.show();

        //获取短信验证码
        button2.setOnAction(event -> {
            CommunicationChannelOfTest.getInstance().setValue(textField.getText().trim());
        });
        //获取Ticket
        button.setOnAction(event -> {
            getTicket();
            if (CommunicationChannelOfTicket.getInstance().size() <= 0) {
                log.error("无法获取到 Ticket，程序将会取消阻塞代码运行，如有必要，请重启");
                CommunicationChannelOfTicket.getInstance().setValue("未能检测到Ticket，自动判断为允许运行阻塞代码");
            }
        });
        new Thread(() -> {
            Thread.currentThread().setName("login-gui-getTicket");
            long start;
            long end = System.currentTimeMillis() + 1500;
            while (true) {
                start = System.currentTimeMillis();
                if (start >= end) {
//                    log.debug("timer getTicket => start time: {},end time: {}", start, end);
                    getTicket();
                    //每 1.5s 执行一次 getTicket
                    end = start + 1500;
                }
                if (!isRunning) break;
            }
            log.info("Thread {} end", Thread.currentThread().getName());
        }).start();

        //更新URL
        new Thread(() -> {
            Thread.currentThread().setName("login-gui-uploadNewURL");
            while (true) {
                if (!isRunning) break;
                String url = CommunicationChannelOfURL.getInstance().getValue();
//                log.info("GetValue URL: {}", url);
                if (url == null) continue;
                loadURL(url);
            }
            log.info("Thread {} end", Thread.currentThread().getName());
        }).start();
    }

    @Override
    public void stop() throws Exception {
        CommunicationChannelOfURL.getInstance().setValue("end");
        isRunning = false;
        super.stop();
        Platform.runLater(() -> {
            if (primaryStage != null) {
                primaryStage.close();
            }
        });
        log.info("窗体被关闭即将被关闭");
        tempListener.complete();
    }

    private void getTicket() {
        Platform.runLater(() -> {
            try {
                //java8
//                JSObject window = (JSObject) webEngine.executeScript("capGetTicket()");
//                String ticket = (String) window.getMember("ticket");
                //java8-17
                String ticket = webEngine.executeScript("capGetTicket().ticket").toString();

                if (ticket.length() > 0) {
                    if (Ticket.equals(ticket)) return;
                    log.info("JS:capGetTicket() Ticket: " + ticket);
                    Ticket = ticket;
                    CommunicationChannelOfTicket.getInstance().setValue(Ticket);
                }
            } catch (Exception e) {
                if (Bot.getInstances().size() <= 0) isRunning = false;
//                log.debug("JS：not found capGetTicket()");
            }
        });
    }

    private void loadURL(String url) {
        Platform.runLater(() -> {
            log.info("Loading URL: {}", url);
            webEngine.load(url);
        });
    }
}
