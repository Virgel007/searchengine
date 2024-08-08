package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.dto.SiteDto;
import searchengine.model.SiteEntity;
import searchengine.services.interfaces.SiteService;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    @Override
    public SiteDto mapToDtoSite(SiteEntity siteEntity) {
        SiteDto siteDto = new SiteDto();
        siteDto.setId(siteEntity.getId());
        siteDto.setUrl(siteEntity.getUrl());
        siteDto.setName(siteEntity.getName());
        siteDto.setStatus(siteEntity.getStatus());
        siteDto.setStatusTime(siteEntity.getStatusTime());
        siteDto.setLastError(siteEntity.getLastError());
        return siteDto;
    }

    @Override
    public SiteEntity mapToEntitySite(SiteDto siteDto) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setId(siteDto.getId());
        siteEntity.setUrl(siteDto.getUrl());
        siteEntity.setName(siteDto.getName());
        siteEntity.setStatus(siteDto.getStatus());
        siteEntity.setStatusTime(siteDto.getStatusTime());
        siteEntity.setLastError(siteDto.getLastError());
        return siteEntity;
    }
}
