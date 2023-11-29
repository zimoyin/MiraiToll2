package github.zimoyin.application.dao.chat;

import java.util.List;

public interface ThesaurusDao {

    List<Thesaurus> getThesaurus();

    boolean addThesaurus(Thesaurus thesaurus);

    List<String> getKeywords();

    List<String> getValues(String key);
}
