package github.zimoyin.mtool.command.filter;

import github.zimoyin.mtool.command.CommandData;

public abstract class AbstractFilter {
    /**
     * 过滤器入口方法
     *
     * @param data 对于局部过滤器，将会在命令执行前将命令数据传入。
     *             对于全局过滤器，将传入 null
     * @return (局部过滤器)是否该命令放行
     */
    public abstract boolean filter(CommandData data);
}
