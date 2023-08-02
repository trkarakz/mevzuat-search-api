package com.evrim.mevzuat.search.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.evrim.common.entity.Log;
import com.evrim.mevzuat.search.api.repository.LogRepository;

@Service
@Transactional
public class LogServiceImpl implements LogService {

	private static final Logger LOG = LoggerFactory.getLogger(LogServiceImpl.class);
	
	LogRepository logRepository;
	
	@Autowired
	public LogServiceImpl(LogRepository logRepository) {
		super();
		this.logRepository = logRepository;
	}

	public void save(Log log) {
		try {
			LOG.debug("Logging an action to DB, log entity : {}", log);
			logRepository.saveAndFlush(log);
		} catch (Exception e) {
			LOG.error("An error occured during saving log to DB :{}", e);
		}
	}
	
}
