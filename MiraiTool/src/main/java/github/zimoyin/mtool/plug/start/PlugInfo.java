package github.zimoyin.mtool.plug.start;

import lombok.Data;

@Data
public class PlugInfo {
    /**
     * 插件ID
     */
    private final String PluginID;
    /**
     * 插件名称
     */
    private final String PluginName;
    /**
     * 插件版本
     */
    private final String PluginVersion;
    /**
     * 插件jar路径
     */
    private String PluginJarPath;
    /**
     * 插件jar名称
     */
    private String PluginJarName;
    /**
     * 是否被禁用
     */
    private boolean isEnabled;
    /**
     * 插件依赖项
     * 依赖为其他插件的ID
     * 对于其他包的依赖，框架会优先加载，之后再加载插件
     */
    private String[] pluginDependencies;
}
