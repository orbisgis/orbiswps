# OrbisWPS : Open-source Web Processing Service (WPS) libraries. [![Build Status](https://travis-ci.org/orbisgis/orbiswps.png?branch=master)](https://travis-ci.org/orbisgis/orbiswps)


OrbisWPS contains a set of OSGI libraries to build a Web Processing Service [(WPS)](http://www.opengeospatial.org/standards/wps) 
compliant with the 2.0 specification.
OrbisWPS is developed by GIS team of the  [Lab-STICC](http://www.lab-sticc.fr/en/index/) laboratory, located in Vannes.
It has been supported by two research projects:

- [MapUCE](http://www.agence-nationale-recherche.fr/?Projet=ANR-13-VBDU-0004) (Modeling and urbAn Planning laws: Urban 
Climate and Energy, 2014-2018),  ANR Programme: Villes et Bâtiments Durables (VBD) 2013

- [ENERGIC-OD](https://www.energic-od.eu/) Project (European Network for Redistributing Geospatial Information to user 
Communities - Open Data; 2014-2017) partially funded under the ICT Policy Support Programme (ICT PSP) as part of the 
Competitiveness and Innovation Framework Programme by the European Community and the French geographic portal 
[GEOPAL](http://www.geopal.org) of the Pays de la Loire region.

As part of the OrbisGIS platform, OrbisWPS is funded and maintained by personal resources of the Lab-STICC GIS team.

## Groovy API
API for the [Groovy language](www.groovy-lang.org) to write WPS scripts. This module contains the declaration of 
annotations based on the WPS 2.0 standard. The script writing is done in two parts : the input/output declarations and 
the process itself.

The input and output declaration is done by annotated all the variable with `@...Input` or `@...Output`, 
with `...` replaced by the data type. As example :
``` java
@EnumerationInput( title = "title", values = ["round", "flat", "butt", "square"])
String[] enumeration

@LiteralDataInput(title = "title)
String string

@LiteralDataOutput(title = "title)
Long number
```
The available data type are : 
- LiteralData : String and Number like int, long, short ...
- BoundingBox : String representation of a bounding box with the pattern : `authority:code`, like `EPSG:2000`.
- ComplexData extensions :
    - Enumeration : String array representation of a selection of values from a predefined list.
    - Geometry : String representation of a geometry in [WKT](https://wikipedia.org/wiki/Well-known_text).
    - JDBCTable : String representation of a JDBC table.
    - JDBCColumn : String representation of a column of a JDBC table.
    - JDBCValue : String representation of a value of a column of a JDBC table.
    - Password : String representation of secret String.
    - RawData : String representation of a file or forlder, not atter its extension.
    
The process itself is written inside a method called `processing()` (the name can't be changed) annotated `@Process`.

## Scripts
This module contains basic WPS scripts and are loaded in the WPS server by the class `WpsScriptPlugin` thanks to the 
OSGI mechanism.

### How to create a script bundle
A WPS script bundle is composed of two part : a java class which is used to load/unload the scripts inside the service 
and a resource folder containing the scripts.
A maven is available at [orbisgis-plugin_maven-archetype](https://github.com/orbisgis/orbisgis-plugin_maven-archetype]),
 in the module wps-scripts 

#### Bundle structure
The structure of the bundle should respect a typical maven project structure. <br/>
An example a the minimal structure : <br/>
```
root
├─ src
│  └─ main
│     ├─ java
│     │  └─ com
│     │     └─ example
│     │        └─ script
│     │           └─ loader.java
│     └─ resources
│        └─ com
│           └─ example
│              └─ script
│                 ├─ script1.groovy
│                 ├─ script1.groovy
│                 └─ script1.groovy
└─ pom.xml<br/>
```

#### Java
The bundle should contains at least one class extending the interface `WpsScriptBundle` from the module `service-api`. 
In the case of a OSGI bundle, this class should also be annotated as `@Component(immediate = true)` to be recognized by 
the `wpsservice` bundle.<br>

###### getScriptsList()
Return all the URLs of the script to load.<br>
:heavy_exclamation_mark: For a osgi jar, the URL has not the same structure as for a standard jar.<br>
In an OSGI environment, you should use :
```java
FrameworkUtil.getBundle(this.getClass()).findEntries();
```
For a standard jar you should use :
```java
new File().listFiles().get(0).toURI().toURL()
```
###### getGroovyProperties()
Returns all the properties that should be inserted inside the scripts by the groovy engine. Those properties are 
returned inside a `Map` object which key is a `String` which is the name of the variable inside the script and the 
value is an `Object` which is the property itself.<br>
As example, a groovy script with a logger object like :<br>
``` java
logger.info("running the script")
logger.warn("do not do anything")
```
should be loaded by a java class with the method :<br>
```java
@Override
public Map<String, Object> getGroovyProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put("logger",new Logger());
    return properties;
}
```
###### getScriptMetadata()
Return a map of metadata which will be integrated to the process on request. Those metadata are used by the WPS 
client to be able to improve the user interface.
 - IS_REMOVABLE : indicates if the process can be removed (like a custom script) or not (like system script)
 - NODE_PATH : indicates the path to the script to structure then as a tree
 - ICON_ARRAY : an array of icon file path to use them with the NODE_PATH metadata. The last icon should be the icon of
  ths script

###### getI18n()
Return the `I18N` object which will be used to translate the processes human readable string (like process title or 
abstract) on runtime.


## Server module
This module contains the whole WPS mechanism which can be divided into two part : the script parsing and the WPS 
request execution. The WPS service can be customized by instantiating the `WpsServerImpl` class with a customized 
version of the [properties file](https://github.com/orbisgis/orbiswps/blob/master/server/src/main/resources/org/orbisgis/orbiswps/service/utils/basicWpsServer.properties)

### Script parsing
The script parsing is done in 3 steps :
- The script parsing is based on the Groovy parser which convert the groovy files into java classes.
- Then those classes are inspected to search for the groovy annotations defined in the Groovy-API module and to 
generate the JAVA object representation of the process. A part of the model comes from the JAXB generated classes from 
the [ogc-custom-jaxb](https://github.com/orbisgis/ogc-custom-jaxb) library and the model package of the module (package containing the custom extension of the ComplexDataType class).
- To finish the Java object are stored into a cache list and becomes available throw the service.

### WPS request execution
The process execution is done by the `WpsServerImpl.callOperation()` method which receive an `InputStream` object 
containing the WPS 2.0 request.

The request is then parsed and executed. In the case of the `Execute` request, an instance of the java class of the 
desired script is instantiated and configured with the input. Then the method `processing()` is executed. Once the 
execution end reached, the output data a retrieved.

## Client API
API interface and classes for the creation of a WPS client compatible with the server.