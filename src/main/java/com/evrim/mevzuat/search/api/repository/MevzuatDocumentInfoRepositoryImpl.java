package com.evrim.mevzuat.search.api.repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import com.evrim.common.db.DbAgentBeanListResultMapper;
import com.evrim.common.db.DbAgentCaller;
import com.evrim.common.util.StringUtil;
import com.evrim.mevzuat.search.api.entity.MevzuatDocument;

@Repository
public class MevzuatDocumentInfoRepositoryImpl  implements MevzuatDocumentInfoRepository {
	
	@Autowired
	DbAgentCaller caller;
	
	@Value("${evrim.dbagent.url}")
	String evrimDbAgentUrl;
	
	static Map<String, String> MEVZUAT_FILE_TRANSFORM_MAP = new HashMap<String, String>() {
		{
			put("RejimMaddeNo","itemNo");
			put("Baslik","title");
			put("AnaKonu","mainTopic");
		}
	};
	
	@Override
	public List<MevzuatDocument> listMevzuatFileInfos(List<Integer> maddeNolar) {
		return caller.callDbAgent(evrimDbAgentUrl, "sp_wg_MevzuatMaddeNoBilgi",
				new DbAgentBeanListResultMapper<MevzuatDocument>(MevzuatDocument.class, MEVZUAT_FILE_TRANSFORM_MAP), StringUtil.mergeStringList(maddeNolar, ","));
	}

}
