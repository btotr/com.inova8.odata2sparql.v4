package com.inova8.odata2sparql;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.olingo.commons.api.edmx.EdmxReference;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.inova8.odata2sparql.Constants.RdfConstants;
import com.inova8.odata2sparql.Exception.OData2SparqlException;
import com.inova8.odata2sparql.RdfEdmProvider.RdfEdmProvider;
import com.inova8.odata2sparql.RdfEdmProvider.RdfEdmProviders;
import com.inova8.odata2sparql.SparqlProcessor.SparqlBatchProcessor;
import com.inova8.odata2sparql.SparqlProcessor.SparqlDefaultProcessor;
import com.inova8.odata2sparql.SparqlProcessor.SparqlEntityCollectionProcessor;
import com.inova8.odata2sparql.SparqlProcessor.SparqlEntityProcessor;
import com.inova8.odata2sparql.SparqlProcessor.SparqlPrimitiveValueProcessor;
import com.inova8.odata2sparql.SparqlProcessor.SparqlReferenceCollectionProcessor;
import com.inova8.odata2sparql.SparqlProcessor.SparqlReferenceProcessor;

public class RdfODataServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final Logger log = LoggerFactory.getLogger(RdfODataServlet.class);
	static private RdfEdmProviders rdfEdmProviders = null;

	protected void service(final HttpServletRequest req, final HttpServletResponse resp)
			throws ServletException, IOException {
		try {
			if (rdfEdmProviders == null) {

				ServletContext servletContext = getServletContext();
				File repositoryDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
				rdfEdmProviders = new RdfEdmProviders(this.getInitParameter("configFolder"),
						this.getInitParameter("repositoryFolder"), this.getInitParameter("repositoryUrl"),
						repositoryDir.getAbsolutePath());
			}
			if (req.getPathInfo() != null && (!req.getPathInfo().equals("/"))) {
				String service = req.getPathInfo().split("/")[1];
				if (service.equals(RdfConstants.RESET)) {
					log.info(RdfConstants.RESET + " requested: " + req.getPathInfo().split("/")[2]);
					rdfEdmProviders.reset(req.getPathInfo().split("/")[2]);
					simpleResponse(req, resp, RdfConstants.RESET + ": " + req.getPathInfo().split("/")[2]);
				} else if (service.equals(RdfConstants.RELOAD)) {
					log.info(RdfConstants.RELOAD + " requested");
					rdfEdmProviders.reload();
					simpleResponse(req, resp, RdfConstants.RELOAD);
				} else {
					//Find provider matching service name			
					RdfEdmProvider rdfEdmProvider = rdfEdmProviders.getRdfEdmProvider(service);
					// create odata handler and configure it with CsdlEdmProvider and Processor
					if (rdfEdmProvider != null) {
						OData odata = OData.newInstance();
						ServiceMetadata edm = odata.createServiceMetadata(rdfEdmProvider,
								new ArrayList<EdmxReference>());
						ODataHttpHandler handler = odata.createHandler(edm);
						//Reserve first parameter for either the service name or a$RESET. $RELOAD
						handler.setSplit(1);
						handler.register(new SparqlEntityCollectionProcessor(rdfEdmProvider));
						handler.register(new SparqlEntityProcessor(rdfEdmProvider));
						handler.register(new SparqlPrimitiveValueProcessor(rdfEdmProvider));
						handler.register(new SparqlDefaultProcessor());
						handler.register(new SparqlReferenceCollectionProcessor(rdfEdmProvider));
						handler.register(new SparqlReferenceProcessor(rdfEdmProvider));
						handler.register(new SparqlBatchProcessor(rdfEdmProvider));
						//handler.register(new SparqlErrorProcessor());
						log.info(req.getMethod() + ": " + req.getPathInfo() + " Query: " + req.getQueryString());
						if (!req.getMethod().equals("OPTIONS")) {
							//Required to satisfy OpenUI5 V1.54.3 and above
							resp.addHeader("Access-Control-Expose-Headers", "DataServiceVersion,OData-Version");
							handler.process(req, resp);
						} else {
							optionsResponse( resp);
						}
					} else {
						throw new OData2SparqlException("No service matching " + service + " found");
					}
				}
			} else {
				htmlResponse(req, resp, "/index.html");
			}
		} catch (RuntimeException | OData2SparqlException e) {
			log.error("Server Error occurred in RdfODataServlet", e);
			throw new ServletException(e);
		}
	}

	private void optionsResponse(final HttpServletResponse resp){
		resp.addHeader("Access-Control-Allow-Origin", "*");
		resp.addHeader("Access-Control-Allow-Headers", "Content-Type, Content-Length, Authorization, Accept, X-Requested-With, X-CSRF-Token, odata-maxversion, odata-version,mime-version");
		resp.addHeader("Access-Control-Allow-Methods", "PUT, POST, GET, DELETE, OPTIONS");
		resp.addHeader("Access-Control-Expose-Headers", "DataServiceVersion,OData-Version");
		resp.addHeader("Access-Control-Max-Age", "86400");
		resp.addHeader("OData-Version", "4.0");
		resp.addHeader("OData-MaxVersion", "4.0");
		resp.setStatus(200);
	}
	private void htmlResponse(HttpServletRequest req, HttpServletResponse resp, String textResponse)
			throws IOException {
		try {
			InputStream in = getServletContext().getResourceAsStream(textResponse);
			String contents = IOUtils.toString(in);
			simpleResponse(req, resp, contents);
		} catch (Exception e) {
			simpleResponse(req, resp, "odata2sparql.v4");
		}
	}

	private void simpleResponse(final HttpServletRequest req, final HttpServletResponse resp, String textResponse)
			throws IOException {
		Scanner scanner = new Scanner(req.getInputStream());
		StringBuilder sb = new StringBuilder();
		while (scanner.hasNextLine()) {
			sb.append(scanner.nextLine());
		}
		resp.getOutputStream().println(textResponse);
		scanner.close();
	}
}
