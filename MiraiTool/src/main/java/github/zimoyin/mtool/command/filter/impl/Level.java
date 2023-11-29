package github.zimoyin.mtool.command.filter.impl;

public enum Level {
    /**
     * 系统级别权限
     */
    System(-10086),
    /**
     * root权限，只有机器人的主人与机器人本身才能执行的命令
     */
    Root(0),
    /**
     * 机器人管理权限
     */
    First(1),
    /**
     * 敏感命令使用权限
     */
    Second(2),
    /**
     * 普通成员
     */
    UNLevel(3);
    private int level;

    Level(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
