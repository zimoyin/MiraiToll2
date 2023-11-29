package github.zimoyin.solver;

import github.zimoyin.solver.gui.LoginSolverGuiRun;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TypeSelect {
    private TypeSelect() {
    }

    public static void open() {
        LoginSolverGuiRun.getInstance();
    }
}
