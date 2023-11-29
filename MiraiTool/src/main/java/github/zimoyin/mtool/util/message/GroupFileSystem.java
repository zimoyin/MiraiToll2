package github.zimoyin.mtool.util.message;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.mamoe.mirai.contact.FileSupported;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.file.AbsoluteFile;
import net.mamoe.mirai.contact.file.AbsoluteFolder;
import net.mamoe.mirai.contact.file.RemoteFiles;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.ProgressionCallback;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
public class GroupFileSystem {
    private final Group Group;
    private final RemoteFiles remoteFiles;
    private final AbsoluteFolder absoluteFolder;
    private final FileSupported fileSupported;
    private final GroupFile ROOT;
    private final HashMap<String, GroupFile> indexMap = new HashMap<>();
    private String path = "./";

    public GroupFileSystem(Group group) {
        this.Group = group;
        //表示一个远程文件或目录.
        remoteFiles = group.getFiles();
        //表示远程文件列表 (管理器).
        absoluteFolder = remoteFiles.getRoot();
        //绝对文件或目录标识. 精确表示一个远程文件. 不会受同名文件或目录的影响.
        fileSupported = remoteFiles.getContact();
        //根目录
        ROOT = new GroupFile(remoteFiles, group, this);
    }

    /**
     * 根据远程文件构建一个对象，该对象是由文件系统进行管理的
     *
     * @param path  远程文件路径
     * @param group 群ID
     */
    public static GroupFile createGroupFile(String path, Group group) {
        //新建个文件系统，从系统中查找到指定文件
        GroupFileSystem system = new GroupFileSystem(group);
        List<String> paths = Arrays.stream(path.split("/")).filter(s -> s != null && !s.isEmpty()).collect(Collectors.toList());
//        System.out.println("path: " + path);
//        System.out.println("paths: " + paths);
        GroupFile file = null;
        List<GroupFile> list = system.list();
        for (String key : paths) {
            file = list.stream().filter(groupFile -> groupFile.getFileName().equalsIgnoreCase(key)).findFirst().orElse(null);
            if (file == null) return null;
            if (file.isDirectory()) list = file.list();
        }
        return file;
    }

    @Deprecated
    public static GroupFile createGroupFileByIndex(String path, Group group) {
        if (path.indexOf("/") != 0) path = "/" + path;
        //新建个文件系统，从系统中查找到指定文件
        GroupFileSystem system = new GroupFileSystem(group);
        system.buildIndex();
        return system.getIndexMap().get(path);
    }

    public static List<AbsoluteFile> find(String path, Group group) {
        AbsoluteFolder root = group.getFiles().getRoot();
        return root.resolveFilesStream(path).collect(Collectors.toList());
    }

    /**
     * 上传文件
     *
     * @param group    群对象
     * @param name     文件的名称
     * @param resource 文件实例 ExternalResource.create(...) 获取，但是请注意关闭他
     * @param call     上传回调
     */
    public static void uploadFileToRoot(Group group, String name, ExternalResource resource, ProgressionCallback<AbsoluteFile, Long> call) {
        group.getFiles().uploadNewFile(name, resource, call);
    }

    /**
     * 上传文件
     *
     * @param group 群对象
     * @param file  文件
     * @param call  上传回调
     */
    public static void uploadFileToRoot(Group group, File file, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        try (ExternalResource resource = ExternalResource.create(file);) {
            group.getFiles().uploadNewFile(file.getName(), resource, call);
        }
    }

    /**
     * 上传文件
     *
     * @param group 群对象
     * @param path  文件路径
     * @param call  上传回调
     */
    @Deprecated
    public static void uploadFileToRoot(Group group, String path, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        uploadFileToRoot(group, new File(path), call);
    }

    public void buildIndex() {
        List<GroupFile> list = ROOT.list().stream().filter(GroupFile::isDirectory).collect(Collectors.toList());
        for (GroupFile groupFile : list) {
            initIndex0(groupFile);
        }
    }

    private void initIndex0(GroupFile file) {
        if (file.isFile()) return;
        file.list();
        for (GroupFile groupFile : file.list().stream().filter(GroupFile::isDirectory).collect(Collectors.toList())) {
            initIndex0(groupFile);
        }
    }

    public GroupFile findByIndex(String path) {
        HashMap<String, GroupFile> index = this.getIndexMap();
        if (path.indexOf("/") != 0) path = "/" + path;
        return index.get(path);
    }

    public List<GroupFile> list() {
        return ROOT.list();
    }

    /**
     * 上传文件
     *
     * @param name     文件的名称
     * @param resource 文件实例 ExternalResource.create(...) 获取，但是请注意关闭他
     * @param call     上传回调
     * @return 文件对象
     */
    public AbsoluteFile uploadFileToRoot(String name, ExternalResource resource, ProgressionCallback<AbsoluteFile, Long> call) {
        return getGroup().getFiles().uploadNewFile(name, resource, call);
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @param call 上传回调
     * @return 文件对象
     */
    public AbsoluteFile uploadFileToRoot(File file, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        try (ExternalResource resource = ExternalResource.create(file);) {
            return getGroup().getFiles().uploadNewFile(file.getName(), resource, call);
        }
    }

    /**
     * 上传文件
     *
     * @param path 文件路径
     * @param call 上传回调
     * @return 文件对象
     */
    public AbsoluteFile uploadFileToRoot(String path, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        return uploadFileToRoot(new File(path), call);
    }
}
