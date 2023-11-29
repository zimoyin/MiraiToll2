package github.zimoyin.mtool.command.filter;


import github.zimoyin.mtool.command.filter.impl.Level;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 过滤表（权限白名单表）
 * 向表里面填充即可设置权限
 * 该表由应用维护，系统不维护此表，仅提供基础的删除、添加、查询
 */
@Data
public class FilterTable {
    private volatile static FilterTable obj;
    /**
     * 系统级别的权限
     */
    private ArrayList<Long> system = new ArrayList<Long>();
    /**
     * root权限
     * key:群ID
     * value:用户ID
     */
    private HashMap<Long, ArrayList<Long>> root = new HashMap<>();
    /**
     * 机器人管理权限
     * key:群ID
     * value:用户ID
     */
    private HashMap<Long, ArrayList<Long>> first = new HashMap<>();
    /**
     * 敏感命令使用权限
     * key:群ID
     * value:用户ID
     */
    private HashMap<Long, ArrayList<Long>> second = new HashMap<>();
    private FilterTable() {
    }

    public synchronized static FilterTable getInstance() {
        if (obj == null) {
            obj = new FilterTable();
        }
        return obj;
    }

    /**
     * 获取这个人的权限等级
     *
     * @param groupID 群id
     * @param userID  用户id
     * @return
     */
    public Level getLevel(long groupID, long userID) {
        //判断是否是System 权限的人
        if (system.contains(userID)) {
            return Level.System;
        }
        //判断此人在群里是否是root权限的人
        ArrayList<Long> roots = root.get(groupID);
        if (roots != null && roots.contains(userID)) {
            return Level.Root;
        }
        //判断此人在群里是否是firsts权限的人
        ArrayList<Long> firsts = first.get(groupID);
        if (firsts != null && firsts.contains(userID)) {
            return Level.Root;
        }
        //判断此人在群里是否是seconds权限的人
        ArrayList<Long> seconds = second.get(groupID);
        if (seconds != null && seconds.contains(userID)) {
            return Level.Root;
        }
        return Level.UNLevel;
    }


    /**
     * 为一个群添加个Root 权限级别的人
     *
     * @param groupID
     * @param userID
     */
    public void setRoot(long groupID, long userID) {
        ArrayList<Long> longs = this.root.get(groupID);
        if (longs == null) longs = new ArrayList<Long>();
        longs.add(userID);
        this.root.put(groupID, longs);
    }

    public void removeRoot(long groupID, long userID) {
        ArrayList<Long> longs = this.root.get(groupID);
        if (longs == null) return;
        longs.remove(userID);
        removeNullGroupTable(groupID);
    }

    /**
     * 为一个群添加个 First 权限级别的人
     *
     * @param groupID
     * @param userID
     */
    public void setFirst(long groupID, long userID) {
        ArrayList<Long> longs = this.first.get(groupID);
        if (longs == null) longs = new ArrayList<Long>();
        longs.add(userID);
        this.first.put(groupID, longs);
    }

    public void removeFirst(long groupID, long userID) {
        ArrayList<Long> longs = this.first.get(groupID);
        if (longs == null) return;
        longs.remove(userID);
        removeNullGroupTable(groupID);
    }

    /**
     * 为一个群添加个 Second 权限级别的人
     *
     * @param groupID
     * @param userID
     */
    public void setSecond(long groupID, long userID) {
        ArrayList<Long> longs = this.second.get(groupID);
        if (longs == null) longs = new ArrayList<Long>();
        longs.add(userID);
        this.second.put(groupID, longs);
    }

    public void removeSecond(long groupID, long userID) {
        ArrayList<Long> longs = this.second.get(groupID);
        if (longs == null) return;
        longs.remove(userID);
        removeNullGroupTable(groupID);
    }


    public void removeNullGroupTable(long groupID) {
        if (root.get(groupID) != null && root.get(groupID).size() == 0) root.remove(groupID);
        if (first.get(groupID) != null && first.get(groupID).size() == 0) root.remove(groupID);
        if (second.get(groupID) != null && second.get(groupID).size() == 0) root.remove(groupID);
    }
}
