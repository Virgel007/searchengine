package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.statistics.DetailedStatisticsItem;
import searchengine.dto.statistics.StatisticsData;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.dto.statistics.TotalStatistics;
import searchengine.model.SiteEntity;
import searchengine.model.Status;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SiteRepositories;
import searchengine.services.interfaces.StatisticsService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {
    private final SiteRepositories siteRepositories;
    private final PageRepositories pageRepositories;
    private final LemmaRepositories lemmaRepositories;
    private final SitesList sites;
    @Override
    public StatisticsResponse getStatistics() {
        TotalStatistics total = new TotalStatistics();
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        int countLemmas = lemmaRepositories.findAll().size();
        total.setSites(sites.getSites().size());
        total.setLemmas(countLemmas);
        total.setIndexing(true);
        List<SiteEntity> siteEntityList = siteRepositories.findAll();
        if (siteEntityList.isEmpty()) {
            return statisticsIsEmptyDataBase();
        }
        for (SiteEntity siteEntity : siteEntityList) {

            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(siteEntity.getName());
            item.setUrl(siteEntity.getUrl());
            int pages = pageRepositories.countPageBySiteId(siteEntity.getId());
            int lemmas = lemmaRepositories.countLemmaBySiteId(siteEntity.getId());
            Status status = siteEntity.getStatus();
            String error = siteEntity.getLastError();
            Date statusTime = siteEntity.getStatusTime();
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(status.toString());
            item.setError(error);
            item.setStatusTime(statusTime.getTime());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas());
            detailed.add(item);
        }
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

    public StatisticsResponse statisticsIsEmptyDataBase() {
        List<DetailedStatisticsItem> detailed = new ArrayList<>();
        TotalStatistics total = new TotalStatistics();
        String[] errors = {
                "Ошибка индексации: главная страница сайта не доступна",
                "Ошибка индексации: сайт не доступен",
                ""
        };
        int countPage = 0;
        int countLemmas = 0;
        total.setSites(sites.getSites().size());
        total.setPages(countPage);
        total.setLemmas(countLemmas);
        total.setIndexing(false);
        List<Site> sitesList = sites.getSites();
        for (Site site : sitesList) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = 0;
            int lemmas = 0;
            String status = "FAILED";
            long statusTime = 0;
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(status);
            item.setError(errors[0]);
            item.setStatusTime(statusTime);
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas());
            detailed.add(item);
        }
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        StatisticsResponse response = new StatisticsResponse();
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }
}
