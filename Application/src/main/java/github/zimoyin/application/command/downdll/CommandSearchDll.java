package github.zimoyin.application.command.downdll;

import github.zimoyin.mtool.annotation.Command;
import github.zimoyin.mtool.annotation.CommandClass;
import github.zimoyin.mtool.command.CommandData;
import github.zimoyin.mtool.command.ForwardMessageData;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * 下载Dll
 */
@CommandClass
public class CommandSearchDll {
    @Command(value = "dll-search",description = "搜索可下载的DLL")
    public String searchDll(CommandData data){
        String param = data.getParam();
        try {
            ArrayList<SearchDll.DllPojo> pojos = SearchDll.searchDlls(param.trim().toLowerCase());
            ForwardMessageData messageData = new ForwardMessageData(data);
            for (SearchDll.DllPojo pojo : pojos) {
                StringBuilder buffer = new StringBuilder();
                buffer.append("Name: ").append(pojo.getName()).append("\n");
                buffer.append("Architecture: ").append(pojo.getArchitecture()).append("\n");
                buffer.append("FileSize: ").append(pojo.getFileSize()).append("\n");
                buffer.append("Version: ").append(pojo.getVersion()).append("\n");
                buffer.append("Description: ").append(pojo.getDescription()).append("\n");
                buffer.append("URL: ").append(pojo.getDownloadViewUrl()).append("\n");
                messageData.append(buffer.toString());
            }
            data.sendForwardMessage(messageData);
        } catch (Exception e) {
            return "无法获取到DLL";
        }
        return null;
    }
}
