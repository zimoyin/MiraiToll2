package github.zimoyin.application.command

import github.zimoyin.application.command.filter.TestFilter
import github.zimoyin.mtool.annotation.Command
import github.zimoyin.mtool.annotation.CommandClass
import github.zimoyin.mtool.annotation.Filter
import github.zimoyin.mtool.annotation.ThreadSpace
import github.zimoyin.mtool.command.CommandData
import github.zimoyin.mtool.command.filter.FilterTable
import github.zimoyin.mtool.command.filter.impl.Level
import github.zimoyin.mtool.dao.H2ConnectionFactory
import lombok.extern.slf4j.Slf4j
import net.mamoe.mirai.event.events.FriendMessageEvent
import net.mamoe.mirai.event.events.MessageEvent
import net.mamoe.mirai.message.data.MusicKind
import net.mamoe.mirai.message.data.MusicShare
import java.io.File
import java.io.IOException

@CommandClass
@ThreadSpace
@Slf4j
class Test2 {
    //测试 拦截器
    //测试 音乐卡片
    @ThreadSpace
    @Command("test2")
    @Filter(value = Level.UNLevel, filterCls = [TestFilter::class])
    fun a(event: MessageEvent, data: CommandData, connection: H2ConnectionFactory?) {
        println("123")
    }
}
