package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "page_table")
public class PageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private SiteEntity siteEntity;

    @Column(name = "path", columnDefinition = "VARCHAR(699)")
    private String path;

    @Column(name = "code")
    private Integer code;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @OneToMany(mappedBy = "pageEntity", cascade = CascadeType.ALL)
    private Set<IndexEntity> indexEntityList;
}