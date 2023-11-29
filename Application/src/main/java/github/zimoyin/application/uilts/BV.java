package github.zimoyin.application.uilts;

public class BV {
    public static boolean isBV(String bv) {
        if (bv.length() <= 3) {
            return false;
        }
        boolean bv1 = bv.substring(0, 2).equalsIgnoreCase("bv");
        for (char c : bv.toCharArray()) {
            if (c >= '0' && c <= '9' || c >= 'A' && c <= 'Z' || c >= 'a' && c <= 'z') {
                continue;
            }
            return false;
        }
        return bv1;
    }
}
