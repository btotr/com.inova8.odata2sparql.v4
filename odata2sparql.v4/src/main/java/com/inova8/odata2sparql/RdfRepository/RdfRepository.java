/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 inova8.com and/or its affiliates. All rights reserved.
 *
 * 
 */
package com.inova8.odata2sparql.RdfRepository;

import java.util.Hashtable;
import org.eclipse.rdf4j.model.Namespace;

import com.inova8.odata2sparql.Constants.TextSearchType;

public class RdfRepository  {

	private RdfConnection dataEndPoint;
	private RdfConnection modelEndPoint;
	private RdfConnection operationEndPoint;
	
	/**
	 * @param dataRepository the dataRepository to set
	 */
	public void setDataRepository(RdfRoleRepository dataRepository) {
		this.dataRepository = dataRepository;
		this.dataEndPoint =new RdfConnection(dataRepository);
	}


	/**
	 * @param modelRepository the modelRepository to set
	 */
	public void setModelRepository(RdfRoleRepository modelRepository) {
		this.modelRepository = modelRepository;

		this.modelEndPoint =new RdfConnection(modelRepository);
		this.operationEndPoint = this.modelEndPoint;
	}


	/**
	 * @return the dataRepository
	 */
	public RdfRoleRepository getDataRepository() {
		return dataRepository;
	}


	/**
	 * @return the modelRepository
	 */
	public RdfRoleRepository getModelRepository() {
		return modelRepository;
	}

	private RdfRoleRepository dataRepository;
	private RdfRoleRepository modelRepository;
	
	private final Namespace defaultPrefix;
	private final Hashtable<String, Namespace> namespaces;
	private int defaultQueryLimit;
	private final String modelName;
	private Boolean withRdfAnnotations;
	private Boolean withSapAnnotations;
	private Boolean useBaseType;
	private Boolean expandOperations = false;
	private TextSearchType textSearchType;
	
	RdfRepository(String modelName,Namespace defaultPrefix,Hashtable<String, Namespace> namespaces) {
		super();
		this.modelName = modelName;
		this.defaultPrefix = defaultPrefix;		
		this.namespaces =  namespaces; 	
	}
	public String getModelName() {
		return modelName;
	}

	public String defaultNamespace() {
		
		return defaultPrefix.getName();
	}
	public Hashtable<String, Namespace> getNamespaces() {
		
		return namespaces;
	}
	public RdfConnection getDataEndpoint() {
		return dataEndPoint;
	}

	public RdfConnection getModelEndPoint() {
		return modelEndPoint;
	}

	public RdfConnection getOperationEndPoint() {
		return operationEndPoint;
	}

	public String getDefaultPrefix() {
		return defaultPrefix.getPrefix();
	}

	public int getDefaultQueryLimit() {
		return defaultQueryLimit;
	}

	public void setDefaultQueryLimit(int defaultQueryLimit) {
		this.defaultQueryLimit = defaultQueryLimit;
	}

	public Boolean getWithRdfAnnotations() {
		return withRdfAnnotations;
	}

	public void setWithRdfAnnotations(Boolean withRdfAnnotations) {
		this.withRdfAnnotations = withRdfAnnotations;
	}

	public Boolean getWithSapAnnotations() {
		return withSapAnnotations;
	}

	public void setWithSapAnnotations(Boolean withSapAnnotations) {
		this.withSapAnnotations = withSapAnnotations;
	}
	public Boolean getUseBaseType() {
		return useBaseType;
	}
	public void setUseBaseType(boolean useBaseType) {
		this.useBaseType = useBaseType;	
	}
	public Boolean getExpandOperations() {
		return expandOperations;
	}
	public void setExpandOperations(boolean expandOperations) {
		this.expandOperations = expandOperations;	
	}
	public TextSearchType getTextSearchType() {
		return textSearchType;
	}
	public void setTextSearchType(TextSearchType textSearchType) {
		this.textSearchType = textSearchType;
	}

}
