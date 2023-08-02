package com.evrim.mevzuat.search.api.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.repository.Highlight;
import org.springframework.data.solr.repository.Query;
import org.springframework.data.solr.repository.SolrCrudRepository;

import com.evrim.mevzuat.search.api.entity.SolrMevzuatDocument;

public interface MevzuatDocumentSearchRepository extends SolrCrudRepository<SolrMevzuatDocument, Integer> {

//	public Page<SolrMevzuatDocument> findByMevzuatOrTitleOrMainTopicOrContentOrLinkedContentOrImportantContentOrKeywords(
//			@Boost(1.0f) String mevzuat, @Boost(1.0f) String title, @Boost(1.0f) String mainTopic, 
//			@Boost(0.3f) String content, @Boost(0.25f) String linkedContent, @Boost(0.8f) String importantContent, @Boost(1.0f)String keywords, Pageable pageable);
	
	
//	@Query(value = "mevzuat:?0 OR title:?0 OR mainTopic:?0 OR keywords:?0",fields={"itemNo", "keyValue", "mevzuat","title","mainTopic","keywords"} )
//	public Page<SolrMevzuatDocument> searchForAutoComplete(String searchTerm, Pageable pageable);
//	
//	@Query(value = "mevzuat:?0 OR title:?0 OR mainTopic:?0 OR keywords:?0 OR content:=?0 OR linkedContent:?0"
//			,fields={"itemNo", "keyValue", "mevzuat","title","mainTopic","keywords","content","linkedContent"} )
//	public Page<SolrMevzuatDocument> search(String searchTerm, Pageable pageable);
	
	
	@Query(value = "(mevzuat:?0)^4 OR (title:?0)^4 OR (mainTopic:?0)^4 OR (keywords:?0)^3"
			,fields={"itemNo","keyValue", "mevzuat", "mainTopic", "title"} ,defType="edismax")
	@Highlight(prefix="<strong>",postfix="</strong>", fragsize=200,
			fields={"mevzuat","title","mainTopic","keywords",})
	public HighlightPage<SolrMevzuatDocument> searchAndHighlightForAutoComplete(String searchTerm, Pageable pageable);
	
	@Query(value = "(mevzuat:?0)^3 OR (title:?0)^3 OR (mainTopic:?0)^3 OR (keywords:?0)^2 OR (importantContent:?0)^2 OR content:=?0 OR linkedContent:?0"
			,fields={"itemNo","keyValue", "mevzuat", "mainTopic", "title", "content", "fileName"} ,defType="edismax")
	@Highlight(prefix="<strong>",postfix="</strong>", fragsize=155,
			fields={"mevzuat","title","mainTopic","keywords","importantContent","content","linkedContent"})
	public HighlightPage<SolrMevzuatDocument> searchAndHighlight(String searchTerm, Pageable pageable);
	
	
}
