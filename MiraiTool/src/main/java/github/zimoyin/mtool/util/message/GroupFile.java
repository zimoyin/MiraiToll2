package github.zimoyin.mtool.util.message;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import net.mamoe.mirai.contact.FileSupported;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.file.AbsoluteFile;
import net.mamoe.mirai.contact.file.AbsoluteFolder;
import net.mamoe.mirai.contact.file.RemoteFiles;
import net.mamoe.mirai.utils.ExternalResource;
import net.mamoe.mirai.utils.ProgressionCallback;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@ToString
@Getter
@Builder
public class GroupFile {
    /**
     * 文件在服务器的具体路径
     */
    private String path;
    /**
     * 文件名称
     */
    private String fileName;
    /**
     * 是否是文件夹
     */
    private boolean isDirectory;
    /**
     * 是否是文件
     */
    private boolean isFile;
    /**
     * 父文件夹
     */
    private GroupFile parentFile;
    /**
     * 文件过期时间
     */
    private long expiryTime;
    /**
     * 文件创建时间
     */
    private long createTime;
    /**
     * 最后一次修改时间
     */
    private long lastModifiedTime;

    /**
     * 上传文件的ID
     */
    private String fileID;
    /**
     * 上传文件人的昵称
     */
    private String fileAuthor;
    /**
     * 上传文件人的QQ
     */
    private long fileAuthorID;
    //    private String fileURL;
    /**
     * 当前文件的大小
     * 或者当前文件夹的文件个数
     */
    private long size;
    private byte[] MD5 = new byte[0];
    private byte[] sha1 = new byte[0];

    /**
     * 是否是根目录
     */
    private boolean root = false;

    /**
     * 表示一个远程文件或目录.
     */
    private RemoteFiles thisRemoteFiles;
    /**
     * 表示远程文件
     */
    private AbsoluteFile thisRemoteFile;
    /**
     * 表示远程文件列表 (管理器).
     */
    private AbsoluteFolder thisRemoteFolder;
    /**
     * 绝对文件或目录标识. 精确表示一个远程文件. 不会受同名文件或目录的影响.
     */
    private FileSupported thisRemoteFileSupported;
    /**
     * 文件所在的群
     */
    private Group group;
    /**
     * 文件所在的文件系统
     */
    private GroupFileSystem system;

    private GroupFile(String path, String fileName, boolean isDirectory, boolean isFile, GroupFile parentFile, long expiryTime, long createTime, long lastModifiedTime, String fileID, String fileAuthor, long fileAuthorID, long size, byte[] MD5, byte[] sha1, boolean root, RemoteFiles thisRemoteFiles, AbsoluteFile thisRemoteFile, AbsoluteFolder thisRemoteFolder, FileSupported thisRemoteFileSupported, Group group, GroupFileSystem system) {
        this.path = path;
        this.fileName = fileName;
        this.isDirectory = isDirectory;
        this.isFile = isFile;
        this.parentFile = parentFile;
        this.expiryTime = expiryTime;
        this.createTime = createTime;
        this.lastModifiedTime = lastModifiedTime;
        this.fileID = fileID;
        this.fileAuthor = fileAuthor;
        this.fileAuthorID = fileAuthorID;
        this.size = size;
        this.MD5 = MD5;
        this.sha1 = sha1;
        this.root = root;
        this.thisRemoteFiles = thisRemoteFiles;
        this.thisRemoteFile = thisRemoteFile;
        this.thisRemoteFolder = thisRemoteFolder;
        this.thisRemoteFileSupported = thisRemoteFileSupported;
        this.group = group;
        this.system = system;
    }

    public GroupFile(String path, Group group) {
        GroupFile file = GroupFileSystem.createGroupFile(path, group);
        if (file == null) throw new NullPointerException("在该群" + group.getId() + "下没有找到所需的文件 " + path);
        buildThisGroupFile(file);
        system.getIndexMap().put(path, this);
    }


    public GroupFile(RemoteFiles files, Group group, GroupFileSystem groupFileSystem) {
        this.path = "/";
        root = true;
        isDirectory = true;
        //表示一个远程文件或目录.
        thisRemoteFiles = files;
        //表示远程文件列表 (管理器).
        thisRemoteFolder = files.getRoot();
        //绝对文件或目录标识. 精确表示一个远程文件. 不会受同名文件或目录的影响.
        thisRemoteFileSupported = files.getContact();
        this.group = group;
        this.system = groupFileSystem;
    }

    private static GroupFileBuilder builder() {
        return new GroupFileBuilder();
    }

    /**
     * 远程文件的文件目录
     */
    @Deprecated
    public AbsoluteFolder getRootRemoteFile() {
        return thisRemoteFiles.getRoot();
    }

    /**
     * 文件的URL
     */
    public String getURL() {
        if (isDirectory) return null;
        return thisRemoteFile.getUrl();
    }

    /**
     * 获取文件作者的名称
     */
    public String getFileAuthor() {
        if (fileAuthor == null) this.fileAuthor = group.getOrFail(fileAuthorID).getNick();
        return fileAuthor;
    }

    /**
     * 构建当前的文件对象对其字段进行赋值。
     */
    private void buildThisGroupFile(GroupFile file) {
        this.thisRemoteFolder = file.getThisRemoteFolder();
        this.thisRemoteFiles = file.getThisRemoteFiles();
        this.thisRemoteFile = file.getThisRemoteFile();
        this.group = file.getGroup();
        this.system = file.getSystem();
        this.path = file.getPath();
        this.fileName = file.getFileName();
        this.fileID = file.getFileID();
        this.MD5 = file.getMD5();
        this.sha1 = file.getSha1();
        this.size = file.getSize();
        this.expiryTime = file.getExpiryTime();
        this.parentFile = file.getParentFile();
        this.isFile = file.isFile();
        this.isDirectory = file.isDirectory();
        this.lastModifiedTime = file.getLastModifiedTime();
        this.createTime = file.getCreateTime();
        this.fileAuthorID = file.getFileAuthorID();
    }

    public List<GroupFile> list() {
        if (isFile) throw new IllegalArgumentException("无法对一个文件进行文件夹遍历");
        List<AbsoluteFolder> collect = thisRemoteFolder.foldersStream().collect(Collectors.toList());
        List<AbsoluteFile> collect1 = thisRemoteFolder.filesStream().collect(Collectors.toList());
        List<GroupFile> list = new ArrayList<GroupFile>();
        HashMap<String, GroupFile> indexMap = this.system.getIndexMap();
        for (AbsoluteFolder file : collect) {
            GroupFile build = GroupFile.builder()
                    .thisRemoteFiles(file.getContact().getFiles())
                    .thisRemoteFolder(file)
                    .group(group)
                    .system(getSystem())
                    .path(file.getName())
                    .fileName(new File(file.getName()).getName())
                    .fileID(file.getId())
                    .size(file.getContentsCount())
                    .parentFile(this)
                    .isFile(false)
                    .isDirectory(true)
                    .lastModifiedTime(file.getLastModifiedTime())
                    .createTime(file.getUploadTime())
                    .fileAuthorID(file.getUploaderId())
                    .build();
            indexMap.put(build.getPath(), build);
            list.add(build);
        }

        for (AbsoluteFile file : collect1) {
            GroupFile build = GroupFile.builder()
                    .thisRemoteFiles(file.getContact().getFiles())
                    .thisRemoteFolder(this.thisRemoteFolder)
                    .thisRemoteFile(file)
                    .group(group)
                    .system(getSystem())
                    .path(file.getName())
                    .fileName(new File(file.getName()).getName())
                    .fileID(file.getId())
                    .MD5(file.getMd5())
                    .sha1(file.getSha1())
                    .size(file.getSize())
                    .expiryTime(file.getExpiryTime())
                    .parentFile(this)
                    .isFile(true)
                    .isDirectory(false)
                    .lastModifiedTime(file.getLastModifiedTime())
                    .createTime(file.getUploadTime())
                    .fileAuthorID(file.getUploaderId())
                    .build();
            indexMap.put(build.getPath(), build);
            list.add(build);
        }
        return list;
    }

    /**
     * 重命名
     */
    public boolean renameTo(String name) {
        //更新路径
        if (getParentFile() != null) {
            HashMap<String, GroupFile> indexMap = this.system.getIndexMap();
            indexMap.remove(this.path);
            this.path = getParentFile().getPath() + "/" + name;
            indexMap.put(this.path, this);
        }
        //更新名称
        if (this.isFile) return thisRemoteFile.renameTo(name);
        else return thisRemoteFolder.renameTo(name);
    }

    /**
     * 删除文件
     */
    public boolean delete() {
        if (this.isFile) return thisRemoteFile.delete();
        else return thisRemoteFolder.delete();
    }

    /**
     * 该方法使用效果与 linux 中的 move指令一致，请仔细判断后使用
     */
    public boolean moveTo(String path) {
        boolean move = false;
        path = path.trim();
        AbsoluteFolder folder = getRootRemoteFile().resolveFolder(path);
        if (path.indexOf("/") != 0 && path.length() <= 1) throw new IllegalArgumentException("Cannot move to " + path);
        if (folder == null) throw new IllegalArgumentException("Cannot move to " + path);
        if (this.isFile) {
            HashMap<String, GroupFile> indexMap = this.system.getIndexMap();
            indexMap.remove(this.path);
            move = thisRemoteFile.moveTo(folder);
            if (move) this.path = path;
            indexMap.put(this.path, this);
        }
        return false;
    }

    /**
     * 创建文件夹
     */
    public AbsoluteFolder mkdir(String folder) {
        if (isDirectory) {
            return thisRemoteFolder.createFolder(folder);
        }
        return null;
    }


    /**
     * 构建索引
     */
    public void buildIndex() {
        system.buildIndex();
    }

    /**
     * 上传文件，如果当前对象是文件则删除文件后再上传。如果当然对象是文件夹则直接上传
     * 注意：这不会去维护现有的索引
     *
     * @param name     文件的名称
     * @param resource 文件实例 ExternalResource.create(...) 获取，但是请注意关闭他
     * @param call     上传回调
     * @return 上传的文件
     */
    public AbsoluteFile uploadFile(String name, ExternalResource resource, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        if (name == null || resource == null) throw new NullPointerException("name and resource  must not be null");
        if (this.isFile) this.delete();
        try {
            return getThisRemoteFolder().uploadNewFile(name, resource, call);
        } finally {
            resource.close();
        }
    }

    /**
     * 上传文件，如果当前对象是文件则删除文件后再上传。如果当然对象是文件夹则直接上传
     * 注意：这不会去维护现有的索引
     *
     * @param file 本机文件
     * @param call 上传回调
     * @return 上传的文件
     */
    public AbsoluteFile uploadFile(File file, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        if (file == null) throw new NullPointerException("file must not be null");
        if (this.isFile) this.delete();
        try (ExternalResource resource = ExternalResource.create(file)) {
            return uploadFile(file.getName(), resource, call);
        }
    }

    /**
     * 上传文件，如果当前对象是文件则删除文件后再上传。如果当然对象是文件夹则直接上传
     * 注意：这不会去维护现有的索引
     *
     * @param fileName 文件名称
     * @param stream   文件流
     * @param call     上传回调
     * @return 上传的文件
     */
    public AbsoluteFile uploadFile(String fileName, InputStream stream, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        if (fileName == null) throw new NullPointerException("file must not be null");
        if (this.isFile) this.delete();
        try (ExternalResource resource = ExternalResource.create(stream)) {
            return uploadFile(fileName, resource, call);
        }
    }

    /**
     * 上传文件，如果当前对象是文件则抛出异常。如果当然对象是文件夹则直接上传
     * 注意：这不会去维护现有的索引
     *
     * @param name     文件名称
     * @param resource 文件实例 ExternalResource.create(...) 获取，但是请注意关闭他
     * @param call     上传回调
     * @return 上传的文件
     */
    public AbsoluteFile uploadNewFile(String name, ExternalResource resource, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        if (name == null || resource == null) throw new NullPointerException("name and resource  must not be null");
        try {
            if (this.isDirectory) return getThisRemoteFolder().uploadNewFile(name, resource, call);
            else
                throw new IllegalArgumentException("这个GroupFile 是一个文件对象，无法对文件对象进行上传文件操作: " + getPath());
        } finally {
            resource.close();
        }
    }

    /**
     * 上传文件，如果当前对象是文件则抛出异常。如果当然对象是文件夹则直接上传
     * 注意：这不会去维护现有的索引
     *
     * @param file 本机的文件路径
     * @param call 上传回调
     * @return 上传的文件
     */
    public AbsoluteFile uploadNewFile(File file, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        if (file == null) throw new NullPointerException("file  must not be null");
        try (ExternalResource resource = ExternalResource.create(file)) {
            if (this.isDirectory) return getThisRemoteFolder().uploadNewFile(file.getName(), resource, call);
            else
                throw new IllegalArgumentException("这个GroupFile 是一个文件对象，无法对文件对象进行上传文件操作: " + getPath());
        }
    }

    /**
     * 上传文件，如果当前对象是文件则抛出异常。如果当然对象是文件夹则直接上传
     * 注意：这不会去维护现有的索引
     *
     * @param path 本机的文件路径
     * @param call 上传回调
     * @return 上传的文件
     */
    public AbsoluteFile uploadNewFile(String path, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        if (path == null) throw new NullPointerException("path  must not be null");
        File file = new File(path);
        return uploadNewFile(file, call);
    }

    /**
     * 上传文件，如果当前对象是文件则抛出异常。如果当然对象是文件夹则直接上传
     * 注意：这不会去维护现有的索引
     *
     * @param fileName 文件名称
     * @param file     本机的文件
     * @param call     上传回调
     * @return 上传的文件
     */
    public AbsoluteFile uploadNewFile(String fileName, byte[] file, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        if (path == null) throw new NullPointerException("path  must not be null");
        try (ExternalResource resource = ExternalResource.create(file)) {
            if (this.isDirectory) return getThisRemoteFolder().uploadNewFile(fileName, resource, call);
            else
                throw new IllegalArgumentException("这个GroupFile 是一个文件对象，无法对文件对象进行上传文件操作: " + getPath());
        }
    }

    /**
     * 上传文件，如果当前对象是文件则抛出异常。如果当然对象是文件夹则直接上传
     * 注意：这不会去维护现有的索引
     *
     * @param fileName    文件名称
     * @param inputStream 本机的文件
     * @param call        上传回调
     * @return 上传的文件
     */
    public AbsoluteFile uploadNewFile(String fileName, InputStream inputStream, ProgressionCallback<AbsoluteFile, Long> call) throws IOException {
        if (path == null) throw new NullPointerException("path  must not be null");
        try (ExternalResource resource = ExternalResource.create(inputStream)) {
            if (this.isDirectory) return getThisRemoteFolder().uploadNewFile(fileName, resource, call);
            else
                throw new IllegalArgumentException("这个GroupFile 是一个文件对象，无法对文件对象进行上传文件操作: " + getPath());
        }
    }
}
