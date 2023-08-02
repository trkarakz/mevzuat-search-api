package com.evrim.mevzuat.search.api.repository;

import java.util.List;

import com.evrim.mevzuat.search.api.entity.MevzuatDocument;


public interface MevzuatDocumentInfoRepository {
	public List<MevzuatDocument> listMevzuatFileInfos(List<Integer> maddeNolar);
}
