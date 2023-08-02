package com.evrim.mevzuat.search.api.entity;

import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "mevzuat_document")
public class MevzuatDocument implements Serializable {

	private static final long serialVersionUID = -435366403198778342L;

	@Id
	@Column(name = "item_no")
	int itemNo;

	@Column(name = "indexer_job_id")
	Integer indexerJobId;
	
	@Column(name = "key_value")
	String keyValue;

	@Column(name = "mevzuat")
	String mevzuat;

	@Column(name = "title")
	String title;
	
	@Column(name = "main_topic")
	String mainTopic;
	
	@Column(name = "raw_content", length=10000000)
	String rawContent;

	@Column(name = "important_content", length=10000000)
	String importantContent;

	@Column(name = "keywords", length=10000000)
	String keywords;

	@Column(name = "index_error", length=10000000)
	String indexError;
	
	@Column(name = "last_file_update_date")
	Timestamp lastFileUpdateDate;

	@Column(name = "last_index_date")
	Timestamp lastIndexDate;

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

	public String getIndexError() {
		return indexError;
	}

	public void setIndexError(String indexError) {
		this.indexError = indexError;
	}

	public Timestamp getLastFileUpdateDate() {
		return lastFileUpdateDate;
	}

	public void setLastFileUpdateDate(Timestamp lastFileUpdateDate) {
		this.lastFileUpdateDate = lastFileUpdateDate;
	}

	public Timestamp getLastIndexDate() {
		return lastIndexDate;
	}

	public void setLastIndexDate(Timestamp lastIndexDate) {
		this.lastIndexDate = lastIndexDate;
	}

	public Integer getIndexerJobId() {
		return indexerJobId;
	}

	public void setIndexerJobId(Integer indexerJobId) {
		this.indexerJobId = indexerJobId;
	}

	@Override
	public String toString() {
		return "MevzuatDocument [itemNo=" + itemNo + ", indexerJobId=" + indexerJobId + ", keyValue=" + keyValue
				+ ", mevzuat=" + mevzuat + ", title=" + title + ", mainTopic=" + mainTopic + ", lastFileUpdateDate=" + lastFileUpdateDate
				+ ", lastIndexDate=" + lastIndexDate + "]";
	}
	
	
}
