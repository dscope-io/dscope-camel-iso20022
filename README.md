## Apache Camel ISO20022 Component

Apache Camel is an Open Source integration framework that empowers you to quickly and easily integrate various systems consuming or producing data.

<a href="https://camel.apache.org/">
https://camel.apache.org/
</a>

The dScope ISO20022 Camel component allows marshaling and unmarshaling [Swift ISO20022](https://www.iso20022.org/) documents in Apache Camel. This component is using open source [Prowide JAXB XML libraries](https://github.com/prowide/prowide-iso20022)


# Downloads / Accessing Binaries

To add Java dScope Apache Camel ISO20022 Component library to your Java project use Maven or Gradle import from Maven Central.

<a href="https://search.maven.org/artifact/dscope/dscope-camel-iso20022/0.2.0/jar">
https://search.maven.org/artifact/dscope/dscope-camel-iso20022/0.2.0/jar
</a>

```
<dependency>
  <groupId>io.dscope</groupId>
  <artifactId>dscope-camel-iso20022</artifactId>
  <version>0.2.0</version>
</dependency>
```

```
implementation 'io.dscope:dscope-camel-iso20022:0.2.0' 
```

To install dScope Apache Camel ISO20022 Component to Apache Karavan Visual Studio Code plug-in add 

```
iso20022
```

to .vscode/extensions/camel-karavan.karavan-4.3.0/components/components.properties file

and content of [src/iso20022.json](./src/iso20022.json) file to .vscode/extensions/camel-karavan.karavan-4.3.0/components/components.json file.




# Build

You need JDK 17+ to build dScope Apache Camel ISO20022 Component.
