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

import java.lang.reflect.Field;

import javax.xml.bind.annotation.XmlType;

import org.w3c.dom.Element;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.support.DefaultAsyncProducer;

import com.prowidesoftware.swift.model.mx.AbstractMX;
import com.prowidesoftware.swift.model.mx.AppHdr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ISO20022Producer extends DefaultAsyncProducer {
	public static final String MESSAGE_TYPE_HEADER = "messageType";
	public static final String DOCUMENT_TYPE_HEADER = "documentType";
	public static final String APP_HEADER = "appHeader";

	private static final Logger LOG = LoggerFactory.getLogger(ISO20022Producer.class);

	private CamelContext camelContext;

	public ISO20022Producer(ISO20022Endpoint endpoint) {

		super(endpoint);
		this.camelContext = endpoint.getCamelContext();

	}

	@Override
	public ISO20022Endpoint getEndpoint() {
		return (ISO20022Endpoint) super.getEndpoint();
	}

	public boolean process(Exchange exchange, AsyncCallback callback) {
		LOG.debug("Exchange Pattern {}", exchange.getPattern());

		String methodType = this.getEndpoint().getMethodType();

		switch (methodType) {
		case ISO20022Configuration.MARSHAL_PREFIX:
			return this.marshall(exchange, callback);
		case ISO20022Configuration.UNMARSHAL_PREFIX:
			return this.unmarshall(exchange, callback);

		}

		exchange.setException(new Exception("Unknown method " + methodType));
		callback.done(true);
		return true;

	}

	boolean marshall(Exchange exchange, AsyncCallback callback) {
		try {
			String type = this.getEndpoint().getType();

			String messageType = this.getEndpoint().getMessageType();

			String documentType = this.getEndpoint().getDocumentType();

			Object document = exchange.getIn().getBody();

			AbstractMX mx;

			// if the object is MX type
			if (document instanceof AbstractMX)
				mx = (AbstractMX) document;
			else {
				if (messageType == null)
					messageType = (String) exchange.getMessage().getHeader(MESSAGE_TYPE_HEADER);

				if (documentType == null)
					documentType = (String) exchange.getMessage().getHeader(DOCUMENT_TYPE_HEADER);

				AppHdr appHeader = (AppHdr) exchange.getMessage().getHeader(APP_HEADER);

				Class mxClass = this.getMXClass(messageType);
				
				Field documentField = this.getDocumentField(mxClass, documentType);

				if (appHeader == null)
					mx = (AbstractMX) mxClass.getConstructor().newInstance();
				else
					mx = (AbstractMX) mxClass.getConstructor(AppHdr.class).newInstance(appHeader);

				
				documentField.setAccessible(true);
				documentField.set(mx, document);

			}

			LOG.debug("Marshalled message type: " + mx.getMxId().id());

			switch (type) {
			case ISO20022Configuration.JSON_TYPE:
				LOG.debug("Marshalled JSON message: " + mx.toJson());
				exchange.getMessage().setBody(mx.toJson());
				break;
			case ISO20022Configuration.DOM_TYPE:
				exchange.getMessage().setBody(mx.element());
				break;				
			default:
				LOG.debug("Marshalled XML message: " + mx.document());
				exchange.getMessage().setBody(mx.document());
			}

			callback.done(true);
			return true;

		} catch (Throwable e) {
			LOG.error(e.getLocalizedMessage(), e);
			exchange.setException(e);
			callback.done(true);
			return true;
		}
	}

	boolean unmarshall(Exchange exchange, AsyncCallback callback) {
		try {
			String type = this.getEndpoint().getType();

			String messageType = this.getEndpoint().getMessageType();

			String documentType = this.getEndpoint().getDocumentType();

			Boolean wrapped = this.getEndpoint().getWrapped();

			if (messageType == null)
				messageType = (String) exchange.getMessage().getHeader(MESSAGE_TYPE_HEADER);

			if (documentType == null)
				documentType = (String) exchange.getMessage().getHeader(DOCUMENT_TYPE_HEADER);

			AbstractMX mx;
			
			
			switch (type) {
			case ISO20022Configuration.JSON_TYPE:
				String jsonInput = (String) exchange.getIn().getBody(String.class);
				LOG.debug("Unmarshalled JSON message: " + jsonInput);
				mx = AbstractMX.fromJson(jsonInput);
				break;
			case ISO20022Configuration.DOM_TYPE:
				Element element = (Element) exchange.getIn().getBody();
				mx = AbstractMX.parse(element);
				break;					
			default:
				String xmlInput = (String) exchange.getIn().getBody(String.class);
				LOG.debug("Unmarshalled XML message: " + xmlInput);
				mx = AbstractMX.parse(xmlInput);
			}
			
			if(wrapped)
				exchange.getMessage().setBody(mx);
			else
			{	
				Class mxClass = mx.getClass();
	
				Field documentField = this.getDocumentField(mxClass, documentType);
				documentField.setAccessible(true);
				Object document = documentField.get(mx);
	
				LOG.debug("Unmarshalled message type: " + mx.getMxId().id());
	
				mxClass = this.getMXClass(mx.getMxId().id());
	
				exchange.getMessage().setBody(document);
				
				if (mx.getAppHdr() != null)
					exchange.getMessage().setHeader(APP_HEADER, mx.getAppHdr());
			}

			exchange.getMessage().setHeader(MESSAGE_TYPE_HEADER, mx.getMxId().id());
			exchange.getMessage().setHeader(DOCUMENT_TYPE_HEADER, documentType);

			callback.done(true);
			return true;

		} catch (Throwable e) {
			LOG.error(e.getLocalizedMessage(), e);
			exchange.setException(e);
			callback.done(true);
			return true;
		}
	}
	
	Field getDocumentField(Class mxClass, String documentType) throws Exception
	{
		if (documentType == null) {
			String[] elements = {};

			if (mxClass.isAnnotationPresent(XmlType.class)) {
				XmlType xmlType = (XmlType) mxClass.getAnnotation(XmlType.class);

				elements = xmlType.propOrder();
			}

			if (elements.length == 0)
				throw new Exception("Unknown document type");

			documentType = elements[0];
		}

		Field[] fields = mxClass.getDeclaredFields();
		
		return mxClass.getDeclaredField(documentType);		
	}

	Class<?> getMXClass(String messageType) throws ClassNotFoundException {
		// remove dots
		String className = messageType.replace(".", "");
		className = "com.prowidesoftware.swift.model.mx.Mx" + className.substring(0, 1).toUpperCase()
				+ className.substring(1);

		return Class.forName(className);
	}

}
