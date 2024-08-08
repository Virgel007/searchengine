package searchengine.dto;

import lombok.Data;

@Data
public class PageDto {
    private Long id;
    private SiteDto siteDtoId;
    private String path;
    private int code;
    private String content;
}

