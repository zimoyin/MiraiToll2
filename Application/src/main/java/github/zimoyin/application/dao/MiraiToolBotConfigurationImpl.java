package github.zimoyin.application.dao;

import github.zimoyin.mtool.config.MiraiToolBotConfiguration;
import github.zimoyin.solver.ImageLoginSolverKt;

public class MiraiToolBotConfigurationImpl extends MiraiToolBotConfiguration {
    @Override
    public void initBefore() {
        setQRLoginEnabled();
    }

    @Override
    public void initAfter() {
        ImageLoginSolverKt solverKt = new ImageLoginSolverKt();//有短信验证
        setLoginSolver(solverKt);
    }
}
