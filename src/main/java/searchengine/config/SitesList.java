package searchengine.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "indexing-settings")
public class SitesList {
    private List<Site> sites;

    public Site searchSiteByName(String name) {
        Site site = new Site();
        for (Site s : sites) {
            if (name.contains(s.getName())) {
                site = s;
            }
        }
        return site;
    }
}
