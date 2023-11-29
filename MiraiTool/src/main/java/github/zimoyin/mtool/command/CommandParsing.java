package github.zimoyin.mtool.command;

import github.zimoyin.mtool.config.global.CommandConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;


/**
 * 异常处理机制不合理，异常应该向上抛出
 * <p>
 * 解析命令字符串的主语
 */
public class CommandParsing {
    private static Logger loggerExp = LoggerFactory.getLogger(CommandParsing.class);
    private static String prefix = CommandConfig.getInstance().getCommandConfigInfo().getCommandPrefix().trim();//配置文件中的文件前缀
    private static boolean space = CommandConfig.getInstance().getCommandConfigInfo().isSpace();//配置文件中是否在前缀与主语之间存在空格


    /**
     * 判断是否是命令前缀，（判断是否是命令？）
     *
     * @param text
     * @return
     */
    @Deprecated
    public static boolean isCommandSubjectParsing(String text) {
        //解析命令
        String order = text.trim();
        //检测text是否为不为空，检测text的前缀是否是命令的前缀
        if (order.trim().length() >= prefix.length()
                && prefix.equals(order.substring(0, prefix.length()))) {
            //去除多余空格
            order = order.trim();
            if (order.length() <= prefix.length()) {
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * 解析出命令主语，不解析参数
     *
     * @param text
     * @return
     */
    public static String commandSubjectParsing(String text) {
        //解析命令
        String order = text.trim();

        //如果指令前缀在配置文件中没有设置就执行
        if (prefix.equals("null")) {
            loggerExp.debug("开始解析命令的主语： " + text);
            //去除多余空格
            order = order.trim();
            if (order.length() <= 1) {
                return null;
            }
            try {
                //分割命令和参数
                String[] s = order.split("\\s+");

                loggerExp.debug("命令主语解析为： " + s[0].trim().toLowerCase());

                //去除命令主语空格并小写
                return s[0].trim().toLowerCase();
            } catch (Exception e) {
                loggerExp.error("解析 " + order + " 命令时出现错误: ", e);
            }
            return null;
        }

        //指令前缀在配置文件中进行了设置就执行
        //检测text是否为不为空，检测text的前缀是否是命令的前缀
        if (order.trim().length() >= prefix.length() && prefix.equals(order.substring(0, prefix.length()))) {
            loggerExp.debug("开始解析命令的主语： " + text);
            //去除多余空格
            order = order.trim();
            if (order.length() <= prefix.length()) {
                return null;
            }
            try {
                //分割命令和参数
                String[] s = order.split("\\s+");

                loggerExp.debug("命令主语解析为： " + s[0].substring(prefix.length()).trim().toLowerCase());

                //去除命令主语前的前缀和空格并小写
                return s[0].substring(prefix.length()).trim().toLowerCase();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * 解析命令，命令和参数以字符串数组形式返回，字符串第一个是命令主语,后面的是参数
     * 命令主语小写
     *
     * @param order 命令
     * @return
     */
    public static String[] commandParsing(String order) {
        //解析命令

        //如果指令前缀在配置文件中没有设置就执行
        if (prefix == null) {
            //去除多余空格
            order = order.trim();
            //在配置文件的前缀模式下如果只有主语就不解析
            if (order.length() <= prefix.length()) {
                return null;
            }
            try {
                //分割命令和参数
                String[] s = order.split("\\s+");
                //去除命令主语空格并小写
                s[0] = s[0].trim();
                return s;
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        //指令前缀在配置文件中进行了设置就执行
        //检测text是否为不为空，检测text的前缀是否是命令的前缀
        if (order.trim().length() >= prefix.length() && //命令主体要大于等于命令的前缀
                prefix.equals(order.trim().substring(0, prefix.length()))) {//获取命令的命令前置是否为配置文件中定义的命令前缀
            //去除多余空格
            order = order.trim();
            try {
                //分割命令和参数
                String[] s = order.split("\\s+");
                //去除命令主语前的前缀和空格
                s[0] = s[0].substring(prefix.length()).trim();
                //删除空白字符
                ArrayList<String> param = new ArrayList<String>();
                for (String s1 : s) {
                    if (s1 == null || s1.isEmpty()) continue;
                    param.add(s1);
                }
                String[] strings = new String[param.size()];
                s = param.toArray(strings);
                //返回示例: .gpt 你好 帅  => ["gpt", "你好","帅]
                return s;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
