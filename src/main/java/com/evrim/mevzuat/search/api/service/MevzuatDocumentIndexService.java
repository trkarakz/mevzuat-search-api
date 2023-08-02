package com.evrim.mevzuat.search.api.service;

import java.io.File;
import java.util.List;

import com.evrim.mevzuat.search.api.entity.SolrMevzuatDocument;

public interface MevzuatDocumentIndexService {
	public void reIndexAllDocuments();
	public void indexSingleMevzuatFile(int itemNo);
	public List<SolrMevzuatDocument> constructMevzuatDocuments(int indexerJobId, List<File> mevzuatFiles);
}
