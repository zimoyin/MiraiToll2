package github.zimoyin.application.command.downdll;

import github.zimoyin.mtool.util.net.httpclient.HttpClientResult;
import github.zimoyin.mtool.util.net.httpclient.HttpClientUtils;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.BiConsumer;

/**
 * 搜索您丢失的DLL文件
 */
@Slf4j
public class SearchDll {
    private static final String SCRIPT_NAME = "https://cn.dll-files.com";


    /**
     * 搜索dll，搜索的dll名称与key完全匹配的项
     * @param key dll 名称
     * @return dll文件列表
     */
    public static ArrayList<DllPojo> searchDlls(String key) throws Exception {
        HashMap<String, String> search = search(key);
        String url = search.get(key.toLowerCase().trim());
        if (url == null) return null;
        return view(url);
    }

    /**
     * 搜索dll，包含该key 字符串的dll
     * @param key dll名称
     * @return 键值对 名称：url
     */
    private static HashMap<String, String> search(String key) throws Exception {
        HashMap<String, String> map = new HashMap<String, String>();
        try (HttpClientResult result = HttpClientUtils.doGet(String.format(SCRIPT_NAME + "/search/?q=%s", key))) {
            String content = result.getContent();
            Element body = Jsoup.parse(content).body();
            Elements select = body.select(".results > table > tbody > tr");
            for (Element element : select) {
                Elements td = element.getElementsByTag("td");
                if (td.size() == 0) continue;
                Elements a = td.get(0).getElementsByTag("a");
                if (a.size() == 0) continue;
                String url = a.get(0).attr("href");
                String name = td.get(0).text();
                String desc = td.get(1).text();
                map.put(name, url);
            }
        }
        return map;
    }

    /**
     * dll所在的视图
     * @param key dll的 uri
     * @return 解析在这个视图中的所有dll信息
     */
    private static ArrayList<DllPojo> view(String key) throws Exception {
        ArrayList<DllPojo> list = new ArrayList<DllPojo>();
        log.info("view dll:  {}",SCRIPT_NAME + key);
        try (HttpClientResult result = HttpClientUtils.doGet(SCRIPT_NAME + key)) {
            String content = result.getContent();
            Element body = Jsoup.parse(content).body();
            Element select = body.select("#grid-container").get(0);
            Elements h2 = body.select("h2:contains(文件描述)");


//            Elements desc = select.select(".inner-grid > .left-pane");
            for (Element element : select.select(".file-info-grid")) {
                Elements infos = element.select(".file-info-grid > .inner-grid > .right-pane");
                DllPojo pojo = new DllPojo();
                for (Element info : infos) {
                    Elements p = info.select("p");
                    pojo.setVersion(p.get(0).text());
                    pojo.setArchitecture(p.get(1).text());
                    pojo.setFileSize(p.get(2).text());
                    pojo.setLanguage(p.get(3).text());
                    pojo.setCompany(p.get(4).text());
                    pojo.setDescription(p.get(5).text());
                }
                Elements downloadInfos = element.select(".file-info-grid > .download-pane");
                for (Element info : downloadInfos) {
                    Elements divs = info.select(".download-pane > div");
                    String md5 = divs.get(0).getElementsByTag("span").text();
                    String SHA1 = divs.get(1).getElementsByTag("span").text();
                    String downloadView = divs.get(2).getElementsByTag("a").attr("href");
                    String zipFileSize = divs.get(3).getElementsByTag("span").text();

                    pojo.setMD5(md5);
                    pojo.setSHA1(SHA1);
                    pojo.setDownloadViewUrl(downloadView);
                    pojo.setZipFileSize(zipFileSize);
                }
                pojo.setName(h2.text().substring(0,h2.text().indexOf("，文件描述")));
                list.add(pojo);
            }
        }
        return list;
    }

    /**
     * 解析出dll的下载地址
     */
    public static String parseUrl(DllPojo url) throws Exception {
        String content;
        try (HttpClientResult result = HttpClientUtils.doGet(SCRIPT_NAME+url.getDownloadViewUrl())) {
            content = result.getContent();
            for (Element script : Jsoup.parse(content).select("script")) {
                if (script.attributes().size() > 0 || !script.html().contains("downloadUrl")) {
                    continue;
                }
                String varDownloadUrl = script.html();
                return varDownloadUrl.substring(varDownloadUrl.indexOf("\"") + 1, varDownloadUrl.lastIndexOf("\""));
            }
        }
        return null;
    }

    @Data
    public static class DllPojo {
        private String Version;
        private String Architecture;
        private String FileSize;
        private String ZipFileSize;
        private String Language;
        private String Company;
        private String Description;
        private String MD5;
        private String SHA1;
        private String DownloadViewUrl;
        private String Name;
    }
}
