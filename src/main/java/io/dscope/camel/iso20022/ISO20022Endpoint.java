/*
 * Copyright 2023 Exilor Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package io.dscope.camel.iso20022;

import org.apache.camel.Category;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.spi.Metadata;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.spi.UriPath;
import org.apache.camel.support.DefaultEndpoint;
import org.apache.camel.util.UnsafeUriCharactersEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@UriEndpoint(firstVersion = "3.19.0", scheme = "iso20022", syntax = "iso20022", title = "ISO20022 Document Processor", category = { Category.DOCUMENT})
public class ISO20022Endpoint extends DefaultEndpoint {
	
	private static final Logger LOG = LoggerFactory.getLogger(ISO20022Endpoint.class);
	
	@UriPath(label = "common",  enums = "marshal,unmarshal")
	@Metadata(description = "The type of ISO20022 operation to use",required = true)
    private String methodType;	
	
	@UriParam(label = "common", enums = "xml,json,dom")
	@Metadata(description = "The type ISO20022 payload", required = false)
    private String type = ISO20022Configuration.XML_TYPE;
	
    @UriParam(label = "common")
    @Metadata(description = "The type of ISO20022 message")
    private String messageType;
    
    @UriParam(label = "common")
    @Metadata(description = "The document type of ISO20022 message")
    private String documentType; 
    
    @UriParam(label = "common")
    @Metadata(description = "MX message wrapped")
    private Boolean wrapped = false;     
    
    
    public ISO20022Endpoint(String uri, ISO20022Component component, String type) {
    	super(UnsafeUriCharactersEncoder.encode(uri), component);
    	
    	if(type != null)
    		this.type = type;
    	  		
    }
    
    public ISO20022Endpoint(String uri, ISO20022Component component, String documentType, String type) {
    	super(UnsafeUriCharactersEncoder.encode(uri), component);
    	
    	if(type != null)
    		this.type = type;
    	
 
    	if(documentType != null)
    		this.documentType = documentType;   	
    	
    } 
    
    public ISO20022Endpoint(String uri, ISO20022Component component) {
    	super(UnsafeUriCharactersEncoder.encode(uri), component);
    }   

	@Override
	public Producer createProducer() throws Exception {
		Producer producer = new ISO20022Producer(this);
		
		return producer;
	}
	
	@Override
	public Consumer createConsumer(Processor processor) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}	

	/**
	 * @return methodType
	 */
	public String getMethodType() {
		return methodType;
	}

	/**
	 * @param methodType the methodType to set
	 */
	public void setMethodType(String methodType) {
		this.methodType = methodType;
	}	

	/**
	 * @return type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param type of payload
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return documentType
	 */
	public String getDocumentType() {
		return this.documentType;
	}

	/**
	 * @param documentType to set
	 */
	public void setDocumentType(String documentType) {
		this.documentType = documentType;
	}

	/**
	 * @return messageType
	 */
	public String getMessageType() {
		return this.messageType;
	}

	/**
	 * @param messageType to set
	 */
	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}
	
	/**
	 * @return wrapped 
	 */
	public Boolean getWrapped() {
		return this.wrapped;
	}

	/**
	 * @param wrapped 
	 */
	public void setWrapped(Boolean wrapped) {
		this.wrapped = wrapped;
	}	
	
}
