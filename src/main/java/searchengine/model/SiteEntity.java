package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.util.Date;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "site_table")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status",
            columnDefinition = "ENUM('INDEXING', 'INDEXED', 'FAILED')")
    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    @Column(name = "status_time")
    private Date statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "url")
    private String url;

    @Column(name = "name")
    private String name;

    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL)
    private Set<PageEntity> pageEntityList;

    @OneToMany(mappedBy = "siteEntity", cascade = CascadeType.ALL)
    private Set<LemmaEntity> lemmaEntityList;

    public SiteEntity setStatus(Status status) {
        this.statusTime = new Date();
        this.status = status;
        return this;
    }
}
