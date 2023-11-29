package github.zimoyin.application.command.filter;

import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.command.filter.AbstractFilter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestFilter extends AbstractFilter {
    @Override
    public boolean filter(CommandData data) {
        log.info("github.zimoyin.tool.command.filter.TestFilter run...");
        return true;
    }
}
