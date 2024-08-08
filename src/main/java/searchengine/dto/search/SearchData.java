package searchengine.dto.search;

import lombok.Data;

@Data
public class SearchData {
    private String site;
    private String siteName;
    private String title;
    private String snippet;
    private float relevance;
    private String error;
    private String uri;
}
