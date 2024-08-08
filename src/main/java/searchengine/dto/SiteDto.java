package searchengine.dto;

import lombok.Data;
import searchengine.model.Status;

import java.util.Date;

@Data
public class SiteDto {
    private Long id;
    private Status status;
    private Date statusTime;
    private String lastError;
    private String url;
    private String name;
}
