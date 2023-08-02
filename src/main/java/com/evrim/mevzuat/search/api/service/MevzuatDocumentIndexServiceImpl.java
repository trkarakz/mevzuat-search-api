package com.evrim.mevzuat.search.api.service;

import java.io.File;
import java.io.FileInputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.transaction.Transactional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.evrim.common.dto.security.AuthResultWithCompanyDto;
import com.evrim.common.entity.Log;
import com.evrim.common.logging.Loggable;
import com.evrim.common.security.AuthenticationService;
import com.evrim.mevzuat.search.api.entity.MevzuatDocument;
import com.evrim.mevzuat.search.api.entity.SolrMevzuatDocument;
import com.evrim.mevzuat.search.api.repository.MevzuatDocumentEntityRepository;
import com.evrim.mevzuat.search.api.repository.MevzuatDocumentInfoRepository;
import com.evrim.mevzuat.search.api.repository.MevzuatDocumentSearchRepository;
import com.evrim.mevzuat.search.api.task.MevzuatDocumentConstructorTask;
import com.evrim.mevzuat.search.api.task.MevzuatDocumentConstructorTaskException;

@Service
@Loggable
public class MevzuatDocumentIndexServiceImpl implements MevzuatDocumentIndexService {
	
	private static final Logger LOG = LoggerFactory.getLogger(MevzuatDocumentIndexServiceImpl.class);
	
	private static final int INDEX_BLOCK_SIZE = 100;
	
	@Autowired
	MevzuatFileListService fileListService;
	
	@Autowired
	MevzuatDocumentInfoRepository fileInfoRepository;
	
	@Autowired
	MevzuatDocumentEntityRepository fileRepository;
	
	@Autowired
	LogService logService;
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Autowired
	SolrOperations solrTemplate;
	
	@Autowired
	MevzuatDocumentSearchRepository searchRepository;
	
	Pattern mevzuatFileNamePattern = Pattern.compile("([0-9]{6,}).(html|mht)$", Pattern.CASE_INSENSITIVE);
	
	@Transactional
	public void indexSingleMevzuatFile(int itemNo) {
		List<File> singleFileList = new ArrayList<File>() {
			{
				add(fileListService.getMevzuatFile(itemNo));
			}
		};
		
		AuthResultWithCompanyDto authResult = authenticationService.getAuthenticatedUser();
		String userInfo = authResult==null?"mevzuat-search-api":authResult.getAuthResult().getEmail();
		
		Integer indexerJobId = fileRepository.getMaxJobId();
		indexerJobId = indexerJobId == null ? 1 : indexerJobId + 1;
		
		List<SolrMevzuatDocument> blockDocument = constructMevzuatDocuments(indexerJobId, singleFileList);
		
		if(! CollectionUtils.isEmpty(blockDocument)) {
			SolrMevzuatDocument solrIndexedDocument = searchRepository.save(blockDocument.get(0));
			saveIndexedDocumentInfoToDb(indexerJobId, new ArrayList<SolrMevzuatDocument>() {{ add(solrIndexedDocument); }}, userInfo);
		} else {
			throw new RuntimeException("Document index error...");
		}
	}
	
	@Async("taskExecutor")
	public void reIndexAllDocuments() {
		AuthResultWithCompanyDto authResult = authenticationService.getAuthenticatedUser();
		String userInfo = authResult==null?"mevzuat-search-api":authResult.getAuthResult().getEmail();
		
		Integer indexerJobId = fileRepository.getMaxJobId();
		indexerJobId = indexerJobId == null ? 1 : indexerJobId + 1;
		
		// list all files from mevzuat_files directory
		List<File> files = fileListService.listAllMevzuatFiles();
		
		if(CollectionUtils.isEmpty(files)) {
			throw new RuntimeException("No files found for index");
		}
		
		// searchRepository.deleteAll();
		
		int i=0;
		while(i < files.size()) {
			int endIndex = Math.min(i+INDEX_BLOCK_SIZE, files.size());
			
			List<SolrMevzuatDocument> blockDocument = constructMevzuatDocuments(indexerJobId, files.subList(i, endIndex));
			List<SolrMevzuatDocument> successfulIndexedDocuments = indexDocumentBlock(indexerJobId, blockDocument, userInfo);
			saveIndexedDocumentInfoToDb(indexerJobId, successfulIndexedDocuments, userInfo);
			
			solrTemplate.commit();
			fileRepository.flush();
			i = endIndex;
		}
	}
	
	private void saveIndexedDocumentInfoToDb(int indexerJobId, List<SolrMevzuatDocument> indexedDocuments, String userInfo) {
		
		for(SolrMevzuatDocument solrDocument:indexedDocuments) {
			try {
				MevzuatDocument doc = constructMevzuatDocumentUsingSolrDocument(indexerJobId, solrDocument, null);
				fileRepository.save(doc);
			} catch (Exception e) {
				LOG.error("save document info error, Document:{},  Exception:{}", solrDocument, e.getMessage());
				
				// write error to Log db
				writeLogToDb(userInfo, "MevzuatDocumentIndexError#saveIndexedDocumentInfoToDb","Document:"+solrDocument.getItemNo()
					+", Exception:"+ e.getMessage());
				
			}
		}
		
		fileRepository.flush();
	}
	
	@Loggable
	private List<SolrMevzuatDocument> indexDocumentBlock(int indexerJobId, List<SolrMevzuatDocument> parsedDocuments, String userInfo) {

		List<SolrMevzuatDocument> successfulIndexedDocuments = new ArrayList<>();
		 
		for(SolrMevzuatDocument doc:parsedDocuments) {
			
			try {
				solrTemplate.saveBean(doc);
				successfulIndexedDocuments.add(doc);
			} catch (Exception e) {
				LOG.error("Mevzuat document can not indexed, Doc:{}", doc);
				
				// write error to Log db
				writeLogToDb(userInfo, "MevzuatDocumentIndexError#indexDocumentBlock","Document:"+doc.getItemNo()
					+", Exception:"+ e.getMessage());
				
				// write error to MevzuatDocument
				MevzuatDocument mevzuatDocument = constructMevzuatDocumentUsingSolrDocument(indexerJobId, doc, e.getMessage());
				fileRepository.save(mevzuatDocument);
			}			
		}
		
		fileRepository.flush();
		return successfulIndexedDocuments;
	}

	@Override
	public List<SolrMevzuatDocument> constructMevzuatDocuments(int indexerJobId, List<File> mevzuatFiles) {
			
		Map<Integer, File> mevzuatFileMap = new HashMap<>();
		List<Integer> mevzuatFileNos = new ArrayList<>();
		
		AuthResultWithCompanyDto authResult = authenticationService.getAuthenticatedUser();
		String userEmail = authResult==null?"mevzuat-search-api":authResult.getAuthResult().getEmail();
		
		for(File file : mevzuatFiles) {
			try {
				Matcher matcher = mevzuatFileNamePattern.matcher(file.getName());
				Integer maddeNo = Integer.parseInt(matcher.matches()?matcher.group(1):"not_found");
				mevzuatFileNos.add(maddeNo);
				mevzuatFileMap.put(maddeNo, file);
			} catch (Exception e) {
				LOG.error("!!! itemNo can not fetched from filename, warning !!! this file will be ignored file:{}, exception:{}",file.getAbsolutePath(), e.getMessage());

				writeLogToDb(userEmail, "MevzuatDocumentIndexError#constructMevzuatDocuments",
						"itemNo can not fetched from filename, File:"+file.getName()+", Exception:"+e.getMessage());
			}
		}
		
		Map<Integer, MevzuatDocument> mevzuatFileInfoMap = new HashMap<>();
		Map<Integer, MevzuatDocument> mevzuatFileExtraSearchInfoMap = new HashMap<>();

		// find mevzuat info from evrim db, call sp and get title, mainTopic, keyValue and mevzuat
		int i=0;
		while(i < mevzuatFileNos.size()) {
			int endIndex = Math.min(i+500, mevzuatFileNos.size());
			
			// find mevzuat info from evrim db, call sp and get title, mainTopic, keyValue and mevzuat
			List<MevzuatDocument> listMevzuatFileInfosFromEvrimDb = fileInfoRepository.listMevzuatFileInfos(mevzuatFileNos.subList(i, endIndex));

			// put info to the map
			for(MevzuatDocument fileInfo:listMevzuatFileInfosFromEvrimDb) {
				mevzuatFileInfoMap.put(fileInfo.getItemNo(), fileInfo);
			}
			
			i = endIndex;
		}
		
		// find mevzuat extra info (keyword, importantContent, lastFileUpdateDate) from postgresql, and put it to the map
		List<MevzuatDocument> mevzuatFileExtraSearchInfo = fileRepository.findByItemNoIn(mevzuatFileNos);
		for(MevzuatDocument fileInfo:mevzuatFileExtraSearchInfo) {
			mevzuatFileExtraSearchInfoMap.put(fileInfo.getItemNo(), fileInfo);
		}
		
		// Mevzuat document executor, and completion service for MevzuatDocumentConstructor task
		ExecutorService executorService = Executors.newFixedThreadPool(Math.min(20, mevzuatFileNos.size()));
		CompletionService<SolrMevzuatDocument> taskCompletionService = new ExecutorCompletionService<SolrMevzuatDocument>(executorService);
		
		// create a MevzuatDocmentConstructorTask for each files, and put tasks to the taskCompletionService
		for(Integer itemNo : mevzuatFileNos) {
			taskCompletionService.submit(new MevzuatDocumentConstructorTask(logService, mevzuatFileInfoMap.get(itemNo),
					mevzuatFileExtraSearchInfoMap.get(itemNo), mevzuatFileMap.get(itemNo)));
		}
		
		List<SolrMevzuatDocument> result = new ArrayList<>();
		
		// take finished task from taskCompletionService
		for(i=0;i<mevzuatFileNos.size();i++) {
			try {
				SolrMevzuatDocument taskResult = taskCompletionService.take().get();
				result.add(taskResult);
			} catch (MevzuatDocumentConstructorTaskException taskException) {
				// write error log to the file
				LOG.error("Mevzuat document construct task error, e:",ExceptionUtils.getRootCauseMessage(taskException));
				
				// write error log to Log table
				writeLogToDb(userEmail, "MevzuatDocumentIndexError#documentConstructorTask", ExceptionUtils.getRootCauseMessage(taskException));
				
				// write error log to MevzuatDocument table...
				MevzuatDocument doc = constructMevzuatDocumentUsingTaskException(taskException);
				doc.setIndexerJobId(indexerJobId);
				
				fileRepository.save(doc);
			} catch (Exception e) {
				// TODO: ...
				LOG.error("Mevzuat document construct task error, e:",ExceptionUtils.getRootCauseMessage(e));
				writeLogToDb(userEmail, "MevzuatDocumentIndexError#documentConstructorTask", ExceptionUtils.getRootCauseMessage(e));
			}
		}
		
		try {
			fileRepository.flush();
		} catch (Exception e) {
			LOG.error("File repository flush error, e:{}", e.getMessage());
		}
		
		try {
			executorService.shutdownNow();
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}
		
		return result;
	}
	
	private void writeLogToDb(String userInfo, String methodInfo, String error) {
		String errorMessage = error.length() >= 500 ? error.substring(0, 496) + "..." : error; 
		Log mevzuatFileParseLog = new Log(userInfo, "mevzuat-search-api", methodInfo, "...", errorMessage, new Date(), "", -1);
		logService.save(mevzuatFileParseLog);
	}
	
	private MevzuatDocument constructMevzuatDocumentUsingSolrDocument(int indexerJobId, SolrMevzuatDocument solrDocument, String error) {
		MevzuatDocument doc = fileRepository.findOne(solrDocument.getItemNo());
		
		if(doc == null) {
			doc = new MevzuatDocument();
		}

		doc.setItemNo(solrDocument.getItemNo());
		doc.setMainTopic(solrDocument.getMainTopic());
		doc.setMevzuat(solrDocument.getMevzuat());
		doc.setTitle(solrDocument.getTitle());
		doc.setKeyValue(solrDocument.getKeyValue());
		
		doc.setIndexerJobId(indexerJobId);
		doc.setRawContent(solrDocument.getRawContent());
		doc.setIndexError(error);
		
		doc.setLastFileUpdateDate(solrDocument.getLastFileUpdateDate());
		doc.setLastIndexDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		
		return doc;
	}
	
	private MevzuatDocument constructMevzuatDocumentUsingTaskException(MevzuatDocumentConstructorTaskException e) {
		MevzuatDocument doc = fileRepository.findOne(e.getTask().getEvrimDbFileInfo().getItemNo());
		
		if(doc == null) {
			doc = new MevzuatDocument();
			BeanUtils.copyProperties(e.getTask().getEvrimDbFileInfo(), doc);
		}
		
		try(FileInputStream fis = new FileInputStream(e.getTask().getMevzuatFile())) {
			doc.setRawContent(IOUtils.toString(fis));
		} catch (Exception ex) {
			LOG.error("File content read exception, e:", e.getMessage());
			doc.setRawContent("File content read exception, e:"+e.getMessage());
		}

		doc.setIndexError(e.getMessage());
		doc.setLastFileUpdateDate(new Timestamp(e.getTask().getMevzuatFile().lastModified()));
		doc.setLastIndexDate(new Timestamp(Calendar.getInstance().getTimeInMillis()));
		
		return doc;
	}
	
}
