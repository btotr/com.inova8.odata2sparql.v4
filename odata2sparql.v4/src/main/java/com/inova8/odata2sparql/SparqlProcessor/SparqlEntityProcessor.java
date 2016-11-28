package com.inova8.odata2sparql.SparqlProcessor;

import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import org.apache.olingo.commons.api.data.ContextURL;
import org.apache.olingo.commons.api.data.ContextURL.Suffix;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.edm.EdmEntitySet;
import org.apache.olingo.commons.api.edm.EdmEntityType;
import org.apache.olingo.commons.api.edm.EdmException;
import org.apache.olingo.commons.api.edm.EdmNavigationProperty;
import org.apache.olingo.commons.api.ex.ODataException;
import org.apache.olingo.commons.api.format.ContentType;
import org.apache.olingo.commons.api.http.HttpHeader;
import org.apache.olingo.commons.api.http.HttpStatusCode;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataApplicationException;
import org.apache.olingo.server.api.ODataLibraryException;
import org.apache.olingo.server.api.ODataRequest;
import org.apache.olingo.server.api.ODataResponse;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityProcessor;
import org.apache.olingo.server.api.serializer.EntitySerializerOptions;
import org.apache.olingo.server.api.serializer.ODataSerializer;
import org.apache.olingo.server.api.serializer.SerializerResult;
import org.apache.olingo.server.api.uri.UriInfo;
import org.apache.olingo.server.api.uri.UriResource;
import org.apache.olingo.server.api.uri.UriResourceEntitySet;
import org.apache.olingo.server.api.uri.UriResourceNavigation;
import org.apache.olingo.server.api.uri.queryoption.ExpandOption;
import org.apache.olingo.server.api.uri.queryoption.SelectOption;

import com.inova8.odata2sparql.Exception.OData2SparqlException;
import com.inova8.odata2sparql.RdfEdmProvider.RdfEdmProvider;
import com.inova8.odata2sparql.RdfEdmProvider.Util;
import com.inova8.odata2sparql.uri.UriType;
import com.inova8.odata2sparql.SparqlStatement.SparqlBaseCommand;
public class SparqlEntityProcessor implements EntityProcessor {
	public SparqlEntityProcessor(RdfEdmProvider rdfEdmProvider) {
		super();
		this.rdfEdmProvider = rdfEdmProvider;
	}
	private final RdfEdmProvider rdfEdmProvider;
	private OData odata;
	private ServiceMetadata serviceMetadata;
	@Override
	public void init(OData odata, ServiceMetadata serviceMetadata) {
		this.odata = odata;
		this.serviceMetadata = serviceMetadata;
	}

	@Override
	public void readEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
	    // 1. retrieve the Entity Type
	    List<UriResource> resourceParts = uriInfo.getUriResourceParts();
	    UriResourceEntitySet uriResourceEntitySet = (UriResourceEntitySet) resourceParts.get(0);
	    EdmEntitySet edmEntitySet = uriResourceEntitySet.getEntitySet();

	    EdmEntityType responseEdmEntityType = null; 
	    EdmEntitySet responseEdmEntitySet = null; 
	    SelectOption selectOption = uriInfo.getSelectOption();
	    ExpandOption expandOption = uriInfo.getExpandOption();	    
	    // 2. retrieve the data from backend
	    Entity entity = null;
		try {
			entity = SparqlBaseCommand.readEntity( rdfEdmProvider,uriInfo,(uriInfo.getUriResourceParts().size() > 1)?UriType.URI6A:UriType.URI2);
		} catch (EdmException | OData2SparqlException | ODataException e) {
			throw new ODataApplicationException(e.getMessage(), HttpStatusCode.NOT_FOUND.getStatusCode(), Locale.ENGLISH);
		}
	    // 3. serialize

		// Analyze the URI segments
		int segmentCount = resourceParts.size();
	    if(segmentCount == 1){  // no navigation
	        responseEdmEntityType = edmEntitySet.getEntityType();
	        responseEdmEntitySet = edmEntitySet; // since we have only one segment
	    } else if (segmentCount == 2){ //navigation
	        UriResource navSegment = resourceParts.get(1);
	        if(navSegment instanceof UriResourceNavigation){
	            UriResourceNavigation uriResourceNavigation = (UriResourceNavigation) navSegment;
	            EdmNavigationProperty edmNavigationProperty = uriResourceNavigation.getProperty();
	            responseEdmEntityType = edmNavigationProperty.getType();
	            responseEdmEntitySet=Util.getNavigationTargetEntitySet(edmEntitySet, edmNavigationProperty);//SparqlBaseCommand.getNavigationTargetEntitySet(uriInfo);
	        }
	    }else{
	        // this would be the case for e.g. Products(1)/Category/Products(1)/Category
	        throw new ODataApplicationException("Not supported", HttpStatusCode.NOT_IMPLEMENTED.getStatusCode(), Locale.ROOT);
	    }
	    
		ContextURL contextUrl = null;
		try {
			//Need absolute URI for PowewrQuery and Linqpad (and probably other MS based OData clients)
			 String selectList = odata.createUriHelper().buildContextURLSelectList(responseEdmEntityType,
					 expandOption, selectOption);
			 contextUrl = ContextURL.with().entitySet(responseEdmEntitySet).selectList(selectList).serviceRoot(new URI(request.getRawBaseUri()+"/")).build();
		} catch (URISyntaxException e) {
			throw new ODataApplicationException("Inavlid RawBaseURI "+ request.getRawBaseUri(), HttpStatusCode.BAD_REQUEST.getStatusCode(), Locale.ROOT);
		}	    

	    EntitySerializerOptions options = EntitySerializerOptions.with().select(selectOption).expand(expandOption).contextURL(contextUrl).build();

	    ODataSerializer serializer = odata.createSerializer(responseFormat);
	    SerializerResult serializerResult = serializer.entity(serviceMetadata, responseEdmEntityType, entity, options);
	    InputStream entityStream = serializerResult.getContent();

	    //4. configure the response object
	    response.setContent(entityStream);
	    response.setStatusCode(HttpStatusCode.OK.getStatusCode());
	    response.setHeader(HttpHeader.CONTENT_TYPE, responseFormat.toContentTypeString());
	}

	@Override
	public void updateEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		// TODO Auto-generated method stub

	}
	@Override 
	public void createEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo, ContentType requestFormat, ContentType responseFormat)
			throws ODataApplicationException, ODataLibraryException {
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteEntity(ODataRequest request, ODataResponse response, UriInfo uriInfo) throws ODataApplicationException,
			ODataLibraryException {
		// TODO Auto-generated method stub

	}
}
