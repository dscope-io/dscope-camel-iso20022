# ISO20022 pain.001 YAML Route Sample

This sample demonstrates how to use the dScope Camel ISO20022 component with YAML DSL to process ISO20022 pain.001.001.03 (Customer Credit Transfer Initiation) messages.

## Overview

The sample shows how to:

1. **Unmarshal** ISO20022 XML messages into Java objects
2. **Marshal** Java objects back to ISO20022 JSON and XML
3. Use **YAML DSL** routes with Camel Main (no Java route code required)

## Project Structure

```text
yaml-pain001-sample/
├── pom.xml
├── src/main/resources/
│   ├── application.properties
│   └── camel/
│       └── pain001-route.camel.yaml
├── data/
│   ├── input/
│   │   └── sample-credit-transfer.xml
│   └── output/
└── README.md
```

## Routes

### `pain001-unmarshal-route`

1. Reads XML files from `data/input`
2. Unmarshals using `iso20022:unmarshal`
3. Marshals to JSON using `iso20022:marshal?type=json`
4. Writes JSON to `data/output/<input-file>.json`

### `pain001-marshal-route`

1. Runs once on startup (timer)
2. Loads `data/input/sample-credit-transfer.xml`
3. Unmarshals to Java object
4. Marshals back to XML using `iso20022:marshal?type=xml`
5. Writes normalized XML to `data/output/sample-credit-transfer.normalized.xml`

## Prerequisites

- Java 21+
- Maven 3.9+
- `io.dscope:dscope-camel-iso20022:1.0.0` in local Maven repository or Maven Central

If needed, install the component first from repository root:

```bash
mvn clean install
```

## Build and Run

```bash
cd samples/yaml-pain001-sample
mvn clean package
mvn exec:java
```

## Expected Output

After startup, you should see files created in `data/output/`:

- `sample-credit-transfer.xml.json`
- `sample-credit-transfer.normalized.xml`
