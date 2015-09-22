# t-factory-agent

t-factory-agent is the agent on every remote server that need to be managed by the t-Factory-Server.
For more information about the architecture of tFactory you can check out <https://github.com/tfactory>

## Table of contents
* [Creator](#Creator)
* [Installation](#Installation)
* [Features](#Features)
* [Contributing](#Contributing)
* [Documentation](#Documentation)
* [BugsAndFeatureRequests](#BugsAndFeatureRequests)
* [Roadmap](#Roadmap)
* [CopyrightAndLicense](#CopyrightAndLicense)


##Creator

**Cesar Hernandez**

* <https://twitter.com/CesarHgt>
* <https://cesarhernandezgt.blogspot.com> 

##Installation
* Download the [latest release] (https://github.com/tfactory/t-factory-agent/releases) (.war) file and deploy it on any Tomcat 7 instance. JRE 7 or latest should be used the the Tomcat instance.
* Start the tomcat instance.
* Deploy the t-factory-agent.war file

For server configuration see: [t-factory-server] (https://github.com/tfactory/t-factory-server/) 

##Features
The following service are avialable:

* Return information about the running agent. 
* Provided 5 available ports on host server. This ports are used by the t-factory-server in order to create new tomcat instances with valid port (http, jpa, shutdown, redirect and jxm). 
* Receives a remote .zip file, name and path inside the host server in order to be copied.
* Unzip and rename the instance template folder previosly downloaded.
* Return standard information of a tomcat instance.
* Update the configuration derived from server.xml from a tomcat instance.


## Contributing
You are wellcome to improve the software. Be sure to check: opening issues, coding standards, and notes on development code.

## Documentation
You can find enough documentation by the generation of the javaDoc of this project.

## BugsAndFeatureRequests
You can check: [open and closed issues.](https://github.com/tfactory/t-factory-agent/issues/new)

## Roadmap
This is the list of the upcoming features. (Looking forward to have your coding contribution):
* Authentication and Authorization 
* Log4j incorporation
* Configure memory parameters to instances when they are created from the tFactory server.
* Configure jmx port parameter to instances when they are created from the tFactory server.
* Instnaces Datasource managements.


## CopyrightAndLicense
Code released under [Apache License Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).


