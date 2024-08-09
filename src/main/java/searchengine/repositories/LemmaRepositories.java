package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;

@Repository
public interface LemmaRepositories extends JpaRepository<LemmaEntity, Long> {

    @Query(value = "SELECT count(*) FROM lemma_table l where l.site_id = :site_id",
            nativeQuery = true)
    int countLemmaBySiteId(@Param("site_id") long id);

    LemmaEntity findBySiteEntityAndLemma(SiteEntity siteEntity, String lemma);
}
