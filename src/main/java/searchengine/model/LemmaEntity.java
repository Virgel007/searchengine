package searchengine.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "lemma_table")
public class LemmaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private SiteEntity siteEntity;

    @Column(name = "lemma")
    private String lemma;

    @Column(name = "frequency")
    private Integer frequency;

    @OneToMany(mappedBy = "lemmaEntity", cascade = CascadeType.ALL)
    private Set<IndexEntity> indexEntityList;
}

