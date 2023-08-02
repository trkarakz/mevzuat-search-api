package com.evrim.mevzuat.search.api.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;

import com.evrim.common.dto.base.PageResult;
import com.evrim.common.dto.mevzuat.MevzuatSearchResult;
import com.evrim.mevzuat.search.api.entity.SolrMevzuatDocument;

public class SolrResultParseUtil {
	private static final Logger LOG = LoggerFactory.getLogger(SolrResultParseUtil.class);
	
	public static Map<String, String> getHighlightMap(List<Highlight> highlights) {
		Map<String, String> result = new HashMap<>();
		
		for(Highlight hg : highlights) {
			StringBuilder snippets = new StringBuilder();
			for(String snipped: hg.getSnipplets()) {
				snippets.append(snipped + "\n");
			}
			
			result.put(hg.getField().getName(),snippets.toString());
		}
		
		return result;
	}
	
	public static PageResult<MevzuatSearchResult> convertToMevzuatSearhResultPage(HighlightPage<SolrMevzuatDocument> solrMevzuatSearhResult) {
		
		PageResult<MevzuatSearchResult> result = new PageResult<>();
		result.setRecordCount(solrMevzuatSearhResult.getTotalElements());
		List<MevzuatSearchResult> resultList = new ArrayList<>();
		
		if(solrMevzuatSearhResult.getNumberOfElements() > 0) {
			
			for(HighlightEntry<SolrMevzuatDocument>  highlightResult : solrMevzuatSearhResult.getHighlighted()) {
				MevzuatSearchResult resultItem = SolrResultParseUtil.getMevzuatSearchResult(highlightResult);
				resultList.add(resultItem);
			}
		}
		
		result.setResult(resultList);
		return result;
	}
	
	public static MevzuatSearchResult getMevzuatSearchResult(HighlightEntry<SolrMevzuatDocument> solrHighlightResult) {
		MevzuatSearchResult searchResult = new MevzuatSearchResult();
		BeanUtils.copyProperties(solrHighlightResult.getEntity(), searchResult);
		
		Map<String, String> highlightMap = getHighlightMap(solrHighlightResult.getHighlights());
		
		String titleHl = highlightMap.get("title");
		String mainTopicHl = highlightMap.get("mainTopic");
		String mevzuatHl = highlightMap.get("mevzuat");
		
		String keywordsHl = highlightMap.get("keywords");
		String importantContentHl = highlightMap.get("importantContent");
		String contentHl = highlightMap.get("content");
		String linkedContentHl = highlightMap.get("linkedContent");
		
		if(titleHl != null) searchResult.setTitle(titleHl);
		
		if(mainTopicHl != null) searchResult.setMainTopic(mainTopicHl);
		
		if(mevzuatHl != null) searchResult.setMevzuat(mevzuatHl);
		
		StringBuilder contentHighLights = new StringBuilder();
		
		if(contentHl != null) contentHighLights.append(contentHl + "\n");
		
		if(importantContentHl != null) contentHighLights.append(importantContentHl + "\n");
		
		if(linkedContentHl != null) contentHighLights.append(linkedContentHl + "\n");
		
		if(keywordsHl != null) contentHighLights.append(keywordsHl);
		
		searchResult.setHighlighted(contentHighLights.toString());
		
		if(StringUtils.isEmpty(searchResult.getHighlighted())) {
			String smallContent = solrHighlightResult.getEntity().getContent();
			
			if(smallContent != null) {
				smallContent = smallContent.substring(0, Math.min(smallContent.length(), 300));
				searchResult.setHighlighted(smallContent);
			}
		}
		
		return searchResult;
	}
}
