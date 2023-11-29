package github.zimoyin.mtool.util.net.httpclient;

import lombok.Getter;
import lombok.Setter;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代理过滤文件（PAC）
 * 该实现是依靠主机地址与IP地址进行判断，如果有任一一项不符合就与结果产生偏差。哪怕是域名相同，ip不同
 */
@Getter
public class ProxyFilterFile {
    private static final HashMap<String, ProxyFilterFile> Instances = new HashMap<String, ProxyFilterFile>();
    /**
     * 代理域名
     */
    private final HashSet<InetAddress> PROXY_HOST = new HashSet<>();
    /**
     * 直连域名
     */
    private final HashSet<InetAddress> PROXY_DIRECT = new HashSet<>();
    /**
     * 过滤器名称
     */
    private final String name;
    /**
     * 设置为 Ture 后，如果要访问的域没有在代理列表与直接访问列表中，那此域就走代理，反之亦然
     * 默认为 false 如果没有任何匹配的域名则直连该域名
     */
    @Setter
    private volatile boolean isProxyHostList = false;
    /**
     * 是否开启动态添加域名，开启后可以针对子域名进行是否代理判断，但是开启后需要消耗额外的性能开销
     */
    @Setter
    private volatile boolean isDynamicAddition = false;

    private ProxyFilterFile(String name) {
        this.name = name;
        Instances.put(name, this);
    }

    /**
     * 获取一个过滤器配置实例
     *
     * @param name 实例名称
     */
    public static ProxyFilterFile getInstance(String name) {
        return Instances.getOrDefault(name, new ProxyFilterFile(name));
    }

    /**
     * 将列表解析出主机实例列表
     *
     * @param hosts 域名/ip/url 列表
     */
    public static List<InetAddress> parseHosts(String[] hosts) {
        List<InetAddress> addresses = new ArrayList<InetAddress>();
        for (String host : hosts) {
            addresses.add(parse(host));
        }
        return addresses;
    }

    /**
     * 将地址解析出主机实例列表
     *
     * @param host0 域名/ip/url
     */
    public static InetAddress parse(String host0) {
        Pattern pattern = Pattern.compile("(?:(?:https?|http|[a-z]*)://)?([\\w.-]+)(?::\\d+)?(?:/.*)?");
//        Pattern pattern = Pattern.compile("(?i)(?:(?:https?|tcp|udp|file|jar)://)?([a-z0-9.-]+)(?::\\\\d+)?");
        Matcher matcher = pattern.matcher(host0);
        if (matcher.find()) {
            String hostGroup = matcher.group(1);
            try {
                return InetAddress.getByName(hostGroup);
            } catch (UnknownHostException e) {
                throw new RuntimeException("无法解析该地址: " + host0, e);
            }
        }
        return null;
    }

    /**
     * 是否代理该域名
     * 该实现是依靠主机地址与IP地址进行判断，如果有任一一项不符合就与结果产生偏差。哪怕是域名相同，ip不同
     *
     * @return 是否代理该域名
     */
    public boolean isProxy(String address0) {
        InetAddress address = parse(address0);
        boolean containsProxy = PROXY_HOST.contains(address);
        boolean containsDirect = PROXY_DIRECT.contains(address);
        //如果该域名不再代理列表与直连列表，则动态添加该域名到其中一个列表中
        if (!containsDirect && !containsProxy && isDynamicAddition && address != null) return dynamicAddition(address);
        if (containsProxy) return true;
        if (containsDirect) return false;
        return isProxyHostList;
    }

    /**
     * 动态添加域名
     *
     * @return 是否需要代理当前的域名
     */
    private boolean dynamicAddition(InetAddress address) {
        String addressHostName = address.getHostName();
        boolean isProxy = PROXY_HOST.stream().anyMatch(inetAddress -> inetAddress.getHostName().contains(addressHostName) || addressHostName.contains(inetAddress.getHostName()));
        boolean isDirect = PROXY_DIRECT.stream().anyMatch(inetAddress -> inetAddress.getHostName().contains(addressHostName) || addressHostName.contains(inetAddress.getHostName()));

        if (isProxy) addProxyHostList(addressHostName);
        if (isDirect) addDirectList(addressHostName);
        if (!isProxy && !isDirect) addDirectList(addressHostName);
        return isProxy;
    }

    /**
     * 更新代理列表
     */
    public void updateProxyHostList(String... addresses) {
        PROXY_HOST.clear();
        addProxyHostList(addresses);
    }

    /**
     * 更新直连列表
     */
    public void updateProxyDirectList(String... addresses) {
        PROXY_DIRECT.clear();
        addDirectList(addresses);
    }

    /**
     * add代理列表
     */
    public void addProxyHostList(String... addresses) {
        for (String address : addresses) {
            PROXY_HOST.add(parse(address));
        }
    }

    /**
     * add直连列表
     */
    public void addDirectList(String... addresses) {
        for (String address : addresses) {
            PROXY_DIRECT.add(parse(address));
        }
    }

    /**
     * remove代理列表
     */
    public void removeProxyHostList(String... addresses) {
        for (String address : addresses) {
            PROXY_HOST.remove(parse(address));
        }
    }

    /**
     * remove直连列表
     */
    public void removeDirectList(String... addresses) {
        for (String address : addresses) {
            PROXY_DIRECT.remove(parse(address));
        }
    }
}
