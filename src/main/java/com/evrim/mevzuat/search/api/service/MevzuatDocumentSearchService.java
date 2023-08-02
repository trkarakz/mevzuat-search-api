package com.evrim.mevzuat.search.api.service;

import com.evrim.common.dto.base.PageResult;
import com.evrim.common.dto.mevzuat.MevzuatSearchResult;

public interface MevzuatDocumentSearchService {
	public String getDocumentRawContent(int itemNo);
	public PageResult<MevzuatSearchResult> searchAndHighlighForAutoComplete(String searchTerm, int page, int size);
	public PageResult<MevzuatSearchResult> searchAndHighligh(String searchTerm, int page, int size);
}
