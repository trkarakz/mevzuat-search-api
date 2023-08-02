package com.evrim.mevzuat.search.api.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.springframework.data.solr.core.SolrTemplate;

import com.evrim.common.dto.base.PageResult;
import com.evrim.common.dto.mevzuat.MevzuatSearchResult;
import com.evrim.common.util.StringUtil;

@Deprecated
public class SolrQueryUtil {
	static final Map<String, String> queryMap = new HashMap<String, String>() {
		{
			put("qf", "mevzuat^4 title^4 mainTopic^4 keywords^3 importantContent^2 content linkedContent");
			put("defType", "edismax");
			put("fl", "itemNo,keyValue,mevzuat,mainTopic,title");
			put("hl", "true");
			put("hl.alternateField", "content");
			put("hl.maxAlternateFieldLength", "156");
			put("defType", "edismax");
			put("defType", "edismax");
		}
	};
	
	private static MapSolrParams getQuery(String terms) {
		Map<String, String> queryMap = new HashMap<>();
		queryMap.putAll(SolrQueryUtil.queryMap);
		queryMap.put("q", terms);
		
		MapSolrParams mapSolrParam = new MapSolrParams(queryMap);
		return mapSolrParam;
	}
	
	public static PageResult<MevzuatSearchResult> executeQUery(SolrTemplate solrTemplate, String terms) {
		try {
			terms = StringUtil.formatSolrSearchString(terms);
			SolrParams params = getQuery(terms);
			QueryResponse solrResult = solrTemplate.getSolrClient().query(params);
			
			PageResult<MevzuatSearchResult> result = new PageResult<>();
			result.setResult(solrResult.getBeans(MevzuatSearchResult.class));
			result.setRecordCount(solrResult.getResults().getNumFound());
			return result;
		} catch (SolrServerException | IOException e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
}
