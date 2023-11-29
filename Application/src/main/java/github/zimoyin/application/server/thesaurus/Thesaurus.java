package github.zimoyin.application.server.thesaurus;

import java.util.ArrayList;
import java.util.HashMap;

public interface Thesaurus {

    public void addEntries(String key, String value);

    public boolean removeEntries(String key, String value);

    public ArrayList<String> removeEntries(String key);


    public ArrayList<String> getEntries(String key);

    public void addCache(HashMap<String, ArrayList<String>> cache);

    public void clearCache();

    public int size();

}
