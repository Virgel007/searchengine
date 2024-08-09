package searchengine.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import searchengine.dto.Indexing.IndexingResponse;
import searchengine.dto.search.SearchResponse;
import searchengine.dto.statistics.StatisticsResponse;
import searchengine.services.interfaces.IndexingService;
import searchengine.services.interfaces.SearchService;
import searchengine.services.interfaces.StatisticsService;

@RestController
@RequestMapping("/api")
public class ApiController {
    private final IndexingService indexingService;
    private final StatisticsService statisticsService;
    private final SearchService searchService;

    public ApiController(StatisticsService statisticsService, IndexingService indexingService, SearchService searchService) {
        this.statisticsService = statisticsService;
        this.indexingService = indexingService;
        this.searchService = searchService;
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<IndexingResponse> startIndexing() {
        return ResponseEntity.ok(indexingService.startIndexing());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<IndexingResponse> stopIndexing() {
        return ResponseEntity.ok(indexingService.stopIndexing());
    }

    @PostMapping("/deleteDataBase")
    public ResponseEntity<IndexingResponse> deleteDataBase() {
        return ResponseEntity.ok(indexingService.deleteDataBase());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestBody String url) {
        return ResponseEntity.ok(indexingService.indexPage(url));

    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/search")
    public ResponseEntity<SearchResponse> search(@RequestParam String query, int offset, int limit, String site) {
        return ResponseEntity.ok(searchService.search(query, offset, limit, site));
    }
}