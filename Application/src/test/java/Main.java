import github.zimoyin.mtool.plug.loader.PlugJarLoader;

public class Main {
    public static void main(String[] args) throws ClassNotFoundException {
        PlugJarLoader loader = PlugJarLoader.getInstance("run/plug/Server.jar");
//        for (String s : PlugJarLoader.getClassNameCachesAll()) {
//            System.out.println(s);
//        }
        System.out.println(loader.loadClass("io.vertx.core.streams.Pipe"));

    }
}
