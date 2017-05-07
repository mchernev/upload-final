package edu.aubg.services;

import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.Semaphore;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import edu.aubg.client.MyClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * To test with curl:
 * 
 * <pre>
 * curl --header "Content-Type:application/octet-stream" --data-binary @test.txt http://localhost:9093/services/upload
 * </pre>
 */
@Path("/services")
public class UploadService {

	//private static final java.nio.file.Path inDir = Paths.get("C:\\Users\\Momchil\\src\\upload\\in");
	private static final java.nio.file.Path inDir = Paths.get(MyClient.saveDir);
	//private static final java.nio.file.Path tmpDir = Paths.get("C:\\Users\\Momchil\\src\\upload\\tmp");
	private static final java.nio.file.Path tmpDir = Paths.get(MyClient.tempDir);

	private static final Logger logger = LoggerFactory.getLogger(UploadService.class);

	//added semaphore to limit uploading to 4 threads
	private final Semaphore sem = new Semaphore(4, true);

	@POST
	@Path("upload")
	@Consumes(MediaType.APPLICATION_OCTET_STREAM)
	@Produces(MediaType.APPLICATION_JSON)
	public Response uploadFile(@Context HttpHeaders headers, InputStream in) {

		File tempFile = tmpDir.resolve(UUID.randomUUID().toString()).toFile();

		if (!sem.tryAcquire())//try to acquire semaphore
			return Response.status(Status.SERVICE_UNAVAILABLE).build();

		try {
			//original code not working on Windows due to bug in Java
			//the input stream is written into ByteArrayOutputStream, then it is written in tempFile
			ByteArrayOutputStream zz = new ByteArrayOutputStream();
			IOUtils.copy(in, zz);
			FileUtils.writeByteArrayToFile(tempFile, zz.toByteArray());

			UploadServiceResponse resp = new UploadServiceResponse();
			resp.setDocId(UUID.randomUUID().toString());
			resp.setFileSize(tempFile.length());

			//In order not to store all files in one directory
			//create a new directory with the name of the first two characters of the file name
			//if directory already was made, catch the thrown exception
			try{
			Files.createDirectory(inDir.resolve(resp.getDocId().substring(0, 2)));
			}
			catch (FileAlreadyExistsException e){
				//do nothing
			}
			//file moved to new directory
			//inDir.resolve(...).resolve(...) -> first resolve specifies the sub directory, second resolve specifies the name of the file
			Files.move(tempFile.toPath(), inDir.resolve(resp.getDocId().substring(0, 2)).resolve(resp.getDocId().substring(2)), StandardCopyOption.ATOMIC_MOVE);

			return Response.ok(resp).build();

		} catch (Exception ex) {

			logger.error("Upload failed", ex);

			return Response.status(Status.INTERNAL_SERVER_ERROR).build();

		} finally {

			FileUtils.deleteQuietly(tempFile);
			sem.release();//release semaphore
		}
	}
}