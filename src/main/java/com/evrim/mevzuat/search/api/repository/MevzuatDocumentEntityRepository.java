package com.evrim.mevzuat.search.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.evrim.mevzuat.search.api.entity.MevzuatDocument;

@Repository
public interface MevzuatDocumentEntityRepository extends JpaRepository<MevzuatDocument, Integer> {
	public List<MevzuatDocument> findByItemNoIn(List<Integer> itemNo);
	
	@Query("SELECT max(t.indexerJobId) FROM #{#entityName} t")
	public Integer getMaxJobId();
}
