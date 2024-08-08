package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.PageDto;
import searchengine.model.PageEntity;
import searchengine.services.interfaces.PageService;

@Service
@RequiredArgsConstructor
public class PageServiceImpl implements PageService {

    private final SiteServiceImpl siteService;
    @Override
    public PageDto mapToDtoPage(PageEntity pageEntity) {
        PageDto pageDto = new PageDto();
        pageDto.setId(pageEntity.getId());
        pageDto.setSiteDtoId(siteService.mapToDtoSite(pageEntity.getSiteEntity()));
        pageDto.setPath(pageEntity.getPath());
        pageDto.setCode(pageEntity.getCode());
        pageDto.setContent(pageEntity.getContent());
        return pageDto;
    }

    @Override
    public PageEntity mapToEntityPage(PageDto pageDto) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setId(pageDto.getId());
        pageEntity.setSiteEntity(siteService.mapToEntitySite(pageDto.getSiteDtoId()));
        pageEntity.setPath(pageDto.getPath());
        pageEntity.setCode(pageDto.getCode());
        pageEntity.setContent(pageDto.getContent());
        return pageEntity;
    }
}
