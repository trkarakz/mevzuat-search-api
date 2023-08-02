package com.evrim.mevzuat.search.api.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.evrim.common.dto.base.PageResult;
import com.evrim.common.dto.mevzuat.MevzuatSearchResult;
import com.evrim.common.logging.Loggable;
import com.evrim.mevzuat.search.api.repository.MevzuatDocumentSearchRepository;
import com.evrim.mevzuat.search.api.service.MevzuatDocumentIndexService;
import com.evrim.mevzuat.search.api.service.MevzuatDocumentSearchService;
import com.evrim.mevzuat.search.api.service.MevzuatFileListService;

@RestController
@RequestMapping(value="/mevzuatDocuments", produces = MediaType.APPLICATION_JSON_VALUE)
@Loggable
public class MevzuatSearchApi {
	
	@Autowired
	MevzuatFileListService fileListService;
	
	@Autowired
	MevzuatDocumentSearchRepository searchRepository;
	
	@Autowired
	SolrOperations solrTemplate;
	
	@Autowired
	MevzuatDocumentIndexService mevzuatDocumentService;
	
	@Autowired
	MevzuatDocumentSearchService mevzuatDocumentSearchService;
	
	@RequestMapping(value="/reIndexAll", method=RequestMethod.POST)
	@PreAuthorize("hasAuthority('AUTH_SUPER_ADMIN')")
	public boolean reIndexAllDocuments() {
		mevzuatDocumentService.reIndexAllDocuments();
		return true;
	}
	
	@RequestMapping(value="/index/{itemNo}", method=RequestMethod.POST)
	@PreAuthorize("hasAuthority('AUTH_SUPER_ADMIN')")
	public boolean indexSingleMevzuatFile(@PathVariable("itemNo") int itemNo) {
		mevzuatDocumentService.indexSingleMevzuatFile(itemNo);
		return true;
	}
	
	@RequestMapping(value="/{itemNo}/rawContent", method=RequestMethod.GET)
	public String getRawContent(@PathVariable("itemNo") int itemNo) {
		return mevzuatDocumentSearchService.getDocumentRawContent(itemNo);
	}
	
	@RequestMapping(value="/search", method=RequestMethod.GET)
	public PageResult<MevzuatSearchResult> search(
			@RequestParam(name = "q", required = true) String searchTerm
			,@RequestParam(name = "page", defaultValue="0") int page
			,@RequestParam(name = "size", defaultValue="10") int size
			) {
		
		return mevzuatDocumentSearchService.searchAndHighligh(searchTerm, page, size);
	}
	
	@RequestMapping(value="/search/autocomplete", method=RequestMethod.GET)
	public PageResult<MevzuatSearchResult> autocomplete(
			@RequestParam(name = "q", required = true) String searchTerm
			,@RequestParam(name = "page", defaultValue="0") int page
			,@RequestParam(name = "size", defaultValue="10") int size
			) {
		
		return mevzuatDocumentSearchService.searchAndHighlighForAutoComplete(searchTerm, page, size);
	}
}
