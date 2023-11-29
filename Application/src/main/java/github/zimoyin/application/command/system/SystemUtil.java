package github.zimoyin.application.command.system;


public class SystemUtil {

    public static String getOsName() {
        String osName = System.getProperty("os.name");
        if (osName == null) {
            return "";
        }
        return osName;
    }

    public static String getOsArch() {
        String osArch = System.getProperty("os.arch");
        if (osArch == null) {
            return "";
        }
        return osArch;
    }

    public static String getOsVersion() {
        String osVersion = System.getProperty("os.version");
        if (osVersion == null) {
            return "";
        }
        return osVersion;
    }

    public static boolean isLinux() {
        return getOsName().toLowerCase().contains("linux");
    }

    public static boolean isWindows() {
        return getOsName().toLowerCase().contains("windows");
    }

    public static String getLineSeprator() {
        String lineSeprator = System.getProperty("line.separator");
        if (lineSeprator == null) {
            return "";
        }
        return lineSeprator;
    }

    public static String getUsername() {
        String username = System.getProperty("user.name");
        if (username == null) {
            return "";
        }
        return username;
    }

    public static String getUserHome() {
        String userhome = System.getProperty("user.home");
        if (userhome == null) {
            return "";
        }
        return userhome;
    }

    public static String getUserDir() {
        String userdir = System.getProperty("user.dir");
        if (userdir == null) {
            return "";
        }
        return userdir;
    }
}
