package github.zimoyin.solver.gui;

import javafx.application.Application;

public class LoginSolverGuiRun {
    private volatile static LoginSolverGuiRun INSTANCE;

    private LoginSolverGuiRun() {
        new Thread(() -> {
            Thread.currentThread().setName("login-gui-run");
            Application.launch(LoginSolverGui.class);
        }).start();
    }

    public static LoginSolverGuiRun getInstance() {
        if (INSTANCE == null) synchronized (LoginSolverGuiRun.class) {
            if (INSTANCE == null) INSTANCE = new LoginSolverGuiRun();
        }
        return INSTANCE;
    }
}
