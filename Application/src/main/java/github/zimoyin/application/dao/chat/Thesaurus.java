package github.zimoyin.application.dao.chat;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
public class Thesaurus {
    /**
     * text_key
     */
    private String text_key;
    /**
     * text_value
     */
    private String text_value;
    /**
     * id
     */
    private Long id;
    /**
     * source
     */
    private String source;
    /**
     * date
     */
    private Date date;

    public Thesaurus(String text_key, String text_value, String source) {
        this.text_key = text_key;
        this.text_value = text_value;
        this.source = source;
        this.date = new Date();
    }

    public Thesaurus(String text_key, String text_value, String source, Date date) {
        this.text_key = text_key;
        this.text_value = text_value;
        this.source = source;
        this.date = date;
    }
}
