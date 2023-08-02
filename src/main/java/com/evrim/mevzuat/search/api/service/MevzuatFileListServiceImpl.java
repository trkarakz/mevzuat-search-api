package com.evrim.mevzuat.search.api.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.evrim.common.logging.Loggable;
import com.evrim.common.security.AuthenticationService;
import com.evrim.mevzuat.search.api.repository.MevzuatDocumentEntityRepository;
import com.evrim.mevzuat.search.api.repository.MevzuatDocumentInfoRepository;

@Service
@Loggable
public class MevzuatFileListServiceImpl implements MevzuatFileListService {
	private static final Logger LOG = LoggerFactory.getLogger(MevzuatFileListServiceImpl.class);
	
	@Autowired
	MevzuatDocumentInfoRepository fileInfoRepository;
	
	@Autowired
	MevzuatDocumentEntityRepository fileRepository;
	
	@Autowired
	LogService logService;
	
	@Autowired
	AuthenticationService authenticationService;
	
	@Value("${mevzuat.files.dir}")
	String mevzuatFilesDir;
	
	Pattern mevzuatFileNamePattern = Pattern.compile("([0-9]{6,}).(html|mht)$", Pattern.CASE_INSENSITIVE);
	
	@Override
	public List<File> listAllMevzuatFiles() {
		List<File> files = new ArrayList<>();
		File mevzuatDir = new File(mevzuatFilesDir);
		
		try {
			for(File file : mevzuatDir.listFiles()) {
				if(file.isFile() && mevzuatFileNamePattern.matcher(file.getName().toString()).matches()) {
					files.add(file);
	        	}
			}
		
		} catch (Exception ex) {
        	LOG.error(ex.getMessage());
        }
        
        return files;
	}

	@Override
	public List<File> listChangedMevzuatFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public File getMevzuatFile(int itemNo) {
		File file = new File(mevzuatFilesDir + File.separator + itemNo + ".html");
		
		if (file.exists()) {
			return file;
		} else {
			file = new File(mevzuatFilesDir + File.separator + itemNo + ".mht");
			
			if (file.exists()) {
				return file;
			} else {
				throw new RuntimeException("Mevzuat File for ItemNo:"+itemNo + " does not exist");
			}
		}
	}
	
}
