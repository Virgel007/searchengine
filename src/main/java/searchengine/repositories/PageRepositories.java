package searchengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import searchengine.model.PageEntity;

@Repository
public interface PageRepositories extends JpaRepository<PageEntity, Long> {

    @Query(value = "SELECT count(*) FROM page_table p where p.site_id = :site_id",
            nativeQuery = true)
    int countPageBySiteId(@Param("site_id") long id);

    PageEntity findByPath(String path);
}
