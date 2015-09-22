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
package tfactory.agent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import tfactory.agent.conf.Aplicacion;
import tfactory.agent.resources.Agente;
import tfactory.agent.resources.Instancia;
import cesarhernandezgt.dto.AgentDto;
import cesarhernandezgt.dto.InstanceDto;
import cesarhernandezgt.dto.ServerXml;




/**
 * Class containing the integration test for the Web Service
 * using Arquillian, shrinkwrap and embeed tomcat tha is 
 * @author cesarhernandezgt
 *
 */
@RunWith(Arquillian.class)
public class PruebaArquillianTest extends Assert {

	/**
	 * Inital configuration for the WebArchive (war) generated by arquillian.
	 * @return
	 */
	 @Deployment
	 public static WebArchive createDeployment2(){
		 
		File[] files = Maven
				.resolver()
				.resolve(
						"org.glassfish.jersey.containers:jersey-container-servlet:2.17",
						"org.glassfish.jersey.media:jersey-media-json-jackson:2.17",
						"cesarhernandezgt:t-factory-utils:0.0.1",
						"org.glassfish.jersey.media:jersey-media-multipart:2.17",
						"net.lingala.zip4j:zip4j:1.3.2")
				.withTransitivity().asFile();
		
		
		final WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "t-factory-ws.war").as(ZipImporter.class).as(WebArchive.class).addClasses(Aplicacion.class, Instancia.class, Agente.class).setWebXML("web.xml").addAsLibraries(files);
		
		System.out.println(webArchive.toString(true));
		
		return webArchive;
	 }
	 
	 
	 
	 
	 
	/**
	 * Conatins the reference context URL from the embedded tomcat 
	 * in which the test will run.
	 */
	 @ArquillianResource
	 private URL webappUrl;
	 

	
	 
	 
	 /**
	  * Method for cleannup files from previos executions of the test,
	  * this method is executed before each TestSuit.
	  */
	@BeforeClass
	 public static void prepararAmbiente(){
		 System.out.println("\n### Preparing Environment for Integration Testing ###");
		 String tomcatHome = "target/tomcat-embedded-7";//same that the one configured on arquillian.xml
		 boolean bandera = false;
		 try {
				//We try to delete folders from previos executions
				File folderZipOriginal = new File(tomcatHome+"/zipOriginal");
				File folderZipDestino = new File(tomcatHome+"/zipUploaded");
				
				if (folderZipOriginal.exists()) {
					delete(folderZipOriginal);
					System.out.println(" Deleted folder :" + tomcatHome+"/zipOriginal");
					bandera = true;
				} 
				if (folderZipDestino.exists()) {
					delete(folderZipDestino);
					System.out.println(" Deleted folder:" + tomcatHome+"/zipUploaded");
					bandera = true;
				} 
				if(!bandera){
					System.out.println(" The enviroment is clean, no previos executions files were found. ");
				}
		
		} catch (Exception e) {
			System.out.println(" There was a Error trying to clean the execution environment from previos integration tests. ");
		}
	 }
	 
	  
	 
	/**
	 * Consumes the REST WS, makes a GET in order ot obtain the information
	 * of an agent (AgentDto.java). If the call succed the status attribute
	 * of the object should be OK, otherwise a error message would be 
	 * recieved. (http://xxx.xxx.xxx.xxx:xxxx/t-factory-agent/api)
	 */
	 @Test @RunAsClient @InSequence(1)
		public void obtenerAgenteSrv(){
		 	System.out.println("\n### Integration test No.1 ###");
			Client cliente = ClientBuilder.newClient();
			AgentDto objServidorDto; 
			
			try {
				WebTarget miTarget = cliente.target(webappUrl+"api/agent");
				Invocation.Builder invocationBuilder = miTarget.request(MediaType.APPLICATION_JSON); 
				Response respuesta = invocationBuilder.get();
				
				System.out.println("Response HTTP code obtained from <"+webappUrl+">: "+respuesta.getStatus());
				
				if( respuesta.getStatus() == 200){
					objServidorDto = respuesta.readEntity(AgentDto.class);
					System.out.println("Remote fake server status: "+objServidorDto.getStatus());
					assertEquals("ok",objServidorDto.getStatus());
				}else{
					System.out.println(" ERROR CODE OBTAINED IN THE RESPONSE");
					assert(false);
				}
			} catch (Exception e) {
				System.out.println("Error trying consuming the service: "+webappUrl);
				//e.printStackTrace();
				objServidorDto = new AgentDto();
				objServidorDto.setStatus("Invalid URL.");
			}
		}

	 
	 	/**
	 	 * Test the service: /api/agent/obtener5AvailablePorts
	 	 */
	 	@SuppressWarnings("unchecked")
		@Test @RunAsClient @InSequence(2)
		public void obtener5PuertosDisponiblesTest(){
	 		System.out.println("\n### Integration test No.2 ###");
	 		Client cliente = ClientBuilder.newClient();
			
			List<Integer>  omitidos  = new ArrayList<Integer>();
			
			int desde = 8080;
			int hasta = 8890;
			omitidos.add(8880);
			omitidos.add(8884);
			omitidos.add(8885);
			omitidos.add(8886);
			omitidos.add(8887);
			// 8888 is the port in with the embbed tomcat managed by arquillian is running.
	
			try {
				WebTarget miTarget = cliente.target(webappUrl+"api/agent/obtain5AvailablePorts");
				WebTarget miTargetConParamas = miTarget.queryParam("initialRange", desde).queryParam("finalRange", hasta).queryParam("excluded", omitidos.toArray());
				Invocation.Builder invocationBuilder = miTargetConParamas.request(MediaType.APPLICATION_JSON); 
				
				Response respuesta = invocationBuilder.get();
				
				System.out.println("Http code obtained by service <"+webappUrl+">: "+respuesta.getStatus());
				
				if( respuesta.getStatus() == 200){
					List<Integer>  cincoPuertosDisponibles; 
					cincoPuertosDisponibles = respuesta.readEntity(List.class);
					System.out.println("5 ports were found successfully: "+cincoPuertosDisponibles);
					assertTrue(5==cincoPuertosDisponibles.size());
					//assertEquals(5,cincoPuertosDisponibles.size());
				}else{
					System.out.println(" ERROR RESPONSE CODE OBTAINED");
					assertTrue(false);
				}
			} catch (Exception e) {
				System.out.println("Error trying consume the service: "+webappUrl);
				e.printStackTrace();
				assertTrue(false);
			}
		}
	 	
	 	
	 	/**
	 	 * Test the service: /api/agent/upload/instance
	 	 */
	 	@Test @RunAsClient @InSequence(3) 
	 	public void uploadZippedFileTest(){
	 		System.out.println("\n### Integration test No.3 ###");
	 		//Client cliente = ClientBuilder.newClient();
	 		String nombreInstanciaZip = "instancia-prueba.zip";
	 		String tomcatHome = "target/tomcat-embedded-7";//the same configured in arquillian.xml
	 		
	 		//Adding /test/resources/instancia-prueba/*
	 		JavaArchive archive = ShrinkWrap.create(JavaArchive.class,nombreInstanciaZip).addAsResource("instancia-prueba");
	 		
			//Creatin of folders for the Original Zip file and the Uploaded Zip File.
			File folderZipOriginal = new File(tomcatHome+"/zipOriginal");
			folderZipOriginal.mkdir();
			 
			File folderZipDestino = new File(tomcatHome+"/zipUploaded");
			folderZipDestino.mkdir();
			
			//Creation of  zip in path: target/tomcat-embedded-7/zipOriginal
			archive.as(ZipExporter.class).exportTo(new File(folderZipOriginal.getAbsolutePath()+"/"+nombreInstanciaZip), true);
	 		System.out.println(archive.toString(true));
	 		 
	 		//Take reference of Zip that will be sended to the WebService.
	 		File archivoAsubirAlWS = new File(folderZipOriginal.getAbsolutePath()+"/"+nombreInstanciaZip);

	 		//***Make the Zip Upload.***
	 		//Posteriormente estos metodos deben de ser encapsulados en un jar para ser utilizado en facotry, agente y otros.
	 		assertTrue(enviarInstanciaAServidorRemoto(webappUrl.toString(), archivoAsubirAlWS , folderZipDestino.getAbsolutePath()));	  
	 	}
	 	

		/**
		 * Test the service : /api/agent/unzip/instance
		 */
		@Test @RunAsClient @InSequence(4)
		public void unzipInstanciaTest(){
			System.out.println("\n### Integration test No.4 ###");
			File pUbicacionInstanciaRemota = new File("target/tomcat-embedded-7/zipUploaded");
			String pNombreActual = "instancia-prueba.zip";
			String pNombreNuevo = "nuevo-tomcat-test";
			assertTrue(descompresionRenombradoRemoto(webappUrl.toString(), pUbicacionInstanciaRemota.getAbsolutePath(), pNombreActual, pNombreNuevo));
		}

		
		/**
		 * Test service: /api/instance/update/serverxml
		 */
		@Test @RunAsClient @InSequence(5)
		public void actualizarServerXmlTest(){
			System.out.println("\n### Integration test No.5 ###");
			File pUbicacionInstanciaRemota = new File("target/tomcat-embedded-7/zipUploaded");
			String pNombreInstancia = "nuevo-tomcat-test";
			int pHttp = 2222;
			int pAjp = 4444;
			int pShutdown = 1111;
			int pHttpRedirect = 3333;
			int pAjpRedirect = 5555;
			
			ServerXml pServerXml = new ServerXml();
			pServerXml.setHttp(pHttp);
			pServerXml.setAjp(pAjp);
			pServerXml.setShutDown(pShutdown);
			pServerXml.setAjpRedirect(pAjpRedirect);
			pServerXml.setHttpRedirect(pHttpRedirect);
			
			//Consuming the REST service
			assertTrue(actualizaArchivoServerXmlRemoto(webappUrl.toString(), pUbicacionInstanciaRemota.getAbsolutePath(), pNombreInstancia, pServerXml));
		}

		
		/**
		 * Test service: /api/instancia/sincronizar
		 */
		@Test @RunAsClient @InSequence(6)
		public void obtenerInstanciaInfoTest(){
			System.out.println("\n### Integration test No.6 ###");
			InstanceDto  pInstDtoObtenida = null;
			File pUbicacionInstanciaRemota = new File("target/tomcat-embedded-7/zipUploaded");
			String pNombreInstancia = "nuevo-tomcat-test";
			pInstDtoObtenida = obtenerInstanciaDto(webappUrl.toString(), pUbicacionInstanciaRemota.getAbsolutePath()+"/"+pNombreInstancia);
			
			//Comparing obtained port the should be the one modified after the test excecution. actualizarServerXmlTest()
			assertTrue(pInstDtoObtenida != null && pInstDtoObtenida.getServerXml().getShutDown() == 1111);
		}
		
		
/* **********************************
 * AUXILIAR METHODS FOR TEST CASE.
 * **********************************/
		
		 /**
		  * Delete files and folder recursively form the OS
		  * @param file
		  * @throws IOException
		  */
		 private static void delete(File file)
			    	throws IOException{
			 
			    	if(file.isDirectory()){
			 
			    		//directory is empty, then delete it
			    		if(file.list().length==0){
			 
			    		   file.delete(); 
			    		   System.out.println(" Deleted directory: "+ file.getAbsolutePath());
			 
			    		}else{
			 
			    		   //list all the directory contents
			        	   String files[] = file.list();
			 
			        	   for (String temp : files) {
			        	      //construct the file structure
			        	      File fileDelete = new File(file, temp);
			 
			        	      //recursive delete
			        	     delete(fileDelete);
			        	   }
			 
			        	   //check the directory again, if empty then delete it
			        	   if(file.list().length==0){
			           	     file.delete();
			        	     System.out.println(" Deleted directory: " + file.getAbsolutePath());
			        	   }
			    		}
			 
			    	}else{
			    		//if file, then delete it
			    		file.delete();
			    		System.out.println(" Deleted file: " + file.getAbsolutePath());
			    	}
		}
	 	
	 	/**
		 * Sent a HTTP Multipart POST with a tomcat template in zip format.
		 * @param pUrlServidorRemoto  Url del servidor remoto donde se encuentra el agente.
		 * @param pArchivoPlantillaZip Plantilla de instancia tomcat en formato zip.
		 * @param pUbicacionDestino Ubicacion del servidor remoto en donde se depositara el zip.
		 * @return
		 */
		private boolean enviarInstanciaAServidorRemoto(String pUrlServidorRemoto, File pArchivoPlantillaZip, String pUbicacionDestino){
			System.out.println("Start to consume service: "+pUrlServidorRemoto+"api/agent/upload/instance");
			boolean respuesta = false;
			
			ClientConfig clientConfig = null;
			WebTarget miTarget = null;
			FileDataBodyPart fileDataBodyPart = null;
		    FormDataMultiPart formDataMultiPart = null;
		    Invocation.Builder invocationBuilder = null;
	        Response response = null;
	        int responseCode;
	        String responseMessageFromServer = null;
	        String responseString = null;
	        
			try {
				clientConfig = new ClientConfig();
				clientConfig.register(MultiPartFeature.class);
				Client cliente =  ClientBuilder.newClient(clientConfig);
				miTarget = cliente.target(pUrlServidorRemoto+"api/agent/upload/instance");
				
				 // set file upload values
	            fileDataBodyPart = new FileDataBodyPart("uploadFile", pArchivoPlantillaZip, MediaType.APPLICATION_OCTET_STREAM_TYPE);
	            formDataMultiPart = (FormDataMultiPart) new FormDataMultiPart().field("locationPath",pUbicacionDestino).bodyPart(fileDataBodyPart);
				
	            // invoke service
	            invocationBuilder = miTarget.request();
	            response = invocationBuilder.post(Entity.entity(formDataMultiPart, MediaType.MULTIPART_FORM_DATA));
	            
	            // get response code
	            responseCode = response.getStatus();
	            System.out.println(" HTTP code obtained: " + responseCode);
	            
	            if (response.getStatus() != 200) {
	            	//esto es lo que se tiene que enviar a Globito
	                System.out.println(" Error sending the file, obtained error code <" + responseCode + "> with message: "+response.readEntity(String.class));
	                respuesta = false;
	            }else{
	            	// get response message
	                responseMessageFromServer = response.getStatusInfo().getReasonPhrase();
	                System.out.println(" ResponseMessageFromServer: " + responseMessageFromServer);
	     
	                // get response string
	                responseString = response.readEntity(String.class);
	                System.out.println(" Response String FromServer: " + responseString);
	                respuesta = true;
	            }
			} catch (Exception e) {
				System.out.println(" Error: "+e);
			
				respuesta = false;
			}finally{
	            // release resources, if any
	            fileDataBodyPart.cleanup();
	            formDataMultiPart.cleanup();
	            try {
					formDataMultiPart.close();
				} catch (IOException e) {
					System.out.println("ERROR trying to close formDataMultiPart.close(): "+e);
				}
	        }
			return respuesta;
		}

		
		/**
		 * Consume the rest WS in order to unxip a existing tomcat instance zip file on a remote server.
		 * @param pServidorUrl
		 * @param pUbicacionInstanciaRemota
		 * @param pNombreActual
		 * @param pNombreNuevo
		 * @return
		 */
		private boolean descompresionRenombradoRemoto(String pServidorUrl, String pUbicacionInstanciaRemota, String pNombreActual, String pNombreNuevo){
			System.out.println("Start consuming service: "+pServidorUrl+"api/agent/unzip/instance");
			boolean resultado = false;

			
			try {
				Client cliente = ClientBuilder.newClient();
				WebTarget miTarget = cliente.target(pServidorUrl+"api/agent/unzip/instance");
				WebTarget miTargetConParamas = miTarget.queryParam("pathLocation", pUbicacionInstanciaRemota).queryParam("templateName", pNombreActual).queryParam("newName", pNombreNuevo);
				Invocation.Builder invocationBuilder = miTargetConParamas.request(MediaType.APPLICATION_JSON);
				Response respuesta = invocationBuilder.get();
				
				
				
				if( respuesta.getStatus() == 200){
					// get response message
	                
					String responseMessageFromServer = respuesta.getStatusInfo().getReasonPhrase();
	                System.out.println(" ResponseMessageFromServer: " + responseMessageFromServer);
					resultado = true;
				}else{
					String responseMessageFromServer = respuesta.getStatusInfo().getReasonPhrase();
	                System.out.println(" ResponseMessageFromServer: " + responseMessageFromServer);
	                
					System.out.println(" ERROR, obtained HTTP Code: "+respuesta.getStatus());
					resultado = false;
				}
			} catch (Exception e) {
				System.out.println(" Error trying to consume service: "+pServidorUrl);
				System.out.println(e);
				resultado = false;
			}
			return resultado;
		}

		
		/**
		 * Makes a HTTP PUT on the Rest WS in order to update the port configuration of a server.xml file
		 * from a remote tomcat instance. 
		 * @param pServidorUrl Servidor remoto en donde se encuentra el agente.
		 * @param pUbicacionInstancia Ubicacion de la instancia en el servidor remoto.
		 * @param pServerXml Objeto ServerXml.class el cual contiene la nueva configuracion de puertos.
		 * @return
		 */
		private boolean actualizaArchivoServerXmlRemoto(String pServidorUrl, String pUbicacionEstandardInstancia, String pNombreInstancia, ServerXml pServerXml){
			System.out.println(" Start consuming service: "+pServidorUrl+"api/instance/update/serverxml");
			
			Client cliente = ClientBuilder.newClient();
			WebTarget miTarget = cliente.target(pServidorUrl+"api/instance/update/serverxml");
			Invocation.Builder invocationBuilder = miTarget.request().header("instancePath", pUbicacionEstandardInstancia+"/"+pNombreInstancia); 
			Response respuesta = invocationBuilder.put(Entity.entity(pServerXml, MediaType.APPLICATION_JSON_TYPE));
			
			if( respuesta.getStatus() == 200){
				System.out.println("Server.xml file updated successfully.");
				// get response message
	           String responseMessageFromServer = respuesta.getStatusInfo().getReasonPhrase();
	           System.out.println("ResponseMessageFromServer: " + responseMessageFromServer);
	           return(true);
			}else{
				System.out.println("Error consuming the service, error code obtained <" + respuesta.getStatus() + "> with message:"+respuesta.readEntity(String.class));
				return(false);
			}

		}
		
		
		
		
		/**
		 * Makes a HTTP Get to the RestWS in order to obtain data of a remote Tomcat Instance.
		 * If the instance is not found null is returned.
		 * @param pServidor Usualmente http://xxx.xxx.xxx.xxx:xxxx/
		 * @param pUbicacionInstancia
		 * @return
		 */
		private InstanceDto obtenerInstanciaDto(String pServidor, String pUbicacionInstancia){
			InstanceDto objInstanciaDto = null; 
			System.out.println("Start consuming service: "+pServidor+"api/instance/synchronize");
			try {
				Client cliente = ClientBuilder.newClient();
				WebTarget miTarget = cliente.target(pServidor+"api/instance/synchronize");
				Invocation.Builder invocationBuilder = miTarget.request(MediaType.APPLICATION_JSON).header("instancePath", pUbicacionInstancia); 
				Response respuesta = invocationBuilder.get();
				
				if( respuesta.getStatus() == 200){
					objInstanciaDto = respuesta.readEntity(InstanceDto.class);
				}
			} catch (Exception e) {
				System.out.println("Error trying consuming the service: "+pServidor);
				System.out.println(e);
			}

			return objInstanciaDto;
		}	
}
