package searchengine.services.interfaces;

import searchengine.dto.PageDto;
import searchengine.model.PageEntity;

public interface PageService {

    PageDto mapToDtoPage(PageEntity pageEntity);

    PageEntity mapToEntityPage(PageDto pageDto);
}
