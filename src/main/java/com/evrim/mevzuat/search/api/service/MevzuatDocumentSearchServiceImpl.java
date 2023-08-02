package com.evrim.mevzuat.search.api.service;

import java.util.Base64;

import org.apache.commons.lang3.StringUtils;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.stereotype.Service;

import com.evrim.common.dto.base.PageResult;
import com.evrim.common.dto.mevzuat.MevzuatSearchResult;
import com.evrim.common.logging.Loggable;
import com.evrim.mevzuat.search.api.entity.MevzuatDocument;
import com.evrim.mevzuat.search.api.entity.SolrMevzuatDocument;
import com.evrim.mevzuat.search.api.repository.MevzuatDocumentEntityRepository;
import com.evrim.mevzuat.search.api.repository.MevzuatDocumentSearchRepository;
import com.evrim.mevzuat.search.api.util.SolrResultParseUtil;

@Service
@Loggable
public class MevzuatDocumentSearchServiceImpl<T> implements MevzuatDocumentSearchService {
	
	@Autowired
	MevzuatDocumentSearchRepository searchRepository;
	
	@Autowired
	MevzuatDocumentEntityRepository mevzuatDocumentEntityRepository; 
	
	@Autowired
	SolrTemplate solrTemplate;
	
	@Override
	public String getDocumentRawContent(int itemNo) {
		MevzuatDocument doc = mevzuatDocumentEntityRepository.findOne(itemNo);
		
		if(doc != null && ! StringUtils.isEmpty(doc.getRawContent()))
			return doc.getRawContent();
			//return new String(Base64.getEncoder().encode(doc.getRawContent().getBytes()));
		else
			return null;
	}

	public PageResult<MevzuatSearchResult> searchAndHighlighForAutoComplete(String searchTerm, int page, int size) {
		String query = ClientUtils.escapeQueryChars(searchTerm);
		HighlightPage<SolrMevzuatDocument> solrResult = searchRepository.searchAndHighlightForAutoComplete(query, new PageRequest(page, size));
		return SolrResultParseUtil.convertToMevzuatSearhResultPage(solrResult);
	}
	
	public PageResult<MevzuatSearchResult> searchAndHighligh(String searchTerm, int page, int size) {
		String query = ClientUtils.escapeQueryChars(searchTerm);
		HighlightPage<SolrMevzuatDocument> solrResult = searchRepository.searchAndHighlight(query, new PageRequest(page, size));
		PageResult<MevzuatSearchResult> result = SolrResultParseUtil.convertToMevzuatSearhResultPage(solrResult);
		
//		List<Integer> itemNos = result.getResult().stream().map(m->m.getItemNo()).collect(Collectors.toList());
//		List<MevzuatDocument> docs =mevzuatDocumentEntityRepository.findByItemNoIn(itemNos);
//		Map<Integer, MevzuatDocument> docsMap = new HashMap<>();
//		if(docs != null) {
//			docs.stream().forEach(c->docsMap.put(c.getItemNo(), c));
//		}
//		
//		for(MevzuatSearchResult st:result.getResult()) {
//			MevzuatDocument doc = docsMap.get(st.getItemNo());
//			
//			if(doc != null)
//				st.setRawContent(doc.getRawContent());
//		}
		
		return result;
	}

}
