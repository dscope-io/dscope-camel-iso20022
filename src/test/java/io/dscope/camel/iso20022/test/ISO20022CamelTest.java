package io.dscope.camel.iso20022.test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;

import org.apache.camel.Exchange;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit5.CamelTestSupport;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;

import com.prowidesoftware.swift.model.mx.AbstractMX;

import io.dscope.camel.iso20022.ISO20022Producer;

public final class ISO20022CamelTest extends CamelTestSupport {

    private static final String SWIFT_XML_NODE_FILE = "CustomerCreditTransferInitiationV03.xml";
    private static final String SWIFT_JSON_NODE_FILE = "CustomerCreditTransferInitiationV03.json";

    @Test
    public void marshalProducesXmlJsonAndDom() throws Exception {
        AbstractMX mx = AbstractMX.parse(readResource(SWIFT_XML_NODE_FILE));

        String xml = template.requestBody("direct:marshalxml", mx, String.class);
        String json = template.requestBody("direct:marshaljson", mx, String.class);
        Object dom = template.requestBody("direct:marshaldom", mx);

        Assertions.assertNotNull(xml);
        Assertions.assertFalse(xml.isBlank());
        Assertions.assertNotNull(json);
        Assertions.assertFalse(json.isBlank());
        Assertions.assertInstanceOf(Element.class, dom);
    }

    @Test
    public void unmarshalParsesXmlJsonAndDom() throws Exception {
        String xml = readResource(SWIFT_XML_NODE_FILE);
        String json = readResource(SWIFT_JSON_NODE_FILE);
        Element dom = AbstractMX.parse(xml).element();

        Exchange xmlExchange = template.request("direct:unmarshalxml", exchange -> exchange.getMessage().setBody(xml));
        Exchange jsonExchange = template.request("direct:unmarshaljson", exchange -> exchange.getMessage().setBody(json));
        Exchange domExchange = template.request("direct:unmarshaldom", exchange -> exchange.getMessage().setBody(dom));

        Object xmlBody = xmlExchange.getMessage().getBody();
        Object jsonBody = jsonExchange.getMessage().getBody();

        Assertions.assertEquals("pain.001.001.03",
                xmlExchange.getMessage().getHeader(ISO20022Producer.MESSAGE_TYPE_HEADER));
        Assertions.assertEquals(xmlBody.getClass(), jsonBody.getClass());
        Assertions.assertNull(domExchange.getException());
    }

    @Test
    public void unmarshalWrappedReturnsMxMessage() throws Exception {
        Object wrapped = template.requestBody("direct:unmarshalwrapped", readResource(SWIFT_XML_NODE_FILE));

        Assertions.assertInstanceOf(AbstractMX.class, wrapped);
        Assertions.assertEquals("pain.001.001.03", ((AbstractMX) wrapped).getMxId().id());
    }

    @Test
    public void marshalUsesHeadersWhenEndpointOptionsAreNotProvided() throws Exception {
        AbstractMX mx = AbstractMX.parse(readResource(SWIFT_XML_NODE_FILE));
        Object document = extractFirstDocument(mx);

        String json = template.request("direct:marshalfromheaders", exchange -> {
            exchange.getMessage().setBody(document);
            exchange.getMessage().setHeader(ISO20022Producer.MESSAGE_TYPE_HEADER, "pain.001.001.03");
            exchange.getMessage().setHeader(ISO20022Producer.DOCUMENT_TYPE_HEADER, "cstmrCdtTrfInitn");
        }).getMessage().getBody(String.class);

        Assertions.assertNotNull(json);
        Assertions.assertFalse(json.isBlank());
    }

    @Test
    public void marshalFailsWhenMessageTypeIsMissing() throws Exception {
        AbstractMX mx = AbstractMX.parse(readResource(SWIFT_XML_NODE_FILE));
        Object document = extractFirstDocument(mx);

        Assertions.assertThrows(CamelExecutionException.class,
                () -> template.requestBody("direct:marshalmissingtype", document, String.class));
    }

    @Override
    protected RoutesBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:marshalxml").routeId("directMarshalXml")
                        .to("iso20022:marshal?messageType=pain.001.001.03")
                        .to("mock:marshalxml");
                from("direct:marshaljson").routeId("directMarshalJson")
                        .to("iso20022:marshal?messageType=pain.001.001.03&type=json")
                        .to("mock:marshaljson");
                from("direct:marshaldom").routeId("directMarshalDom")
                        .to("iso20022:marshal?messageType=pain.001.001.03&type=dom")
                        .to("mock:marshaldom");
                from("direct:unmarshalxml").routeId("directUnmarshalXml")
                        .to("iso20022:unmarshal")
                        .to("mock:unmarshalxml");
                from("direct:unmarshaljson").routeId("directUnmarshalJson")
                        .to("iso20022:unmarshal?type=json")
                        .to("mock:unmarshaljson");
                from("direct:unmarshaldom").routeId("directUnmarshalDom")
                        .to("iso20022:unmarshal?type=dom")
                        .to("mock:unmarshaldom");
                from("direct:unmarshalwrapped").routeId("directUnmarshalWrapped")
                        .to("iso20022:unmarshal?wrapped=true")
                        .to("mock:unmarshalwrapped");
                from("direct:marshalfromheaders").routeId("directMarshalFromHeaders")
                        .to("iso20022:marshal?type=json")
                        .to("mock:marshalfromheaders");
                from("direct:marshalmissingtype").routeId("directMarshalMissingType")
                        .to("iso20022:marshal?type=json")
                        .to("mock:marshalmissingtype");
            }
        };
    }

    private static Object extractFirstDocument(AbstractMX mx) throws Exception {
        Class<?> mxClass = mx.getClass();
        String documentType = firstPropOrderEntry(mxClass);
        Field payloadField = mxClass.getDeclaredField(documentType);
        payloadField.setAccessible(true);
        return payloadField.get(mx);
    }

    @SuppressWarnings("unchecked")
    private static String firstPropOrderEntry(Class<?> mxClass) throws Exception {
        String[] annotationTypes = { "jakarta.xml.bind.annotation.XmlType", "javax.xml.bind.annotation.XmlType" };

        for (String annotationType : annotationTypes) {
            try {
                Class<? extends Annotation> xmlTypeClass = (Class<? extends Annotation>) Class.forName(annotationType);
                if (!mxClass.isAnnotationPresent(xmlTypeClass)) {
                    continue;
                }
                Annotation annotation = mxClass.getAnnotation(xmlTypeClass);
                Method propOrderMethod = xmlTypeClass.getMethod("propOrder");
                String[] propOrder = (String[]) propOrderMethod.invoke(annotation);
                if (propOrder.length > 0) {
                    return propOrder[0];
                }
            } catch (ClassNotFoundException e) {
                // Ignore and try alternative JAXB annotation package.
            }
        }

        throw new IllegalStateException("Unable to resolve document type from MX class annotations");
    }

    private static String readResource(String fileName) throws IOException {
        try (InputStream inputStream = ISO20022CamelTest.class.getClassLoader().getResourceAsStream(fileName)) {
            if (inputStream == null) {
                throw new IOException("Missing resource: " + fileName);
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
