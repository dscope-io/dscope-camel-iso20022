package io.dscope.camel.iso20022.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.Properties;

import javax.xml.bind.annotation.XmlType;

import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.prowidesoftware.swift.model.mx.AbstractMX;

public final class ISO20022CamelTest extends CamelTestSupport {
	static Logger LOG;

	static String PROPERTIES_FILE_NAME = "application.properties";
	
	static final String SWIFT_XML_NODE_FILE = "CustomerCreditTransferInitiationV03.xml";
	
	static final String SWIFT_JSON_NODE_FILE = "CustomerCreditTransferInitiationV03.json";
	
		
	static Properties env;

	static {
		LOG = LoggerFactory.getLogger(ISO20022CamelTest.class);
		
		InputStream propInputStream = ISO20022CamelTest.class.getClassLoader()
				.getResourceAsStream(PROPERTIES_FILE_NAME);

		env = new Properties();
		try {
			env.load(propInputStream);			
		} catch (IOException e) {
			LOG.error(e.getLocalizedMessage(), e);
			Assertions.fail(e.getMessage());
		}		
	}

	@Test
	public void test() {

		try {            
            InputStream inputStream = ISO20022CamelTest.class.getClassLoader().getResourceAsStream(SWIFT_XML_NODE_FILE);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String xml = "";
            
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                xml += line;
            }  
            
            inputStream = ISO20022CamelTest.class.getClassLoader().getResourceAsStream(SWIFT_JSON_NODE_FILE);
            inputStreamReader = new InputStreamReader(inputStream);
            bufferedReader = new BufferedReader(inputStreamReader);
            String json = "";
            
            while ((line = bufferedReader.readLine()) != null) {
                json += line;
            }
			
			AbstractMX mx = AbstractMX.parse(xml);
			
			Class mxClass = mx.getClass();
			
			String[] elements = {};
			
			if(mxClass.isAnnotationPresent(XmlType.class))
			{
				XmlType xmlType = (XmlType) mxClass.getAnnotation(XmlType.class);
				
				elements = xmlType.propOrder();
			}
			
			Field[] fields = mxClass.getDeclaredFields();
			
			Object payload= null;
			
			if(elements.length > 0)
			{				
				Field payloadField = mxClass.getDeclaredField(elements[0]);
				
				payloadField.setAccessible(true);
				payload = payloadField.get(mx);
			}
			
			LOG.info("Parsed message type: " + mx.getMxId().id());
			
	        getMockEndpoint("mock:marshaljson");

	        template.sendBody("direct:marshaljson", payload);   	        
	    
	        MockEndpoint.assertIsSatisfied(this.context());	
	        
	        getMockEndpoint("mock:marshaljson");

	        template.sendBody("direct:marshaljson", payload);   	        
	    
	        MockEndpoint.assertIsSatisfied(this.context());
	        
	        getMockEndpoint("mock:marshaldom");

	        template.sendBody("direct:marshaldom", payload);   	        
	    
	        MockEndpoint.assertIsSatisfied(this.context());;	        
	        			
	        getMockEndpoint("mock:unmarshalxml").expectedBodiesReceived(payload);

	        template.sendBody("direct:unmarshalxml", xml);   	        

	        MockEndpoint.assertIsSatisfied(this.context());	
	        
	        getMockEndpoint("mock:unmarshaljson").expectedBodiesReceived(payload);

	        template.sendBody("direct:unmarshaljson", json);   	        

	        MockEndpoint.assertIsSatisfied(this.context());
	        
	        getMockEndpoint("mock:unmarshaldom");

	        template.sendBody("direct:unmarshaldom", mx.element());   	        

	        MockEndpoint.assertIsSatisfied(this.context());		        

		} catch (Exception e) {
			LOG.error(e.getLocalizedMessage(), e);
			Assertions.fail(e.getMessage());
		}
	}
	
    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
            	from("file:data?noop=true").to("iso20022:unmarshal").to("mock:unmarshalxml");    			          	
                from("direct:marshalxml").to("iso20022:marshal?messageType=pain.001.001.03").to("mock:marshalxml");
                from("direct:marshaljson").to("iso20022:marshal?messageType=pain.001.001.03&type=json").to("mock:marshaljson");
                from("direct:marshaldom").to("iso20022:marshal?messageType=pain.001.001.03&type=dom").to("mock:marshaldom");
                from("direct:unmarshalxml").to("iso20022:unmarshal").to("mock:unmarshalxml");
                from("direct:unmarshaljson").to("iso20022:unmarshal?type=json").to("mock:unmarshaljson");
                from("direct:unmarshaldom").to("iso20022:unmarshal?type=dom").to("mock:unmarshaldom");
                        	
            }
        };
    }

}
