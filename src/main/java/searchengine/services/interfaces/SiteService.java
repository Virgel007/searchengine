package searchengine.services.interfaces;

import searchengine.dto.SiteDto;
import searchengine.model.SiteEntity;

public interface SiteService {

    SiteDto mapToDtoSite(SiteEntity siteEntity);

    SiteEntity mapToEntitySite(SiteDto siteDto);

}
