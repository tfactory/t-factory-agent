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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import cesarhernandezgt.dto.AgentDto;
import cesarhernandezgt.dto.InstanceDto;

/**
 * Clase auxiliar para generar las pruebas de integracion. Todos los metodos de
 * esta clase deben de tener la anotacion @Test comentada dado que la
 * implementacion final de cada metodo se formalizan en la clase
 * PruebaArquillian.java.
 * 
 * @author cesarhernandezgt
 *
 */
public class TestEscenarios {

	
	private Client client;
	private WebTarget miTarget;
	


	//update with yor environment and then remove @Ignore annotation from the test case you want to test.
	
	@Before
	public void initClient(){
		this.client = ClientBuilder.newClient();
		this.miTarget = this.client.target("http://localhost:8989/t-factory-agent");
		
		
	}

	//@Ignore because this file is usefull when the arquillian test needs to be changed.
	@Test @Ignore
	public void pruebaResourceAgente(){
		
		this.miTarget = this.miTarget.path("/api/agent");
		Invocation.Builder invocationBuilder = miTarget.request(MediaType.APPLICATION_JSON); 
		//invocationBuilder.header("some-header", "true");

		Response respuesta = invocationBuilder.get();
		
		int codigoRespuestHTTP = respuesta.getStatus();
		
		AgentDto agenteDto = respuesta.readEntity(AgentDto.class);
		
		System.out.println("Codigo: "+codigoRespuestHTTP  );
		System.out.println("Local host name: "+agenteDto.getHostname()  );
		System.out.println(agenteDto.toString());
		
		assertTrue(agenteDto.getStatus().equalsIgnoreCase("ok"));
	}
	
	//@Ignore because this file is usefull when the arquillian test needs to be changed.
	@Test @Ignore
	public void pruebaResourceInstanciaSincronizar(){
		
		this.miTarget = this.miTarget.path("api/instance/synchronize");
		
		InstanceDto objInstanciaDto = null;
		
		Invocation.Builder invocationBuilder = miTarget.request(MediaType.APPLICATION_JSON); 
		//change the instace path you want.
		invocationBuilder.header("instancePath", "/Users/cesarhgt/Instalados/tomcats/apache-tomcat-7.0.59/");

		Response respuesta = invocationBuilder.get();
		
		int codigoRespuestHTTP = respuesta.getStatus();
		
		 objInstanciaDto = respuesta.readEntity(InstanceDto.class);
		
		System.out.println("Codigo de respuesta http: "+codigoRespuestHTTP  );
		//System.out.println("Local host name: "+agenteDto.getHostname()  );
		System.out.println(objInstanciaDto);
		
		assertTrue(objInstanciaDto.getStatus().equalsIgnoreCase("0"));
	}
	
	
	
	
	@Test @Ignore
	public void obtener5PuertosDisponiblesTest(){
		
		List<Integer>  omitidos  = new ArrayList<Integer>();
		
	
		omitidos.add(8987);
		omitidos.add(8988);
		//8989 el puerto en donde esta corriendo el tomcat
		int desde = 8987;
		int hasta = 8999;
		
		try {
			this.miTarget = miTarget.path("api/agent/obtain5AvailablePorts");
			WebTarget miTargetConParamas = miTarget.queryParam("initialRange", desde).queryParam("finalRange", hasta).queryParam("excluded", omitidos.toArray());
			Invocation.Builder invocationBuilder = miTargetConParamas.request(MediaType.APPLICATION_JSON); 
			
			Response respuesta = invocationBuilder.get();
			
			System.out.println("Respuesta HTTP obtenida"+respuesta.getStatus());
			
			if( respuesta.getStatus() == 200){
				List<Integer>  cincoPuertosDisponibles; 
				cincoPuertosDisponibles = respuesta.readEntity(List.class);
				System.out.println("Exito en encontrar 5 puertos disponibles: "+cincoPuertosDisponibles);
				assertTrue(5==cincoPuertosDisponibles.size());
				//assertEquals(5,cincoPuertosDisponibles.size());
			}else{
				System.out.println(" ERORR EN RESPUESTA CODIGO");
				assertTrue(false);
			}
		} catch (Exception e) {
			System.out.println("Error al consumir servicio: obtener5PuertosDisponibles");
			e.printStackTrace();
			assertTrue(false);
		}
	}

	
	//@Test
	/**
	 * Algorithm to change port configuration on server.xml file.
	 */
	public void cambiarPuertosServerXml (){
		
		String pHttp = "2222";
		String pAjp = "4444";
		String pShutDown = "1111";
		String pRedirect = "5555";
		
		//change to your environment.
		String pUbicacionInstancia = "/Users/cesarhgt/Instalados/tomcats/apache-tomcat-8.0.21/";
		
		
		//obtenemos información de archivo server.xml
		Document document = null;
		try {
			File archivoServerXml = new File(pUbicacionInstancia
					+ File.separator + "conf" + File.separator + "server.xml");
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db;
			db = dbf.newDocumentBuilder();
			document = db.parse(archivoServerXml);
			
			//Obteniendo puerto shutdown
			//obtenemos lista de nodos Server
			NodeList nodeList = document.getElementsByTagName("Server");
			//obtenemos el primer nodo: Server del arreglo 
			Element elementoServer = (Element) nodeList.item(0);
			
			//cambiamos el valor que tiene el atributo: PORT para Shutdown 
			elementoServer.getAttributes().getNamedItem("port").setNodeValue(pShutDown);
		
			//obtenemos lista de nodos Connector
			NodeList connectorLista = elementoServer.getElementsByTagName("Connector");
				
			String tipoProtocolo="";
		
			for (int i = 0; i < connectorLista.getLength(); i++) {
				// obtenemos el primer nodo: Connector
				Element elementoConnector = (Element) connectorLista.item(i);

				// obtenemos el texto que tiene el atributo: protocol
				tipoProtocolo = elementoConnector.getAttributes().getNamedItem("protocol").getNodeValue();

				if (tipoProtocolo.equalsIgnoreCase("HTTP/1.1")) {
					//cambiamos el valor que tiene el atributo: PORT y REDIRECT PORT para HTTP 
					elementoConnector.getAttributes().getNamedItem("port").setNodeValue(pHttp);
					elementoConnector.getAttributes().getNamedItem("redirectPort").setNodeValue(pRedirect);					
				} else if (tipoProtocolo.equalsIgnoreCase("AJP/1.3")) {
					//cambiamos el valor que tiene el atributo: PORT y REDIRECT PORT para AJP
					elementoConnector.getAttributes().getNamedItem("port").setNodeValue(pAjp);
					elementoConnector.getAttributes().getNamedItem("redirectPort").setNodeValue(pRedirect);
				}				

			}										 
		} catch (Exception e) {
			System.out.println("Error al intentar parsear Instancia <"+ pUbicacionInstancia+"> :" +e);
		}
		
		//Actualizamos para Re escribir hacia el archivo
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(document);
			StreamResult result = new StreamResult(new File(pUbicacionInstancia
						+ File.separator + "conf" + File.separator + "server.xml"));
			transformer.transform(source, result);
			System.out.println("Se actualizo server.xml de instancia: "+pUbicacionInstancia);
			assertTrue(true);
		} catch (TransformerException e) {
			System.out.println("Error al grabar nuevo server.xml: "+e);
		}	
	}
	
	
	public void uploadZippedFileTest(){
		
	}
	
}
