package github.zimoyin.application.uilts;

/**
 * 字符串相似度比较算法：Jaro–Winkler similarity
 *
 * @link <a href="https://www.jianshu.com/p/a4af202cb702">原理及实现</a>
 */
public class JaroWinklerDistance {

    private final float MAX_P = 0.25f;
    private final int MAX_L = 4;
    private float p = 0.1f;

    public static void main(String args[]) {
        String s1 = "abcdefgh";
        String s2 = "abehc";

        JaroWinklerDistance distance = new JaroWinklerDistance();
        System.out.println("字符串A(\"" + s1 + "\")" + "和字符串B(\"" + s2 + "\")：");
        System.out.println("Jaro similarity:" + distance.getJaroDistance(s1, s2));
        System.out.println("Jaro-Winkler similarity:" + distance.getJaroWinklerDistance(s1, s2));
    }

    /**
     * 用户可以修改p参数，以提高共同前缀的权重
     *
     * @param p
     */
    private void setP(float p) {
        this.p = p;
    }

    public float getJaroDistance(CharSequence s1, CharSequence s2) {
        if (s1 == null || s2 == null) return 0f;
        int result[] = matches(s1, s2);
        float m = result[0];
        if (m == 0f)
            return 0f;

        float j = ((m / s1.length() + m / s2.length() + (m - result[1]) / m)) / 3;
        return j;
    }

    public float getJaroWinklerDistance(CharSequence s1, CharSequence s2) {
        if (s1 == null || s2 == null) return 0f;
        int result[] = matches(s1, s2);

        float m = result[0];
        if (m == 0f)
            return 0f;

        float j = ((m / s1.length() + m / s2.length() + (m - result[1]) / m)) / 3;
        float jw = j + Math.min(p, MAX_P) * result[2] * (1 - j);
        return jw;


    }

    private int[] matches(CharSequence s1, CharSequence s2) {
        //用max来保存较长的字符串，min保存较短的字符串
        //这是为了以短字符串为行元素遍历，长字符串为列元素遍历。
        CharSequence max, min;
        if (s1.length() > s2.length()) {
            max = s1;
            min = s2;
        } else {
            max = s2;
            min = s1;
        }

        //匹配窗口的大小，对于每一行i，列j只在(i-matchedwindow,i+matchedwindow)内移动，
        //在该范围内遇到相等的字符，表示匹配成功
        int matchedWindow = Math.max(max.length() / 2 - 1, 0);
        //记录字符串的匹配状态，true表示已经匹配成功
        boolean[] minMatchFlag = new boolean[min.length()];
        boolean[] maxMatchFlag = new boolean[max.length()];
        int matches = 0;

        for (int i = 0; i < min.length(); i++) {
            char minChar = min.charAt(i);
            //列元素的搜索：j的变化包括i往前搜索窗口长度和i往后搜索窗口长度。
            for (int j = Math.max(i - matchedWindow, 0);
                 j < Math.min(i + matchedWindow + 1, max.length()); j++) {
                if (!maxMatchFlag[j] && minChar == max.charAt(j)) {
                    maxMatchFlag[j] = true;
                    minMatchFlag[i] = true;
                    matches++;
                    break;
                }
            }
        }
        //求转换次数和相同前缀长度
        int transpositions = 0;
        int prefix = 0;

        int j = 0;
        for (int i = 0; i < min.length(); i++) {
            if (minMatchFlag[i]) {
                while (!maxMatchFlag[j]) j++;

                if (min.charAt(i) != max.charAt(j)) {
                    transpositions++;
                }
                j++;
            }
        }

        for (int i = 0; i < min.length(); i++) {
            if (s1.charAt(i) == s2.charAt(i)) {
                prefix++;
            } else {
                break;
            }
        }

        return new int[]{matches, transpositions / 2, prefix > MAX_L ? MAX_L : prefix};
    }
}