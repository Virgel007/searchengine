package searchengine.services.interfaces;

import searchengine.dto.search.SearchResponse;

public interface SearchService {
    SearchResponse search(String query, int offset, int limit, String site);
}
