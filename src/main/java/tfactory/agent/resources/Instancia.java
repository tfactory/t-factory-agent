/**
 * Copyright (C) 2015 Cesar Hernandez. (https://github.com/tfactory)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package tfactory.agent.resources;


import java.io.File;
import java.util.Date;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cesarhernandezgt.dto.InstanceDto;
import cesarhernandezgt.dto.ServerXml;

@Path("/instance")
public class Instancia {

	/**
	 * Get method that return standard information of a tomcat instance.
	 * @param pUbicacionInstancia Path of the instance to be readed.
	 * @return Object InstanceDto.java containing instance information.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/synchronize")
	public InstanceDto obtenerInstanciaInfo(@HeaderParam("instancePath") String pUbicacionInstancia ){
		
		Date timeStamp = new Date();
		System.out.println("\n"+timeStamp + "- Start obtenerInstanciaInfo(): "+pUbicacionInstancia);
	
		InstanceDto instanciaObtenida = obtenerInstancia(pUbicacionInstancia);
		
		return instanciaObtenida;
	}
	
	
	
	/**
	 * Put method to update the configuration derived from server.xml from a tomcat instance.
	 * @param pUbicacion Instance Path.
	 * @param pHttp New Http port.
	 * @param pAjp New Ajp port.
	 * @param pShutDown New Shutdown port.
	 * @param pRedirect New Redirect port.
	 * @return code 200 Ok if succed, otherwise 403 with message error.
	 */
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	@Path("/update/serverxml")
	public Response actualizarServerxml(@HeaderParam("instancePath") String pUbicacionInstancia, ServerXml pServerXml){
		
		Date timeStamp = new Date();
		System.out.println("\n"+ timeStamp + " - Start actualizarServerxmlInstancia(): "+pUbicacionInstancia);
		
		if(actualizarServerXml(pUbicacionInstancia, pServerXml.getHttp(), pServerXml.getAjp(), pServerXml.getShutDown(), pServerXml.getHttpRedirect(), pServerXml.getAjpRedirect())){
			return Response.ok("Instance updated successfully.").build();
		}else{
			return Response.status(403).entity("There was an error trying to update the file Server.xml from the instance.").build();
		}
	}
	
	
	
	
	
	/**
	 * Upadte the port information of server.xml file from an tomcat instance.
	 * @param pUbicacionInstancia InstancePath
	 * @param pHttp
	 * @param pAjp
	 * @param pShutDown
	 * @param pRedirect
	 * @return Boolean indication true (success) false (error).
	 */
	private boolean actualizarServerXml(String pUbicacionInstancia,  int pHttp, int pAjp,int pShutDown,  int pHttpRedirect, int pAjpRedirect ){
		System.out.println(" Http: ["+pHttp+"], Ajp:["+pAjp+"], Shutdown: ["+pShutDown+"], Http Redirect: ["+pHttpRedirect+"], Ajp Redirect: ["+pAjpRedirect+"]");
		
		//validate the instance paht, the existence of conf folder and the existence of server.xml file.
		int estadoValidacion = validaUbicacion(pUbicacionInstancia); 
		if(estadoValidacion != 0){	
			System.out.println(" Error: validation of instance path, conf folder and server.xml failed.");
			return false;			
		}
		
		//verify port format and range compliance with grather than 0 and shorter than 99999
		int min = 0;
		int max = 99999;
		if( pHttp > 0  && pHttp < max  && pAjp > 0 && pAjp < max && pShutDown > 0 && pShutDown < max && pHttpRedirect > 0 && pHttpRedirect < max && pAjpRedirect > 0 && pAjpRedirect < max){
			
				//obtaining information from server.xml file
				Document document = null;
				try {
					File archivoServerXml = new File(pUbicacionInstancia
							+ File.separator + "conf" + File.separator + "server.xml");
					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
					DocumentBuilder db;
					db = dbf.newDocumentBuilder();
					document = db.parse(archivoServerXml);
					
					//Obtaining shutdown port.
					//Obtaingint list of Server nodes.
					NodeList nodeList = document.getElementsByTagName("Server");
					//obtaining the firts "Server" node from the array. 
					Element elementoServer = (Element) nodeList.item(0);
					
					//we change the SHUTDOWN port value. 
					elementoServer.getAttributes().getNamedItem("port").setNodeValue(Integer.toString(pShutDown));
					
					//Obtaining list of Connector nodes.
					NodeList connectorLista = elementoServer.getElementsByTagName("Connector");
					
					String tipoProtocolo="";
					
					for (int i = 0; i < connectorLista.getLength(); i++) {
						// Obtaining firt node of type: Connector
						Element elementoConnector = (Element) connectorLista.item(i);
						
						// obtenemos el texto que tiene el atributo: protocol
						tipoProtocolo = elementoConnector.getAttributes().getNamedItem("protocol").getNodeValue();
						
						if (tipoProtocolo.equalsIgnoreCase("HTTP/1.1")) {
							//changing the PORT and REDIRECT PORT for the connector HTTP 
							elementoConnector.getAttributes().getNamedItem("port").setNodeValue(Integer.toString(pHttp));
							elementoConnector.getAttributes().getNamedItem("redirectPort").setNodeValue(Integer.toString(pHttpRedirect));					
						} else if (tipoProtocolo.equalsIgnoreCase("AJP/1.3")) {
							//Changing the PORT and REDIRECT PORT values for AJP
							elementoConnector.getAttributes().getNamedItem("port").setNodeValue(Integer.toString(pAjp));
							elementoConnector.getAttributes().getNamedItem("redirectPort").setNodeValue(Integer.toString(pAjpRedirect));
						}				
					}										 
				} catch (Exception e) {
					System.out.println(" Error trying to parse the Instance <"+ pUbicacionInstancia+"> :" +e);
					return false;
				}
				
				//update the file server.xml
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = null;
				try {
					transformer = transformerFactory.newTransformer();
					DOMSource source = new DOMSource(document);
					StreamResult result = new StreamResult(new File(pUbicacionInstancia
							+ File.separator + "conf" + File.separator + "server.xml"));
					transformer.transform(source, result);
					System.out.println(" Success: server.xml updated from instance: "+pUbicacionInstancia);
					return true;
				} catch (TransformerException e) {
					System.out.println(" Error when saving new server.xml file: "+e);
					return false;
				}	
		}else{
			System.out.println(" Error, one of the new ports is not in valid range ("+min+","+max+").");
			return false;
		}
	}
	
	
	
	/**
	 * Obtain the information of port from a server.xml of an instance.
	 * @param pUbicacion Intance Path.
	 * @return Object InstanceDto.java containing the server.xml information.
	 */
	private InstanceDto obtenerInstancia(String pUbicacion){
		System.out.println(" Searching: "+pUbicacion);
		
		InstanceDto objIntanciaDto = new InstanceDto();
		
		//validate the instance paht, the existence of conf folder and the existence of server.xml file.
		int estadoValidacion = validaUbicacion(pUbicacion); 
		if(estadoValidacion != 0){	
			objIntanciaDto.setStatus(String.valueOf(estadoValidacion));
			return objIntanciaDto;			
		}
				
		//We start file parsing to obtain port values.
		boolean banderaMultiplesConectors = false;
		//obtenemos información de archivo server.xml
		try {
			File archivoServerXml = new File(pUbicacion
					+ File.separator + "conf" + File.separator + "server.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			Document document;
			document = db.parse(archivoServerXml);
			
			//Obtaining SHUTDOWN port.
			//obtaining list of nodes: Server
			NodeList nodeList = document.getElementsByTagName("Server");
			//obtaining the first node Server from the array. 
			Element elementoServer = (Element) nodeList.item(0);
			//obtaining shutdown port number.
			String puertoShutdownStr = elementoServer.getAttributes().getNamedItem("port").getNodeValue();
			int puertoShutdownInt = Integer.parseInt(puertoShutdownStr);
			
		
			//obtaining list of Connector nodes.
			NodeList connectorLista = elementoServer.getElementsByTagName("Connector");
				
			String tipoProtocolo="";
			String strPuertoHttp = "";
			String strPuertoRedirectHttp = "";
			String strPuertoAjp = "";
			String strPuertoRedirectAjp = "";
			
			if( connectorLista.getLength() > 2){
				banderaMultiplesConectors = true;
			}
			
			for (int i = 0; i < connectorLista.getLength(); i++) {
				// obtaining the first node of type: Connector
				Element elementoConnector = (Element) connectorLista.item(i);

				// obtaining text of protocol attribute.
				tipoProtocolo = elementoConnector.getAttributes().getNamedItem("protocol").getNodeValue();

				if (tipoProtocolo.equalsIgnoreCase("HTTP/1.1")) {
					strPuertoHttp = elementoConnector.getAttributes().getNamedItem("port").getNodeValue();
					strPuertoRedirectHttp = elementoConnector.getAttributes().getNamedItem("redirectPort").getNodeValue();

				} else if (tipoProtocolo.equalsIgnoreCase("AJP/1.3")) {
					strPuertoAjp = elementoConnector.getAttributes().getNamedItem("port").getNodeValue();
					strPuertoRedirectAjp = elementoConnector.getAttributes().getNamedItem("redirectPort").getNodeValue();
				}				

			}
			
			System.out.println("HTTP <"+strPuertoHttp+"> <"+strPuertoRedirectHttp+">  AJP <"+strPuertoAjp+"> <"+strPuertoRedirectAjp+">  Shutdown<"+puertoShutdownInt+">");

			//Creation of serverXml object.
			ServerXml serverXmlObj = new ServerXml();
			serverXmlObj.setHttp ( Integer.parseInt(strPuertoHttp) );
			serverXmlObj.setHttpRedirect(Integer.parseInt(strPuertoRedirectHttp));
			serverXmlObj.setAjp(Integer.parseInt(strPuertoAjp));
			serverXmlObj.setAjpRedirect(Integer.parseInt(strPuertoRedirectAjp));
			serverXmlObj.setShutDown(puertoShutdownInt);
			
			//InstanciaDto instanciaDto = new InstanciaDto();
			objIntanciaDto.setServerXml(serverXmlObj);
			
			File ubicacionPath = new File(pUbicacion);
			objIntanciaDto.setName(ubicacionPath.getName());
			objIntanciaDto.setPathLocation(ubicacionPath.getAbsolutePath()); //con esto prevenimos que grabe diagonal al  final.
		} catch (Exception e) {
			System.out.println("Error trying parse intance <"+ pUbicacion+"> :" +e);
			objIntanciaDto.setStatus("2");
			return objIntanciaDto;
		}
		
		if(banderaMultiplesConectors){
			System.out.println("ERORR On path :<"+pUbicacion+"> were found more connectors apart from HTTP y AJP");
			objIntanciaDto.setStatus("1");
			return objIntanciaDto;
		}else{
			objIntanciaDto.setStatus("0");
			return objIntanciaDto;
		}
		
	}
	
	
	/**
	 * Validate a instance path for the existence of conf folder and
	 * server.xml file.
	 * 
	 * @param ubicacion
	 *            Instance Path.
	 * @return 0 if succed, otherwise error code.
	 */
	private int validaUbicacion(String ubicacion){
		File pathInstanciaObtenida = null;
		File pathTomcatConf = null;
		File pathServerXml = null;
		
		try {
			pathInstanciaObtenida = new File(ubicacion);
			
			if(pathInstanciaObtenida.exists()){
				pathTomcatConf = new File(pathInstanciaObtenida+File.separator+"conf");
				if(pathTomcatConf.exists()){
					pathServerXml = new File(pathInstanciaObtenida+File.separator+"conf"+File.separator+"server.xml");
					
					if(pathServerXml.exists()){
						return 0;//todo exitoso ;)
					}else {
						System.out.println("ERROR server.xml was not found at instace path <"+pathInstanciaObtenida+">.");
						return 6;
					}
				}else{
					System.out.println("ERROR conf folder was not found on instance path <"+pathInstanciaObtenida+">.");
					return 5;
				}
			}else{
				System.out.println("ERROR path does not exist <"+pathInstanciaObtenida+">.");
				return 4;
			}
			
		} catch (Exception e) {
			System.out.println("ERROR trying validate path and/or files from path <"+pathInstanciaObtenida+">: "+e);
			return 3;
		}
		
	}	
	
}
