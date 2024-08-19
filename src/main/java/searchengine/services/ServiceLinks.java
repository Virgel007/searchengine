package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import searchengine.model.*;
import searchengine.repositories.IndexRepositories;
import searchengine.repositories.LemmaRepositories;
import searchengine.repositories.PageRepositories;
import searchengine.repositories.SiteRepositories;
import searchengine.services.implementations.IndexingServiceImpl;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveAction;

@Service
@RequiredArgsConstructor
@Scope("prototype")
public class ServiceLinks extends RecursiveAction {
    private final SiteRepositories siteRepositories;
    private final PageRepositories pageRepositories;
    private final LemmaRepositories lemmaRepositories;
    private final IndexRepositories indexRepositories;
    private final IndexingServiceImpl indexingService;
    private final ServiceLemma serviceLemma;
    private HashSet<String> linkList;
    private PageEntity pageEntity;
    static boolean stop = false;

    public ServiceLinks setEntity(PageEntity pageEntity, HashSet<String> linkList) {
        this.pageEntity = pageEntity;
        this.linkList = linkList;
        return this;
    }

    public ServiceLinks setEntity(PageEntity pageEntity) {
        this.pageEntity = pageEntity;
        this.linkList = new HashSet<>();
        return this;
    }

    @Override
    protected void compute() {
        stop = indexingService.getIsStop();
        if (stop) {
            indexingService.setInWork(false);
            return;
        }

        indexingService.setInWork(true);
        TreeSet<ServiceLinks> listTask = new TreeSet<>(Comparator.comparing(o -> o.pageEntity.getSiteEntity().getUrl()));

        try {

            Thread.sleep(150);
            Connection.Response responseFirst = connect(pageEntity.getPath());
            Document document = responseFirst.parse();
            Elements elements = document.select("a[href]");

            for (Element element : elements) {
                indexingService.taskController();
                stop = indexingService.getIsStop();
                if (stop) {
                    indexingService.setInWork(false);
                    return;
                }

                String atrHref = element.absUrl("href");
                if (atrHref.startsWith(pageEntity.getPath())
                        && !atrHref.contains("#")
                        && !linkList.contains(atrHref)) {

                    if (!(atrHref.startsWith("https://") ||
                            atrHref.startsWith("http://"))) {
                        continue;
                    }

                    SiteEntity siteEntity = getSiteEntity();
                    siteEntity.setStatus(Status.INDEXING);
                    PageEntity page = new PageEntity();
                    page.setPath(atrHref);
                    page.setSiteEntity(siteEntity);
                    ServiceLinks serviceLinks = getServicesLinks();
                    serviceLinks.setEntity(page, linkList);
                    serviceLinks.fork();
                    listTask.add(serviceLinks);
                    linkList.add(atrHref);

                    Connection.Response responsePage = connect(atrHref);
                    Document docPage = responsePage.parse();

                    StringBuilder builder = new StringBuilder();
                    builder.append(docPage);
                    page.setContent(builder.toString());
                    page.setCode(responsePage.statusCode());

                    if (!stop) {
                        siteRepositories.save(getSiteEntity());
                    }

                    synchronized (pageRepositories) {
                        pageRepositories.save(page);
                    }

                    HashMap<String, Integer> lemmaMap = new HashMap<>(serviceLemma.getLemmasInText(builder));
                    for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
                        String lemma = entry.getKey();
                        Integer count = entry.getValue();
                        LemmaEntity lemmaEntity = saveLemmaEntityInRepo(lemma, count);
                        saveIndexEntityInRepo(lemmaEntity, page, count);
                    }
                }
            }

        } catch (InterruptedException e) {
            SiteEntity siteEntity = getSiteEntity();
            siteEntity.setStatus(Status.FAILED);
            siteEntity.setLastError(e.getMessage());
            siteRepositories.save(siteEntity);
        } catch (HttpStatusException e) {
            if (!linkList.contains(e.getUrl())) {
                PageEntity page = new PageEntity();
                page.setCode(e.getStatusCode());
                page.setPath(e.getUrl());
                page.setContent(e.getMessage());
                page.setSiteEntity(getSiteEntity());
                pageRepositories.save(page);
                linkList.add(e.getUrl());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public LemmaEntity saveLemmaEntityInRepo(String lemma, Integer value) {
        synchronized (lemmaRepositories) {
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
    }

    public void saveIndexEntityInRepo(LemmaEntity lemmaEntity, PageEntity pageEntity, Integer value) {
        synchronized (indexingService) {
            IndexEntity indexEntity = new IndexEntity();
            indexEntity.setLemmaEntity(lemmaEntity);
            indexEntity.setPageEntity(pageEntity);
            float rank = (float) value;
            indexEntity.setRank(rank);
            indexRepositories.save(indexEntity);
        }
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

    public SiteEntity getSiteEntity() {
        SiteEntity siteEntity = pageEntity.getSiteEntity();
        siteEntity.setLastError(null);
        return siteEntity;
    }


    @Lookup
    public ServiceLinks getServicesLinks() {
        return null;
    }
}