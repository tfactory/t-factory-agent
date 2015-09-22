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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import net.lingala.zip4j.core.ZipFile;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import cesarhernandezgt.dto.AgentDto;
import cesarhernandezgt.utils.PortUtils;

@Path("/agent")
public class Agente {

	/**
	 * Default method of the Rest WS, return info about the running agent
	 * This do not require security.
	 * @return json message of type ServerDto containing agente info.
	 */
	@GET()
	@Produces(MediaType.APPLICATION_JSON)
	public AgentDto hola(){
		
		String serverLocalName = "";
		AgentDto agentDto = new AgentDto();
		
		try {
			serverLocalName = InetAddress.getLocalHost().getHostName();
			Date timeStamp = new Date();
			System.out.println("\n"+timeStamp+"- Agent is starting to be called, server hosting the agent hostname: "+ serverLocalName);
		} catch (UnknownHostException e) {
			//hostname cannot be obtained
			System.out.println("Hostname could not be retrieved: "+e);
			agentDto.setStatus("error");
			return agentDto;
		}
		//Successfull call
		agentDto.setStatus("ok");
		agentDto.setHostname(serverLocalName);
		agentDto.setVersionAgent("0.0.1");
		return agentDto;
	}
	
	
	
	/**
	 * Provided 5 available ports on host server. This ports are used by the
	 * t-factory-server in order to create new tomcat instances with 
	 * valid port (http, jpa, shutdown, redirect and jxm). 
	 * 
	 * @param pInicialRange inclusive inital port range.
	 * @param pFinalRange inclusive final port range.
	 * @param pExcludes List of port to be excluded from the search.
	 * @return
	 */
	@GET()
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/obtain5AvailablePorts")
	public List<Integer> obtain5AvailablePorts(@QueryParam("initialRange") int pInicialRange, @QueryParam("finalRange") int pFinalRange, @QueryParam("excluded") List<Integer> pExcludes ){
		int contador = 0;
		List<Integer>  result  = new ArrayList<Integer>();
		Date timeStamp = new Date();
		System.out.println("\n"+timeStamp+"= Start obtain5AvailablePorts() for port range:"+pInicialRange+","+pFinalRange);
		System.out.println(" Excluded ports:"+pExcludes);
		for (int i = pInicialRange; i <= pFinalRange; i++) {
			//verify if pExcludes list is not empty and that the port found is not inside the excluded list.
			if(pExcludes!=null && !pExcludes.isEmpty()){
				if (pExcludes.contains(Integer.valueOf(i))){
					continue;
				}
			}
			
			if(PortUtils.availablePort(i)){
				contador ++;
				result.add(i);
				
				if (contador >= 5) {
					break;
				}
			}
		}
		
		System.out.println("5 ports were successfully found: "+result);
		return result;
	}
	
	
//   /** Method for exposing a simple REST Download service*/	
//	 @GET
//	    @Path("/download/zip")
//	    @Produces("application/zip")
//	    public Response downloadZippedFile() {
//	 
//	        // set file (and path) to be download
//	        File file = new File("/Users/cesarhgt/Instalados/tomcats/apache-tomcat-7.0.59/bin/archivo.zip");
//	 
//	        ResponseBuilder responseBuilder = Response.ok((Object) file);
//	        responseBuilder.header("Content-Disposition", "attachment; filename=\"archivo.zip\"");
//	        return responseBuilder.build();
//	    }
	

	//aqui me quede traduciendo, yalos paths estan cambiados, solo faltan parametros y traduccion normal
	
	/**
	 * This method receives a .zip file, name and path inside the remote 
	 * server in order to be copied.
	 * 
	 * @param fileInputStream
	 *            File .zip. (i.e. apache-tomcat-8.0.23.zip) with extension.
	 * @param fileFormDataContentDisposition
	 *            File Metadata.
	 * @param locationPath
	 *            Path in witch the zip file will be copied in this remote
	 *            server i.e.: (/srv/tomcat8/).
	 * @return
	 */
	@POST
	@Path("/upload/instance")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	public Response uploadZippedFile(
			@FormDataParam("uploadFile") InputStream fileInputStream,
			@FormDataParam("uploadFile") FormDataContentDisposition fileFormDataContentDisposition,
			@FormDataParam("locationPath") String locationPath) {
		Date timeStamp = new Date();
		System.out.println("\n"+timeStamp+"= Start of  uploadZippedFile()");
		// local variable
		String zipFileName = null;

		try {
			zipFileName = fileFormDataContentDisposition.getFileName();
			System.out.println("<" + zipFileName + ">,<"
					+ locationPath + ">");

			if (escribirArchivoEnServidor(fileInputStream, zipFileName,locationPath)) {
				System.out.println("success.");
				return Response.ok(
						"File was uploaded successfully to remote server with path: "
								+ locationPath +"/"+zipFileName).build();
			} else {
				return Response
						.status(403)
						.entity("Error trying to write the file on remote server ERROR 1.")
						.build();
			}
		} catch (Exception ioe) {
			ioe.printStackTrace();
			return Response
					.status(403)
					.entity("Error trying to write the file on remote server ERROR 2.")
					.build();
		}

	}

	/**
	 * Create a new file in the file system of remote server.
	 * 
	 * @param pInputStream
	 *            zip File.
	 * @param pZipFileName
	 *            Name of the file to be created.
	 * @param pLocationPath
	 *            Path en whitch the new file will be createde.
	 * @return
	 * @throws IOException
	 */
	private boolean escribirArchivoEnServidor(InputStream pInputStream,
			String pZipFileName, String pLocationPath)
			throws Exception {
		OutputStream outputStream = null;
		String pathCompletoDelZip = pLocationPath+"/"+ pZipFileName;

		try {
			outputStream = new FileOutputStream(new File(pathCompletoDelZip));
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = pInputStream.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.flush();
		} finally {
			outputStream.close();
		}

		return true;
	}	    
	
	
	
	/**
	 * Unzip and rename the instance folder.
	 * @param pUbicacionInstancia Ubicacion en el servidor host del agente ejm: (/srv/Tomcat8/).
	 * @param pNombrePlantilla Nombre plantilla original ejm: (apache-tomcat-8.0.23.zip) con extension.
	 * @param pNuevoNombre Nombre de la nueva instancia ejm: (apache-tomcat-arquitectura).
	 * @return
	 */
	@GET
    @Path("/unzip/instance")
    @Produces(MediaType.APPLICATION_JSON)
    public Response unzipInstance(@QueryParam("pathLocation") String pUbicacionInstancia, @QueryParam("templateName") String pNombrePlantilla, @QueryParam("newName") String pNuevoNombre) {
			Date timeStamp = new Date();	
			System.out.println("\n"+timeStamp+"= Start  unzipInstance()");
			System.out.println(pUbicacionInstancia);
			System.out.println(pNombrePlantilla);
			System.out.println(pNuevoNombre);
			
			
			
		try {
			//Unziping file
			System.out.println("Unziping..."+pUbicacionInstancia+"/"+pNombrePlantilla);
			ZipFile zip = new ZipFile(pUbicacionInstancia+"/"+pNombrePlantilla);
			zip.extractAll(pUbicacionInstancia);
			
			//Rename file
			File antiguoNombre = new File (pUbicacionInstancia+"/"+ pNombrePlantilla.replaceFirst(".zip", ""));//we eliminate the .zip extension
			File nuevoNombre = new File (pUbicacionInstancia+"/"+pNuevoNombre);
			
			System.out.println("OldName:"+antiguoNombre.getAbsolutePath());
			System.out.println("NewName:"+nuevoNombre.getAbsolutePath());
			
			if (antiguoNombre.renameTo(nuevoNombre)) {
				
				String osType =System.getProperty("os.name");
			       
				//If the server is a Linux, Mac or Unix, We assign to the instance execution permission by the user executing the agent
		        if (!osType.toLowerCase().contains("windows")) {
		        	if (nuevoNombre.setExecutable(true)) {
		        		//chmod 766 -R 
		        		Runtime rt = Runtime.getRuntime();
		        		Process proc;
		        		int exitVal = -1;
		        		try {
		        			proc =  rt.exec("chmod -R 744 "+pUbicacionInstancia+"/"+pNuevoNombre);
		        			exitVal = proc.waitFor();
		        			
		        			if (exitVal != -1) {
		        				
		        				//Success!!!
		        				System.out.println("Unziped and renamed successfully:"+nuevoNombre.getAbsolutePath());
		        				return Response.ok("Unziped and renamed successfully").build();
		        				
		        			} else {
		        				System.out.println("Error trying to grant execution permissions to the instance. <5>");
		        				return Response.status(403).entity("Error trying unzip the file <5>").build();
		        			}
		        		} catch (Exception e) {
		        			System.out.println("Error trying to grant execution permissions to the instance. <4>");
		        			return Response.status(403).entity("Error trying unzip the file <4>").build();
		        		}
		        	} else {
		        		System.out.println("Error trying to grant execution permissions to the instance. <3>");
		        		return Response.status(403).entity("Error trying unzip the file <3>").build();
		        	}
				}else{
					//Success!!!
    				System.out.println("Unziped and renamed successfully:"+nuevoNombre.getAbsolutePath());
    				return Response.ok("Unziped and renamed successfully").build();
				}
				
			} else {
				System.out.println("Error trying to rename the instance folder.");
				return Response.status(403).entity("Error trying unzip the file <2>").build();
			}
			
		} catch (Exception e) {
			System.out.println("Error:"+e);
			return Response.status(403).entity("Error trying unzip the file <1>").build();
		}
    }

	 
	 
}
