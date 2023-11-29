package github.zimoyin.application.command.filter;

import github.zimoyin.mtool.annotation.Filter;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.command.filter.AbstractFilter;
import lombok.extern.slf4j.Slf4j;

/**
 * 全局过滤器
 */
@Filter
@Slf4j
public class TestFilter2 extends AbstractFilter {
    @Override
    public boolean filter(CommandData data) {
        log.info("github.zimoyin.tool.command.filter.TestFilter2 run...");
        return true;
    }
}
