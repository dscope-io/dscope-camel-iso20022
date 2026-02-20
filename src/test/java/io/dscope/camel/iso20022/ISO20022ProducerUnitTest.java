package io.dscope.camel.iso20022;

import java.lang.reflect.Field;

import org.apache.camel.impl.DefaultCamelContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

final class ISO20022ProducerUnitTest {

    @Test
    void getMxClassResolvesSwiftMessageClass() throws Exception {
        ISO20022Producer producer = createProducer();

        Class<?> mxClass = producer.getMXClass("pain.001.001.03");

        Assertions.assertEquals("com.prowidesoftware.swift.model.mx.MxPain00100103", mxClass.getName());
    }

    @Test
    void getDocumentFieldFallsBackToXmlTypePropOrder() throws Exception {
        ISO20022Producer producer = createProducer();
        Class<?> mxClass = producer.getMXClass("pain.001.001.03");

        Field documentField = producer.getDocumentField(mxClass, null);

        Assertions.assertEquals("cstmrCdtTrfInitn", documentField.getName());
    }

    @Test
    void getDocumentFieldThrowsWhenFieldIsUnknown() throws Exception {
        ISO20022Producer producer = createProducer();
        Class<?> mxClass = producer.getMXClass("pain.001.001.03");

        Assertions.assertThrows(NoSuchFieldException.class,
                () -> producer.getDocumentField(mxClass, "doesNotExist"));
    }

    private static ISO20022Producer createProducer() {
        ISO20022Component component = new ISO20022Component();
        component.setCamelContext(new DefaultCamelContext());

        ISO20022Endpoint endpoint = new ISO20022Endpoint("iso20022:marshal", component);
        endpoint.setMethodType(ISO20022Configuration.MARSHAL_PREFIX);

        return new ISO20022Producer(endpoint);
    }
}
