package searchengine.services;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ServiceLemma {

    public HashMap<String, Integer> getLemmasInText(StringBuilder doc) {
        HashMap<String, Integer> lemmaMap = new HashMap<>();
        String noHtmlTagsTexts = toCleanCodeOfWebPagesFromHtmlTags(doc);
        List<String> texts = new ArrayList<>(splitTextIntoWords(noHtmlTagsTexts));
        List<String> lemmaList = new ArrayList<>(createLemmaList(texts));
        lemmaList.forEach(l -> {
            lemmaMap.put(l, 0);
        });

        for (Map.Entry<String, Integer> entry : lemmaMap.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();

            for (String lemma : lemmaList) {
                if (!lemma.equals(key)) {
                    continue;
                } else {
                    value++;
                }
                lemmaMap.put(lemma, value);
            }
        }
        return lemmaMap;
    }

    public String toCleanCodeOfWebPagesFromHtmlTags(StringBuilder doc) {
        String cleanText = String.valueOf(doc);
        cleanText = Jsoup.parse(cleanText).text();
        return cleanText;
    }

    public List<String> splitTextIntoWords(String text) {
        String[] wordsRussian = text.toLowerCase().split("[^а-яА-я]+");
        List<String> stringList = new ArrayList<>();
        for (String word : wordsRussian) {
            if (!word.isEmpty()) {
                stringList.add(word);
            }
        }
        return stringList;
    }

    public List<String> createLemmaList(List<String> texts) {
        List<String> lemmaList = new ArrayList<>();
        try {
            LuceneMorphology luceneMorph = new RussianLuceneMorphology();
            for (String text : texts) {
                List<String> morphInfoCleaning = new ArrayList<>();
                List<String> normalFormsCleaning = new ArrayList<>();
                List<String> normalTexts = luceneMorph.getNormalForms(text);
                List<String> morphTexts = luceneMorph.getMorphInfo(text);
                for (String morph : morphTexts) {
                    if (!(morph.contains("|o МЕЖД")
                            || morph.contains("|l ПРЕДЛ")
                            || morph.contains("|n СОЮЗ")
                            || morph.contains("|p ЧАСТ"))
                    ) {
                        morphInfoCleaning.add(morph);
                    }
                }
                for (String normal : normalTexts) {
                    for (String morph : morphInfoCleaning) {
                        if (morph.startsWith(normal)) {
                            normalFormsCleaning.add(normal);
                        }
                    }
                }
                lemmaList.addAll(normalFormsCleaning);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return lemmaList;
    }
}
