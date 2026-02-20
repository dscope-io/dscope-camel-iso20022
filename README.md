# dScope Camel ISO20022 Component

`dscope-camel-iso20022` is an Apache Camel component for marshaling and unmarshaling ISO20022 MX messages (for example `pain.001.001.03`) using Prowide ISO20022 models.

## What It Does

- Unmarshal ISO20022 payloads from:
  - XML (`type=xml`, default)
  - JSON (`type=json`)
  - DOM Element (`type=dom`)
- Marshal ISO20022 payloads to:
  - XML (`type=xml`, default)
  - JSON (`type=json`)
  - DOM Element (`type=dom`)
- Supports both wrapped (`AbstractMX`) and unwrapped document payload processing.

## Maven Coordinates

```xml
<dependency>
  <groupId>io.dscope</groupId>
  <artifactId>dscope-camel-iso20022</artifactId>
  <version>1.0.0</version>
</dependency>
```

Maven Central:

[https://search.maven.org/artifact/io.dscope/dscope-camel-iso20022/1.0.0/jar](https://search.maven.org/artifact/io.dscope/dscope-camel-iso20022/1.0.0/jar)

## Requirements

- Java 21+
- Maven 3.9+
- Apache Camel 4.15.0 (component is built and tested with this version)

## Component URI

```text
iso20022:marshal?...    # marshal route endpoint
iso20022:unmarshal?...  # unmarshal route endpoint
```

## Endpoint Options

- `type` (`xml` | `json` | `dom`, default: `xml`)
  - Payload format to read/write.
- `messageType` (example: `pain.001.001.03`)
  - Required for marshaling when body is not already `AbstractMX`.
- `documentType` (example: `cstmrCdtTrfInitn`)
  - Field name of the ISO20022 document in the MX wrapper.
  - If omitted, component tries to infer from JAXB `@XmlType(propOrder=...)`.
- `wrapped` (`true` | `false`, default: `false`, unmarshal only)
  - If `true`, returns the full `AbstractMX` object.
  - If `false`, returns only the document payload object.

## Message Headers

You can pass metadata through Camel headers:

- `messageType`
- `documentType`
- `appHeader` (optional `AppHdr` when marshaling)

These map to constants in code:

- `ISO20022Producer.MESSAGE_TYPE_HEADER`
- `ISO20022Producer.DOCUMENT_TYPE_HEADER`
- `ISO20022Producer.APP_HEADER`

## Body Behavior

### Marshal

- Input body:
  - `AbstractMX` OR
  - document object + `messageType` (and usually `documentType`)
- Output body:
  - XML `String` (`type=xml`)
  - JSON `String` (`type=json`)
  - `org.w3c.dom.Element` (`type=dom`)

### Unmarshal

- Input body:
  - XML `String` (`type=xml`)
  - JSON `String` (`type=json`)
  - `org.w3c.dom.Element` (`type=dom`)
- Output body:
  - document object (`wrapped=false`)
  - `AbstractMX` (`wrapped=true`)

## Java DSL Example

```java
from("direct:marshalJson")
    .to("iso20022:marshal?messageType=pain.001.001.03&documentType=cstmrCdtTrfInitn&type=json");

from("direct:unmarshalXml")
    .to("iso20022:unmarshal")
    .to("log:unmarshalled");

from("direct:unmarshalWrapped")
    .to("iso20022:unmarshal?wrapped=true")
    .to("log:mx");
```

## YAML DSL Example

```yaml
- route:
    id: pain001-unmarshal-route
    from:
      uri: file:data/input
      parameters:
        noop: true
      steps:
        - to:
            uri: iso20022:unmarshal
        - to:
            uri: iso20022:marshal
            parameters:
              messageType: pain.001.001.03
              documentType: cstmrCdtTrfInitn
              type: json
        - to:
            uri: file:data/output
            parameters:
              fileName: "${header.CamelFileName}.json"
```

## Sample Project

A standalone sample (similar structure to the RosettaNet sample) is available at:

`/Users/roman/Projects/eclipse-workspace/dscope-camel-iso20022/samples/yaml-pain001-sample`

Run it:

```bash
cd samples/yaml-pain001-sample
mvn clean package
mvn exec:java
```

Output files are created in:

`samples/yaml-pain001-sample/data/output`

## Build This Component

```bash
mvn clean verify
```

Install locally:

```bash
mvn clean install
```

## Publish to Maven Central

This project follows the same `central-release` profile convention used by the RosettaNet component.

```bash
mvn -Pcentral-release clean deploy
```

Expected Maven server credentials/config:

- `ossrh` (for distribution management)
- `central` (for central publishing plugin)
- GPG configured for artifact signing

## Karavan Metadata

Component metadata is in:

- `src/iso20022.json`

For Apache Karavan VS Code extension, register `iso20022` in the extension component lists and merge metadata from `src/iso20022.json`.

## Notes

- The component supports both `jakarta.xml.bind` and `javax.xml.bind` `XmlType` annotation lookup for document field inference.
- For predictable marshaling from plain document objects, provide both `messageType` and `documentType` explicitly.
