package com.evrim.mevzuat.search.api.config;

import org.apache.solr.client.solrj.impl.CloudSolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.solr.core.SolrOperations;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.repository.config.EnableSolrRepositories;

@Configuration
@EnableSolrRepositories(basePackages = {"com.evrim.mevzuat.search.api.repository"})
public class SolrConfig {
	
	@Value("${spring.data.solr.zk-host}")
	String zkHost;
	
	@Value("${solr.http.client.url}")
	String httpUrl;
	
	public CloudSolrClient cloudSolrClient() {
		return new CloudSolrClient(zkHost);
	}
	
	public HttpSolrClient httpSolrClient() {
		return new HttpSolrClient(httpUrl);
	}
	
	@Bean
	public SolrOperations solrTemplate() {
		SolrTemplate solrTemplate = new SolrTemplate(httpSolrClient());
		return solrTemplate;
	}
}
