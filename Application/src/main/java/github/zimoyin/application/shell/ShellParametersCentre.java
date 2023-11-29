package github.zimoyin.application.shell;

import java.util.HashMap;

public class ShellParametersCentre extends HashMap<String, Object> {
    private volatile static ShellParametersCentre INSTANCE;

    private ShellParametersCentre() {
    }

    public static ShellParametersCentre getInstance() {
        if (INSTANCE == null) synchronized (ShellParametersCentre.class) {
            if (INSTANCE == null) INSTANCE = new ShellParametersCentre();
        }
        return INSTANCE;
    }

}
