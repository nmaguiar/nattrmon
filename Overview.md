

# Configuration #

The configuration of this tool is contained on a XML file.

This XML file contains 3 areas:

  * Settings
  * Services
  * Outputs

## Settings ##

On the settings area, the general settings to be used by the tool are defined (for example: the logging level).

Additionally it also includes which services and outputs types (explained below) will be available. This enables the tool to be flexible to accommodate new types of services and outputs without changing the configuration format.

## Services ##

On the services area, it’s defined where the tool will gather it’s input data. Each source, for this input data, is called a Service. A Service can be a connection to a database, the execution of commands on the operating system (shell), a Java JMX connection, etc.. This means that there are several Service types that can be used. Each one of these service types must be included previously on the Settings area in order to be used.

Inside this Services area it’s possible to declare one or more than one service to be used.

### Services configuration example ###

```
<services>
  <service url="jdbc:oracle:thin:@//1.2.3.4:1521/ORDB"
           params="login=scott;password=tiger">
     [...]
  </service>
  <service url="shell:">

     [...]
  </service>
</services>
```

### Services syntax ###

| **<service url="serviceType:something"  [params="param1=value1;param2=value2"]>** |
|:----------------------------------------------------------------------------------|
| The 

&lt;service&gt;

 tag is composed of the following properties:<p><li><b>url</b> – The URL to access the service where the prefix before the first ‘:’ defines it.</li><li><b>param</b> – Any parameters specific to the service. </tbody></table>

### Services detail ###

There are 2 levels of detail for the type of information that it’s necessary to gather from each service. These levels are mandatory. They are:

  * Objects
  * Attributes

Each service should contain one or more objects. And each object should contain one or more attributes.

## Objects ##

Objects define an element provided by a service that is of interest to gather information.

For example, for a database connection an Object can be a SQL query to execute; for executing commands, it can be the complete command line to execute; for a Java JMX connection it can be the JMX object path;

Having more than one object allows to reuse the service to retrieve different information. For example, for a SQL query it allows to perform several SQL queries; for executing commands, several commands; for Java JMX to access several JMX objects.

### Objects configuration example ###

```
<services>
   <service url="jdbc:oracle:thin:@//1.2.3.4:1521/ORDB"
            params="login=scott;password=tiger">
      <object name="select sysdate d, count(1) from queuetable">   
      [...]
      </object>
      <object name="select count(1) from events">   
      [...]
      </object>
   </service>
</services>
```

### Objects syntax ###

| **<object name="objectPath" [params="param1=value1;param2=value2"]>** |
|:----------------------------------------------------------------------|
| The 

&lt;object&gt;

 tag is composed of two possible properties:<p><li><b>name</b> - An unique name/path, within the service type which defines a service object.</li><li><b>params</b> - Any parameters specific to the object.</li> </tbody></table>

<h2>Attributes ##

For each Object it’s possible to define one or more Attributes. This will be the exact information, from each object, that needs to be gather.

For example, for a SQL query object, the attributes will be the result set fields; for a command line object it will be the position of the returned output of the command; for a Java JMX object it will be corresponding attributes.

For each attribute a unique identifier (uid) should be specified. This is the id that will be used to identify each attribute for output.

### Attributes configuration example ###

```
<services>
   <service url="jdbc:oracle:thin:@//1.2.3.4:1521/ORDB"
            params="login=scott;password=tiger">
      <object name="select sysdate d, count(1) from queuetable">   
         <attribute uid="SQL_DATE"  name="d"/>
         <attribute uid="SQL_COUNT" name="2" type="id"/>      
      </object>
      <object name="select count(1) from events">   
         <attribute uid="EVENTS_COUNT" name="1" type="id"/>
      </object>
   </service>
</services>
```

### Attributes syntax ###

| **<attribute uid="aUID" name="aName" [type=aType]>** |
|:-----------------------------------------------------|
| The 

&lt;attribute&gt;

 is composed of three possible properties:<p><li><b>uid</b> - An global unique name to the entire file. The value will be assigned to it in each interaction.</li><li><b>name</b> - Depending on the attribute type, defines a path/id to identify the attribute within an object.</li><li><b>type</b> – An attribute can be Simple, Reflect, Name or Id. Default is Simple.<p>Attribute types description:<p><li><b>Simple</b> - This is the basic attribute type. The 'name' defines completely the attribute within the corresponding object.</li><li><b>Name</b> - The 'name' defines an alphanumeric way to define the attribute (for example: the name of a field in an object database query).</li><li><b>Id</b> - The 'name' defines a numeric way to define the attribute (for example: the position of a field in an object database query).</li><li><b>Reflect</b> - The 'name' defines a java field that must be retrieved using java reflection (for example: a custom jmx field of an java JMX object).</li> </tbody></table>

<h2>Outputs</h2>

On the outputs area, a set of outputs can be defined (e.g. console, database, jmx, etc.). For each defined output, the tool will cycle through all the attributes identified for output (using the specified attribute uid (unique identifier) retrieving the corresponding values.<br>
<br>
On each cycle the value for an attribute uid is retrieved only one time for consistency.<br>
<br>
<pre><code>&lt;outputs&gt;<br>
   &lt;output type="orderOutput"&gt;<br>
      SQL_DATE, SQL_COUNT, EVENTS_COUNT<br>
   &lt;/output&gt;<br>
&lt;/outputs&gt;<br>
</code></pre>

The tool will also automatically decide to retrieve values in parallel or sequentially in order to minimize delays.<br>
<br>
The output will be triggered in parallel so it’s recommended to always have one attribute as the timestamp (please refer to the internalStats service to provide this value if necessary).<br>
<br>
<h1>Running</h1>

<pre><code>java –jar nattrmon.jar [parameters]<br>
</code></pre>

The parameters that can be used are:<br>
<br>
<table><thead><th> Parameter Name </th><th> Parameter Description </th></thead><tbody>
<tr><td> -c <code>[file]</code> </td><td> Provide the XML configuration file (should follow the format specified in the Configuration chapter) </td></tr>
<tr><td> <code>[first parameter]</code> </td><td> Number of seconds between each attribute values retrieval cycle (if none specified, 1 second) </td></tr>
<tr><td> <code>[second parameter]</code> </td><td> The number of attribute values retrieval cycles to execute (if none specified, infinite) </td></tr></tbody></table>

Example<br>
<br>
<pre><code>nattrmon.sh 1 &gt; nattrmon.output<br>
</code></pre>