package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;
import searchengine.config.Site;
import searchengine.config.SitesList;
import searchengine.dto.Indexing.IndexingResponse;
import searchengine.model.*;
import searchengine.repositories.IndexRepositories;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SiteRepositories;
import searchengine.services.ServiceLemma;
import searchengine.services.ServiceLinks;
import searchengine.services.interfaces.IndexingService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;


@Service
@RequiredArgsConstructor
public class IndexingServiceImpl implements IndexingService {
    private static final ForkJoinPool JOIN_POOL = new ForkJoinPool();
    private final ServiceLemma serviceLemma;
    private final SitesList sites;
    private final SiteRepositories siteRepositories;
    private final PageRepositories pageRepositories;
    private final LemmaRepositories lemmaRepositories;
    private final IndexRepositories indexRepositories;

    private final HashMap<SiteEntity, ServiceLinks> controlWorkIndexingProgress = new HashMap<>();
    private long waitStopForkJoinPool = 1_000;
    private boolean inWork = false;
    private boolean isStop = false;

    /**
     * Indexing starts and stops almost immediately, but due to the features of Forkjoinpool,
     * a quick start and start is not possible without errors, it takes time,
     * for this a simple delay method is used for correct operation.
     * Description of the work:
     * the first start occurs immediately, then when the user stops,
     * a delay is set for re-indexing for 45 seconds.
     */
    @Override
    public IndexingResponse startIndexing() {
        try {
            Thread.sleep(waitStopForkJoinPool);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (!inWork) {
            isStop = false;
            waitStopForkJoinPool = 1_000;
            List<Site> sitesList = sites.getSites();
            sitesList.forEach(site -> {
                SiteEntity siteEntity = siteRepositories.findByName(site.getName());
                if (siteEntity != null) {
                    siteRepositories.delete(siteEntity);
                }
            });
            controlWorkIndexingProgress.clear();
            for (Site site : sitesList) {
                PageEntity page = new PageEntity();
                page.setSiteEntity(createSiteInDataBase(site));
                page.setPath(site.getUrl());
                ServiceLinks task = getServiceLinks().setEntity(page);
                new Thread(() -> JOIN_POOL.invoke(task)).start();
                controlWorkIndexingProgress.put(page.getSiteEntity(), task);
            }
        } else {
            IndexingResponse response = new IndexingResponse();
            response.setResult(false);
            response.setError("Индексация уже запущена");
            return response;
        }
        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        return response;
    }

    @Override
    public IndexingResponse stopIndexing() {
        if (inWork) {
            isStop = true;
            waitStopForkJoinPool = 45_000;
            List<SiteEntity> siteEntityList = siteRepositories.findAll();
            siteEntityList.forEach(site -> {
                if (site.getStatus().equals(Status.INDEXING)) {
                    site.setStatus(Status.FAILED);
                    site.setLastError("Индексация остановлена пользователем");
                    siteRepositories.save(site);
                }
            });
        } else {
            IndexingResponse response = new IndexingResponse();
            response.setResult(false);
            response.setError("Индексация не запущена");
            return response;
        }
        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        return response;
    }


    @Override
    public IndexingResponse indexPage(String urlPage) {
        String resultUrl = java.net.URLDecoder.decode(urlPage, StandardCharsets.UTF_8);
        String atrHref = resultUrl.substring(4).toLowerCase();
        SiteEntity site = new SiteEntity();
        List<SiteEntity> siteEntityList = siteRepositories.findAll();
        if (!inWork && !siteEntityList.isEmpty()) {
            isStop = false;
            try {
                Connection.Response responsePage = connect(atrHref);

                StringBuilder builder = new StringBuilder();
                Document docPage = responsePage.parse();
                builder.append(docPage);

                for (SiteEntity siteEntity : siteEntityList) {
                    String siteUrl = siteEntity.getUrl();
                    if (atrHref.startsWith(siteUrl)) {
                        PageEntity page = new PageEntity();
                        page.setPath(atrHref);
                        page.setSiteEntity(siteEntity);
                        page.setContent(builder.toString());
                        page.setCode(responsePage.statusCode());
                        siteEntity.setStatus(Status.INDEXED);
                        siteEntity.setLastError("");
                        site = siteEntity;
                        PageEntity pageEntity = pageRepositories.findByPath(page.getPath());
                        pageRepositories.delete(pageEntity);
                        siteRepositories.save(siteEntity);
                        pageRepositories.save(page);

                        HashMap<String, Integer> lemmaMap = new HashMap<>(serviceLemma.getLemmasInText(builder));
                        for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
                            String lemma = entry.getKey();
                            Integer count = entry.getValue();
                            LemmaEntity lemmaEntity = saveLemmaEntityInRepo(page, lemma, count);
                            saveIndexEntityInRepo(lemmaEntity, page, count);
                        }
                    }
                }
            } catch (HttpStatusException e) {
                PageEntity page = new PageEntity();
                page.setCode(e.getStatusCode());
                page.setPath(e.getUrl());
                page.setContent(e.getMessage());
                page.setSiteEntity(site);
                pageRepositories.save(page);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            IndexingResponse response = new IndexingResponse();
            response.setResult(false);
            response.setError("Данная страница находится за пределами сайтов, указанных в конфигурационном файле");
            return response;
        }
        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        return response;
    }

    public void taskController() {
        for (Map.Entry<SiteEntity, ServiceLinks> entry : controlWorkIndexingProgress.entrySet()) {
            SiteEntity siteEntity = entry.getKey();
            ServiceLinks serviceLinks = entry.getValue();
            synchronized (siteRepositories) {
                SiteEntity siteStatus = siteRepositories.findByName(siteEntity.getName());
                String status = String.valueOf(siteStatus.getStatus());
                if ("INDEXING".equals(status) && serviceLinks.isDone()) {
                    siteStatus.setStatus(Status.INDEXED);
                    System.out.println("Индексация сайта:{" + siteEntity.getName() + "}:{" + serviceLinks.isDone() + "}: Status:" + status);
                    siteStatus.setLastError(null);
                    siteRepositories.save(siteStatus);
                }
            }
        }
    }


    @Override
    public IndexingResponse deleteDataBase() {
        List<Site> sitesList = sites.getSites();
        sitesList.forEach(site -> {
            SiteEntity siteEntity = siteRepositories.findByName(site.getName());
            siteRepositories.delete(siteEntity);
        });
        IndexingResponse response = new IndexingResponse();
        response.setResult(true);
        return response;
    }


    public SiteEntity createSiteInDataBase(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setStatus(Status.FAILED);
        siteEntity.setLastError("Ошибка индексации: главная страница сайта не доступна");
        siteEntity.setUrl(site.getUrl());
        siteEntity.setName(site.getName());
        siteRepositories.save(siteEntity);
        return siteEntity;
    }

    public LemmaEntity saveLemmaEntityInRepo(PageEntity pageEntity, String lemma, Integer value) {
        LemmaEntity lemmaEntity = lemmaRepositories.findBySiteEntityAndLemma(pageEntity.getSiteEntity(), lemma);
        if (lemmaEntity == null) {
            lemmaEntity = new LemmaEntity();
            lemmaEntity.setSiteEntity(pageEntity.getSiteEntity());
            lemmaEntity.setFrequency(value);
            lemmaEntity.setLemma(lemma);
        } else {
            lemmaEntity.setFrequency(lemmaEntity.getFrequency() + value);
        }
        lemmaRepositories.save(lemmaEntity);
        return lemmaEntity;
    }

    public void saveIndexEntityInRepo(LemmaEntity lemmaEntity, PageEntity pageEntity, Integer value) {
        IndexEntity indexEntity = new IndexEntity();
        indexEntity.setLemmaEntity(lemmaEntity);
        indexEntity.setPageEntity(pageEntity);
        float rank = (float) value;
        indexEntity.setRank(rank);
        indexRepositories.save(indexEntity);
    }

    public Connection.Response connect(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                .referrer("http://www.google.com")
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .timeout(240000)
                .maxBodySize(0)
                .execute();
    }

    @Lookup
    public ServiceLinks getServiceLinks() {
        return null;
    }

    public boolean getIsStop() {
        return isStop;
    }

    public void setInWork(boolean inWork) {
        this.inWork = inWork;
    }
}
