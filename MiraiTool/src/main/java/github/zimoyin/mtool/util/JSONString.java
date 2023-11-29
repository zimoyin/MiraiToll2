package github.zimoyin.mtool.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JSONString {
    /**
     * 将数组中所有元素都用双引号引起来
     */
    public static String wrapElementsInQuotes2(String str0) {
        return str0.replace("\"", "")
                .replaceAll("(?<=,|^)\\p{L}+|(?<=,|^)\\p{N}+|(?<=,|^)\\p{Punct}+", "\"$0\"")
                .replaceFirst("\"", "");
    }

    /**
     * 将数组中所有元素都用双引号引起来
     */
    public static String wrapElementsInQuotes(String str0) {
        //改进
        String str = str0.replace("\"", "");
        StringBuilder sb = new StringBuilder();
//        String regex = "[a-zA-Z0-9]+|true|false";
        String regex = "(?<=,|^)\\p{L}+|(?<=,|^)\\p{N}+|(?<=,|^)\\p{Punct}+";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        int lastIndex = 0;

        while (matcher.find()) {
            String match = matcher.group();
            int start = matcher.start();
            int end = matcher.end();
            if (start > lastIndex) {
                sb.append(str.substring(lastIndex, start));
            }
            sb.append("\"").append(match).append("\"");
            lastIndex = end;
        }

        if (lastIndex < str.length()) {
            sb.append(str.substring(lastIndex));
        }

        return sb.toString();
    }
}
