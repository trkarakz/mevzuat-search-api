package com.evrim.mevzuat.search.api.task;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.evrim.mevzuat.search.api.service.MevzuatDocumentIndexService;

@Component
public class ScheduledTasks {
	private static final Logger LOG = LoggerFactory.getLogger(ScheduledTasks.class);
	
	@Autowired
	MevzuatDocumentIndexService mevzuatDocumentIndexerService;
	
	@Scheduled(cron="0 15 2 * * *")
	public void checkMevzuatFilesAndIndex() {
		LOG.debug("Indexer job started at:{}",LocalDateTime.now());
		mevzuatDocumentIndexerService.reIndexAllDocuments();
		LOG.debug("Indexer job finished at:{}",LocalDateTime.now());
	}
}
