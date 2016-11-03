/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 inova8.com and/or its affiliates. All rights reserved.
 *
 * 
 */
package com.inova8.odata2sparql.SparqlStatement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.olingo.commons.api.ex.ODataRuntimeException;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import com.inova8.odata2sparql.Exception.OData2SparqlException;
import com.inova8.odata2sparql.RdfConnector.openrdf.RdfConstructQuery;
import com.inova8.odata2sparql.RdfConnector.openrdf.RdfTripleSet;
import com.inova8.odata2sparql.RdfEdmProvider.RdfEdmProvider;
import com.inova8.odata2sparql.RdfModel.RdfModel.RdfEntityType;

public class SparqlStatement {
	private final static Log log = LogFactory.getLog(SparqlStatement.class);
	private static String sparql;

	public SparqlStatement(String sparql) {
		this.sparql = sparql;
	}

	public String getSparql() {
		return sparql;
	}
	public SparqlEntityCollection executeQuery(RdfEdmProvider sparqlEdmProvider, RdfEntityType entityType,
			//TODO V2			List<ArrayList<NavigationPropertySegment>> expand, List<SelectItem> select) throws OData2SparqlException {
			ExpandOption expand, SelectOption select) throws OData2SparqlException {
		RdfConstructQuery rdfQuery = new RdfConstructQuery(sparqlEdmProvider.getRdfRepository().getDataRepository(),
				sparql);
		RdfTripleSet results;
		try {
			results = rdfQuery.execConstruct();
		} catch (OData2SparqlException e) {
			log.error(e.getMessage());
			throw new ODataRuntimeException(e.getMessage(), null);
		}
		return new SparqlEntityCollection(sparqlEdmProvider, entityType, results, expand, select);
	}
}