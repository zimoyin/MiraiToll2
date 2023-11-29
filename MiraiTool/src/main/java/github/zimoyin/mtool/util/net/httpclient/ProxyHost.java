package github.zimoyin.mtool.util.net.httpclient;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

@Getter
@ToString
public class ProxyHost {
    private String host;
    private int port;
    @Setter
    private String scheme;
    /**
     * 上次计算延迟的值，当ping 小于 0 时，表示主机不可用
     */
    private long ping = 9999;
    /**
     * 添加该代理的时间
     */
    private long addTime;
    /**
     * 主机所在的物理位置
     */
    @Setter
    private String hostLocation;

    public ProxyHost(String host, int port) {
        setHost(host);
        setPort(port);
        update();
    }

    public ProxyHost(String host, int port, String scheme) {
        setHost(host);
        setPort(port);
        this.scheme = scheme;
        update();
    }

    public ProxyHost(String host, int port, String scheme, long addTime, String hostLocation) {
        setHost(host);
        setPort(port);
        this.scheme = scheme;
        this.addTime = addTime;
        this.hostLocation = hostLocation;
        update();
    }

    public ProxyHost(String host, int port, String scheme, long addTime) {
        setHost(host);
        setPort(port);
        this.scheme = scheme;
        this.addTime = addTime;
        update();
    }

    public ProxyHost(String host, int port, long addTime) {
        setHost(host);
        setPort(port);
        this.addTime = addTime;
        update();
    }

    public ProxyHost(String host, int port, String scheme, String hostLocation) {
        setHost(host);
        setPort(port);
        this.scheme = scheme;
        this.hostLocation = hostLocation;
        update();
    }

    private void setPort(int port) {
        if (port <= 0 || port >= 65536) {
            throw new IllegalArgumentException("port must be between 0 and 65536");
        }
        this.port = port;
    }

    private void setHost(String host) {
        this.host = host;
    }


    public InetAddress getHostAddress() {
        return new InetSocketAddress("", 0).getAddress();
    }

    /**
     * 刷新ping 与 添加时间
     */
    public void update() {
        // TODO Auto-generated method stub
        this.addTime = System.currentTimeMillis();
        try {
            // 创建Socket对象并连接到指定的IP地址和端口号
            Socket socket = new Socket();
            long start = System.currentTimeMillis();
            socket.connect(new InetSocketAddress(this.host, this.port), 5 * 1000); // 5000为连接超时时间（毫秒）

            // 如果连接成功，则主机存活
            ping = (System.currentTimeMillis() - start);

            // 关闭套接字
            socket.close();
        } catch (Exception e) {
            // 如果发生异常，则主机不可达或连接超时
            ping = -1;
        }
    }
}
