package com.evrim.mevzuat.search.api.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;

public class DocumentParseUtil {
	private static final Logger LOG = LoggerFactory.getLogger(DocumentParseUtil.class);
	
	public static String parseContent(InputStream inputStream, int timeOutSecond) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
	    Future<String> future = executor.submit(new DocumentParser(inputStream));

	    String result = null;
	    
	    try {
			result = future.get(timeOutSecond, TimeUnit.SECONDS);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	    
	    try {
	    	executor.shutdownNow();
	    } catch (Exception e) {
	    	LOG.error("Executor shutdown error, e:"+e.getMessage());
	    }
	    
	    return result;
	}
	
	public static MediaType detectMediaType(InputStream inputStream) {
		Detector detector = new DefaultDetector();
		Metadata metadata = new Metadata();
		
		try {
			return detector.detect(inputStream, metadata);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} 
	}
	
	static class DocumentParser implements Callable<String> {
		InputStream inputStream;
		
		public DocumentParser(InputStream inputStream) {
			this.inputStream = inputStream;
		}
		
		@Override
		public String call() throws Exception {
			Thread.currentThread().setName("DocumentParser Thread");
			
			Parser parser = new AutoDetectParser();
			Metadata metadata = new Metadata();
			ContentHandler contenthandler = new BodyContentHandler(300000);
			
			parser.parse(inputStream, contenthandler, metadata, new ParseContext());
			
			return contenthandler.toString();
		}
		
	}
}
