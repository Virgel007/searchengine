create table index_table (id bigint not null AUTO_INCREMENT, lemma_id bigint NOT NULL, page_id bigint NOT NULL, rank_lemma float NOT NULL, primary key (id)) engine=InnoDB;
create table lemma_table (id bigint not null AUTO_INCREMENT, site_id bigint NOT NULL, lemma varchar(255) NOT NULL, frequency integer NOT NULL, primary key (id)) engine=InnoDB;
CREATE INDEX lemma_index ON lemma_table (lemma) USING BTREE;
create table page_table (id bigint not null AUTO_INCREMENT, site_id bigint NOT NULL, path VARCHAR(699) NOT NULL, code integer NOT NULL, content LONGTEXT NOT NULL, primary key (id)) engine=InnoDB;
CREATE INDEX path_index ON page_table (path) USING BTREE;
create table site_table (id bigint not null AUTO_INCREMENT, status ENUM('INDEXING', 'INDEXED', 'FAILED') NOT NULL, status_time datetime(6) NOT NULL, last_error TEXT, url varchar(255) NOT NULL, name varchar(255) NOT NULL, primary key (id)) engine=InnoDB;
alter table index_table add constraint index54612 foreign key (lemma_id) references lemma_table (id);
alter table index_table add constraint index45764 foreign key (page_id) references page_table (id);
alter table lemma_table add constraint lemma81273 foreign key (site_id) references site_table (id);
alter table page_table add constraint page52673 foreign key (site_id) references site_table (id);