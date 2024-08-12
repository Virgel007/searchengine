package searchengine.services.implementations;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import searchengine.dto.search.SearchData;
import searchengine.dto.search.SearchResponse;
import searchengine.model.IndexEntity;
import searchengine.model.LemmaEntity;
import searchengine.model.PageEntity;
import searchengine.repositories.IndexRepositories;
import searchengine.repositories.LemmaRepositories;
import searchengine.services.ServiceLemma;
import searchengine.services.interfaces.SearchService;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ServiceLemma serviceLemma;
    private final LemmaRepositories lemmaRepositories;
    private final IndexRepositories indexRepositories;

    @Override
    public SearchResponse search(String query, int offset, int limit, String site) {
        SearchResponse response = new SearchResponse();
        List<LemmaEntity> searchLemmaList = new ArrayList<>();
        if (site != null) {
            searchLemmaList.addAll(searchLemmasInSite(query, site));
        } else searchLemmaList.addAll(searchLemmasAllSites(query));
        if (query.isEmpty()) {
            response.setResult(false);
            response.setError("Задан пустой поисковый запрос");
            return response;
        }
        if (searchLemmaList.isEmpty()) {
            response.setResult(false);
            response.setError("Указанная страница не найдена");
            return response;
        }

        int endSet = offset + limit;
        int count = 0;
        List<SearchData> searchDataList = new ArrayList<>();
        TreeMap<Float, PageEntity> relativeRelevanceInPage = searchForMultipleLemmasOnPages(searchLemmaList);
        for (Map.Entry<Float, PageEntity> entry : relativeRelevanceInPage.entrySet()) {
            SearchData data = new SearchData();
            PageEntity pageEntity = entry.getValue();
            float relevance = entry.getKey();
            data.setSite(pageEntity.getSiteEntity().getUrl());
            data.setSiteName(pageEntity.getSiteEntity().getName());
            data.setUri(chengPageUrl(pageEntity.getPath(), pageEntity.getSiteEntity().getUrl()));
            data.setTitle(getTitlePage(pageEntity));
            data.setSnippet(getSnippetPage(query, pageEntity));
            data.setRelevance(relevance);
            data.setError("");
            if (offset <= count && count < endSet) {
                searchDataList.add(data);
            }
            count++;
        }
        response.setData(searchDataList);
        response.setCount(relativeRelevanceInPage.size());
        response.setResult(true);
        return response;
    }

    public String getTitlePage(PageEntity page) {
        String doc = page.getContent();
        return Jsoup.parse(doc).title();
    }

    public String getSnippetPage(String query, PageEntity page) {
        StringBuilder builder;
        String lemmas = replaceWord(query);
        Document document = Jsoup.parse(page.getContent());
        Elements metaTags = document.getElementsByTag("meta");
        for (Element element : metaTags) {
            String content = element.attr("content").toLowerCase();
            String name = element.attr("name");
            if (name.contains("description") && content.contains(lemmas)) {
                String[] words = content.toLowerCase().split("\\s+");
                builder = new StringBuilder();
                for (String str : words) {
                    if (lemmas.contains(str)) {
                        String res = str.replaceAll(str, "<b>" + str + "</b>");
                        builder.append(res).append(" ");
                    } else {
                        builder.append(str).append(" ");
                    }
                }
                return String.valueOf(builder);
            }
        }
        String noHtmlTagsTexts = Jsoup.parse(page.getContent()).text();
        String[] textDoc = noHtmlTagsTexts.toLowerCase().split("[.]+");
        List<String> listText = new ArrayList<>();
        for (String text : textDoc) {
            if (text.length() < 5) {
                continue;
            }
            if (text.length() > 200) {
                continue;
            }
            String[] words = text.toLowerCase().split("\\s+");
            builder = new StringBuilder();
            for (String word : words) {
                String[] strings = lemmas.split("\\s+");
                if (lemmas.contains(word) || query.contains(word) || strings[0].contains(word)) {
                    if (word.length() > 3) {
                        String res = word.replaceAll(word, "<b>" + word + "</b>");
                        builder.append(res).append(" ");
                    }
                } else {
                    builder.append(word).append(" ");
                }
            }
            listText.add(String.valueOf(builder));
        }
        StringBuilder result = new StringBuilder();
        for (String str : listText) {
            if (str.contains("<b>") && result.length() < 200) {
                result.append(str).append(" ... ");
            }
        }
        if (result.isEmpty()) {
            for (Element element : metaTags) {
                String content = element.attr("content").toLowerCase();
                String name = element.attr("name");
                if (name.contains("description")) {
                    result.append("Поиск: ").append("<b>").append(query).append("</b> ").append("Default text:").append(content);
                }
            }
        }
        return String.valueOf(result);
    }

    public String chengPageUrl(String pageUrl, String siteUrl) {
        return pageUrl.replaceAll(siteUrl, "");
    }

    public String replaceWord(String text) {
        StringBuilder builder = new StringBuilder();
        String[] masString = text.toLowerCase().split("[^а-яА-я]+");
        for (String s : masString) {
            builder.append(s).append(" ");
        }
        return String.valueOf(builder);
    }

    public List<LemmaEntity> searchLemmasInSite(String query, String site) {
        List<LemmaEntity> searchResult = new ArrayList<>();
        List<String> words = serviceLemma.splitTextIntoWords(query);
        List<String> lemmaList = serviceLemma.createLemmaList(words);
        List<LemmaEntity> lemmaEntityListInDataBase = lemmaRepositories.findAll();
        String siteURL = java.net.URLDecoder.decode(site, StandardCharsets.UTF_8);
        for (LemmaEntity lemmaEntity : lemmaEntityListInDataBase) {
            if (lemmaEntity.getSiteEntity().getUrl().contains(siteURL)) {
                if (lemmaList.contains(lemmaEntity.getLemma())) {
                    searchResult.add(lemmaEntity);
                }
            }
        }
        return searchResult;
    }

    public List<LemmaEntity> searchLemmasAllSites(String query) {
        List<LemmaEntity> searchResult = new ArrayList<>();
        List<String> words = serviceLemma.splitTextIntoWords(query);
        List<String> lemmaList = serviceLemma.createLemmaList(words);
        List<LemmaEntity> lemmaEntityListInDataBase = lemmaRepositories.findAll();
        for (LemmaEntity lemmaEntity : lemmaEntityListInDataBase) {
            if (lemmaList.contains(lemmaEntity.getLemma())) {
                searchResult.add(lemmaEntity);
            }
        }
        return searchResult;
    }

    public TreeMap<Float, PageEntity> searchForMultipleLemmasOnPages(List<LemmaEntity> searchLemmaList) {
        searchLemmaList.sort(Comparator.comparingInt(LemmaEntity::getFrequency));
        List<IndexEntity> indexInDataBase = indexRepositories.findAll();
        List<IndexEntity> allIndexFindByLemma = new ArrayList<>();
        for (IndexEntity index : indexInDataBase) {
            if (searchLemmaList.contains(index.getLemmaEntity())) {
                allIndexFindByLemma.add(index);
            }
        }

        List<PageEntity> allPageFindByLemma = allIndexFindByLemma.stream().map(IndexEntity::getPageEntity).toList();

        Map<PageEntity, List<IndexEntity>> listIndexGroupByPage = allIndexFindByLemma.stream().collect(groupingBy(IndexEntity::getPageEntity, toList()));
        Map<LemmaEntity, List<IndexEntity>> listIndexGroupByLemma = allIndexFindByLemma.stream().collect(groupingBy(IndexEntity::getLemmaEntity, toList()));

        Map<PageEntity, List<String>> mapPageFindByStringList = new HashMap<>();
        Map<String, List<PageEntity>> mapStringFindByPageList = new HashMap<>();

        for (Map.Entry<PageEntity, List<IndexEntity>> entry : listIndexGroupByPage.entrySet()) {
            if (mapPageFindByStringList.get(entry.getKey()) == null) {
                mapPageFindByStringList.put(entry.getKey(), new ArrayList<>());
                mapPageFindByStringList.get(entry.getKey()).addAll(entry.getValue().stream().map(x -> x.getLemmaEntity().getLemma()).toList());
            } else {
                mapPageFindByStringList.get(entry.getKey()).addAll(entry.getValue().stream().map(x -> x.getLemmaEntity().getLemma()).toList());
            }
        }

        for (Map.Entry<LemmaEntity, List<IndexEntity>> entry : listIndexGroupByLemma.entrySet()) {
            if (mapStringFindByPageList.get(entry.getKey().getLemma()) == null) {
                mapStringFindByPageList.put(entry.getKey().getLemma(), new ArrayList<>());
                mapStringFindByPageList.get(entry.getKey().getLemma()).addAll(entry.getValue().stream().map(IndexEntity::getPageEntity).toList());
            } else {
                mapStringFindByPageList.get(entry.getKey().getLemma()).addAll(entry.getValue().stream().map(IndexEntity::getPageEntity).toList());
            }
        }

        List<PageEntity> pageListResult = new ArrayList<>();
        for (PageEntity page : allPageFindByLemma) {
            boolean contains = true;
            for (String entity : mapStringFindByPageList.keySet()) {
                if (!mapStringFindByPageList.get(entity).contains(page)
                        && !mapPageFindByStringList.get(page).contains(entity)) {
                    contains = false;
                }
            }
            if (contains) {
                pageListResult.add(page);
            }
        }

        Set<IndexEntity> finalResult = new HashSet<>();
        for (IndexEntity index : allIndexFindByLemma) {
            for (PageEntity page : pageListResult) {
                if (Objects.equals(index.getPageEntity().getId(), page.getId())) {
                    finalResult.add(index);
                }
            }
        }

        HashMap<PageEntity, Float> absoluteRelevanceInPage = new HashMap<>();
        for (PageEntity page : pageListResult) {
            float maxRankInPage = 0.0f;
            for (IndexEntity index : finalResult) {
                if (Objects.equals(page, index.getPageEntity())) {
                    maxRankInPage = maxRankInPage + index.getRank();
                }
            }
            absoluteRelevanceInPage.put(page, maxRankInPage);
        }
        float maxAbsoluteRelevanceInPages = 0.0f;
        for (Map.Entry<PageEntity, Float> entry : absoluteRelevanceInPage.entrySet()) {
            if (maxAbsoluteRelevanceInPages < entry.getValue()) {
                maxAbsoluteRelevanceInPages = entry.getValue();
            }
        }

        TreeMap<Float, PageEntity> relativeRelevanceInPage = new TreeMap<>(Collections.reverseOrder());
        for (Map.Entry<PageEntity, Float> entry : absoluteRelevanceInPage.entrySet()) {
            float absoluteRelevance = entry.getValue();
            float relativeRelevance = (absoluteRelevance / maxAbsoluteRelevanceInPages) * 1000;
            float result = (float) (Math.random() + Math.round(relativeRelevance));
            relativeRelevanceInPage.put(result, entry.getKey());
        }
        return relativeRelevanceInPage;
    }
}
