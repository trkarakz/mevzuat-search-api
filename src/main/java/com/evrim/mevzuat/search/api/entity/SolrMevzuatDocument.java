package com.evrim.mevzuat.search.api.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Transient;

import org.apache.solr.client.solrj.beans.Field;
import org.springframework.data.annotation.Id;
import org.springframework.data.solr.core.mapping.SolrDocument;

@SolrDocument(solrCoreName="mevzuat_file")
public class SolrMevzuatDocument implements Serializable {

	private static final long serialVersionUID = -8497241774001475263L;

	@Id
	int itemNo;

	@Field
	String keyValue;

	@Field
	String mevzuat;
	
	@Field
	String fileName;

	@Field
	String title;
	
	@Field
	String mainTopic;
	
	@Field
	String content;

	@Transient
	String rawContent;
	
	@Field
	String linkedContent;

	@Field
	String importantContent;

	@Field
	String keywords;

	@Transient
	Timestamp lastFileUpdateDate;

	public int getItemNo() {
		return itemNo;
	}

	public void setItemNo(int itemNo) {
		this.itemNo = itemNo;
	}

	public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public String getMevzuat() {
		return mevzuat;
	}

	public void setMevzuat(String mevzuat) {
		this.mevzuat = mevzuat;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLinkedContent() {
		return linkedContent;
	}

	public void setLinkedContent(String linkedContent) {
		this.linkedContent = linkedContent;
	}

	public String getImportantContent() {
		return importantContent;
	}

	public void setImportantContent(String importantContent) {
		this.importantContent = importantContent;
	}

	public String getKeywords() {
		return keywords;
	}

	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public Timestamp getLastFileUpdateDate() {
		return lastFileUpdateDate;
	}

	public void setLastFileUpdateDate(Timestamp lastFileUpdateDate) {
		this.lastFileUpdateDate = lastFileUpdateDate;
	}

	public String getMainTopic() {
		return mainTopic;
	}

	public void setMainTopic(String mainTopic) {
		this.mainTopic = mainTopic;
	}

	public String getRawContent() {
		return rawContent;
	}

	public void setRawContent(String rawContent) {
		this.rawContent = rawContent;
	}

	@Override
	public String toString() {
		return "MevzuatDocument [itemNo=" + itemNo + ", keyValue=" + keyValue + ", mevzuat=" + mevzuat + ", title="
				+ title + ", mainTopic=" + mainTopic + ", lastFileUpdateDate=" + lastFileUpdateDate + "]";
	}

}
