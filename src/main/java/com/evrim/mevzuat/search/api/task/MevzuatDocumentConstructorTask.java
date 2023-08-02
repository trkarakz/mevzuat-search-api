package com.evrim.mevzuat.search.api.task;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

import com.evrim.common.entity.Log;
import com.evrim.common.util.StringUtil;
import com.evrim.mevzuat.search.api.entity.MevzuatDocument;
import com.evrim.mevzuat.search.api.entity.SolrMevzuatDocument;
import com.evrim.mevzuat.search.api.service.LogService;
import com.evrim.mevzuat.search.api.util.DocumentParseUtil;

public class MevzuatDocumentConstructorTask implements Callable<SolrMevzuatDocument> {
	
	private static final Logger LOG = LoggerFactory.getLogger(MevzuatDocumentConstructorTask.class);
	
	static Pattern patternExternalLink = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
	static Pattern patternFileName = Pattern.compile("([0-9]{6,}).(html|mht)[^\"]*", Pattern.CASE_INSENSITIVE);
	static Pattern patternDummyInnerFile = Pattern.compile("_(dosyalar|files)/[^\"]+", Pattern.CASE_INSENSITIVE);
	static Pattern patternLocalDocument = Pattern.compile("^#[^\"]*");
	
	MevzuatDocument evrimDbFileInfo;
	MevzuatDocument extraSearchIfo;
	File mevzuatFile;
	LogService logService;
	
	public MevzuatDocumentConstructorTask(LogService logService, MevzuatDocument evrimDbFileInfo, MevzuatDocument extraSearchIfo, File mevzuatFile) {
		this.logService = logService;
		this.evrimDbFileInfo = evrimDbFileInfo;
		this.extraSearchIfo = extraSearchIfo;
		this.mevzuatFile = mevzuatFile;
	}

	@Override
	public SolrMevzuatDocument call() throws Exception {
		long startTime = Calendar.getInstance().getTimeInMillis();
		
		try {
			Thread.currentThread().setName("MevzuatDocumentConstructorThread - "+mevzuatFile.getName());
			
			LOG.debug("MevzuatDocumentConstructorTask started, File:{}"+mevzuatFile.getName());
			SolrMevzuatDocument result = new SolrMevzuatDocument();
			
			result.setItemNo(evrimDbFileInfo.getItemNo());
			result.setKeyValue(evrimDbFileInfo.getKeyValue());
			result.setMainTopic(evrimDbFileInfo.getMainTopic());
			result.setTitle(evrimDbFileInfo.getTitle());
			result.setMevzuat(evrimDbFileInfo.getMevzuat());
			result.setLastFileUpdateDate(new Timestamp(mevzuatFile.lastModified()));
			result.setFileName(mevzuatFile.getName());
			
			crawlFile(result);
	
			result.setImportantContent(extraSearchIfo != null ? extraSearchIfo.getImportantContent() : null);
			result.setKeywords(extraSearchIfo != null ? extraSearchIfo.getKeywords() : null);
			
			LOG.debug("MevzuatDocumentConstructorTask finished, File:{}, Duration:{}", mevzuatFile.getName(), Calendar.getInstance().getTimeInMillis() - startTime);
			return result;
		} catch (Exception e) {
			LOG.debug("MevzuatDocumentConstructorTask Error, File:{}, Error:{}, Duration:{}", mevzuatFile.getName(),
				e.getMessage(), Calendar.getInstance().getTimeInMillis() - startTime);
			
			throw new MevzuatDocumentConstructorTaskException(this, e);
		}
	}
	
	public void crawlFile(SolrMevzuatDocument result) {
		try {
			Parser parser = new AutoDetectParser();
			Metadata metadata = new Metadata();
			
			// Extract raw content
			try(FileInputStream fis = new FileInputStream(mevzuatFile)) {
				result.setRawContent(IOUtils.toString(fis));
			}
						
			// Extract content
			try(FileInputStream fis = new FileInputStream(mevzuatFile)) {
				ContentHandler contenthandler = new BodyContentHandler(300000);
				parser.parse(fis, contenthandler, metadata, new ParseContext());
				String content = StringUtil.removeExtraBlankChars(contenthandler.toString());
				
				result.setContent(content);
			}
			
			// Extract links, and crawl links' content
			try(FileInputStream fis = new FileInputStream(mevzuatFile)) {
				LinkContentHandler linkContentHandler = new LinkContentHandler();
				parser.parse(fis, linkContentHandler, metadata, new ParseContext());
			
				List<Link> links=linkContentHandler.getLinks();
				
				crawlFileLinks(result, links);
			}
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public void crawlFileLinks(SolrMevzuatDocument result, List<Link> links) {
		List<String> realLinks = findRealLinks(links);
		
		if(CollectionUtils.isEmpty(realLinks)) {
			LOG.debug("No real links found for document : {}", mevzuatFile.getName());
		} else {
			StringBuilder linkedContent = new StringBuilder();
			for(String realLink:realLinks) {
				try {
					String lc = fetchLinkContent(realLink);
					linkedContent.append(lc + "\n\n");
				} catch (Exception e) {
					LOG.error("Linked content fetch error, Document:{}, Error:{}", result.getItemNo(),e.getMessage());
					writeLogToDb("mevzuat-file-api", "MevzuatDocumentIndexError#crawlFileLinks", ExceptionUtils.getRootCauseMessage(e));
				}
			}
			
			result.setLinkedContent(linkedContent.toString());
		}
	}
	
	
	private String fetchLinkContent(String link) {
		try {
			Parser parser = new AutoDetectParser();
			Metadata metadata = new Metadata();
			
			if(patternExternalLink.matcher(link).matches()) {
				URL url = new URL(link);
				
				URLConnection conn = url.openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(60000);
				
				try(InputStream urlStream = conn.getInputStream()) {
					ContentHandler contenthandler = new BodyContentHandler(300000);
					parser.parse(urlStream, contenthandler, metadata, new ParseContext());
					String content = StringUtil.removeExtraBlankChars(contenthandler.toString());
					
					return content;
				} catch (Exception e) {
					throw new RuntimeException("Mevzuat Document link parse error, MevzuatFile:"+mevzuatFile.getName()+", Link:"+link);
				}
				
			} else {
				
				try(FileInputStream fis = new FileInputStream(mevzuatFile.getParentFile().getAbsolutePath() + File.separator + link)) {
					ContentHandler contenthandler = new BodyContentHandler(300000);
					parser.parse(fis, contenthandler, metadata, new ParseContext());
					String content = StringUtil.removeExtraBlankChars(contenthandler.toString());
					
					return content;
				} catch (Exception e) {
					throw new RuntimeException("Mevzuat Document link parse error, MevzuatFile:"+mevzuatFile.getName()+", Link:"+link);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException("Mevzuat Document link error, MevzuatFile:"+mevzuatFile.getName()+", Link:"+link);
		}
	}
	
	/**
	 * Finds real links
	 * 		Not inner location links ex: #mevzuat32
	 *  	Not dummy links ex: 100120_dosyalar/filelist.xml
	 *   	Not another mevzuat file link like 100102.html
	 *   	Not empty links
	 *   
	 * @param links (tika extracted links)
	 * @return
	 */
	private List<String> findRealLinks(List<Link> links) {
		if(! CollectionUtils.isEmpty(links)) {
			List<String> result = new ArrayList<>();

			Iterator<Link> i=links.iterator();
			
			while(i.hasNext()){
				Link link = i.next();

				if(link != null) {
					if(StringUtils.isBlank(link.getUri())) {
						//LOG.debug("Invalid Link, link is EMPTY, File:{}", mevzuatFile.getName());
					} else if(patternExternalLink.matcher(link.getUri()).matches()) {
						//LOG.debug("External Link found File:{}, Link:{}", mevzuatFile.getName(), link.getUri());
						result.add(link.getUri());
					} else if(patternFileName.matcher(link.getUri()).find()) {
						//LOG.debug("Invalid Link, link is another Mevzuat, File:{}, Link:{}", mevzuatFile.getName(), link.getUri());
					} else if(patternDummyInnerFile.matcher(link.getUri()).find()) {
						//LOG.debug("Invalid Link, link is DUMMY inner Link, File:{}, Link:{}", mevzuatFile.getName(), link.getUri());
					} else if(patternLocalDocument.matcher(link.getUri()).matches()) {
						//LOG.debug("Invalid Link, link is local location, File:{}, Link:{}", mevzuatFile.getName(), link.getUri());
					} else  {
						//LOG.debug("Internal Link found File:{}, Link:{}", mevzuatFile.getName(), link.getUri());
						result.add(link.getUri());
					}
				}
		    }
			
			return result;
		} else {
			return null;
		}
	}

	public MevzuatDocument getEvrimDbFileInfo() {
		return evrimDbFileInfo;
	}

	public void setEvrimDbFileInfo(MevzuatDocument evrimDbFileInfo) {
		this.evrimDbFileInfo = evrimDbFileInfo;
	}

	public MevzuatDocument getExtraSearchIfo() {
		return extraSearchIfo;
	}

	public void setExtraSearchIfo(MevzuatDocument extraSearchIfo) {
		this.extraSearchIfo = extraSearchIfo;
	}

	public File getMevzuatFile() {
		return mevzuatFile;
	}

	public void setMevzuatFile(File mevzuatFile) {
		this.mevzuatFile = mevzuatFile;
	}
	
	private void writeLogToDb(String userInfo, String methodInfo, String error) {
		String errorMessage = error.length() >= 500 ? error.substring(0, 496) + "..." : error; 
		Log mevzuatFileParseLog = new Log(userInfo, "mevzuat-search-api", methodInfo, "...", errorMessage, new Date(), "", -1);
		logService.save(mevzuatFileParseLog);
	}
}
