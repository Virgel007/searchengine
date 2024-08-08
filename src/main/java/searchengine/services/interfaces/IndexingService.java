package searchengine.services.interfaces;


import searchengine.dto.Indexing.IndexingResponse;

public interface IndexingService {
    IndexingResponse startIndexing();

    IndexingResponse stopIndexing();

    IndexingResponse indexPage(String page);

    IndexingResponse deleteDataBase();
}