package github.zimoyin.application.dao;

public enum State {
    /**
     * 添加记录
     */
    Add(1),
    /**
     * 删除记录。被标记为删除状态的记录不可被恢复，并且在持久化数据库时先执行删除操作
     */
    Delete(-1),
    /**
     * 更新记录
     */
    Update(2),
    /**
     * 无操作
     */
    NoAction(0);

    private final int state;

    State(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
}
