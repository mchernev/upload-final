package edu.aubg.client;


import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;

import java.io.File;
import java.nio.file.Paths;
import java.util.Stack;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class MyClient {

    //holds the paths of the files to be uploaded
    private static Stack<String> filePaths = new Stack<>();

    //holds the directory that keeps the saved files
    public static String saveDir;

    //holds the directory that keeps the temp files
    public static String tempDir;

    //check if string is valid path
    private static boolean isPath(String path){
        File file = new File(path);
        return file.isDirectory();
    }

    //returns # of directories from root
    //used as precaution, not delete something very serious
    private static int dirsFromRoot(String path){
        File file = new File(path);
        int count = 0;
        while(file.getParent()!=null){
            ++count;
            file = new File(file.getParent());
        }
        return count;
    }

    //adds the file paths to filePaths
    //uses recursion to enter sub-directories and get files
    private static void getFilePaths(String path){
        File file = new File(path);
        File[] f = file.listFiles();
        for(int i = 0; i<f.length; ++i){
            if(f[i].isFile()){
                filePaths.push(f[i].getPath());
            }
            else    if(f[i].isDirectory())
                        getFilePaths(f[i].getPath());
        }
    }


    private static HttpClient httpClient;

    //tries to send the file paths to UploadService.java
    public static boolean tryUpload(String path) {
        //creates post request
        try {
            ContentResponse response = httpClient.newRequest("http://localhost:9093/services/upload")
                    .method(HttpMethod.POST)
                    .file(Paths.get(path))
                    .send();

            //handles server overload
            if (response.getStatus() == 503) {
                //Sleep...
            }

            //prints the server's response
            System.out.println("got"
                    //+ " " + response.getMediaType()
                    //+ " " + response.getEncoding()
                    + " " + response.getContentAsString());
            return true;
        } catch (Exception e) {
            System.err.println("Failed to upload " + path);
            e.printStackTrace(System.err);
            return false;
        }
    }

    public static void main(String [] args) {

        //holds the path given by the user
        String path;

        //checks if the three arguments are valid paths, if not valid -> exits program and gives message
        if(args.length == 3) {
            if (isPath(args[0]) && isPath(args[1]) && isPath(args[2]) && dirsFromRoot(args[0]) > 3) {
                path = args[0];
                saveDir = args[1];
                tempDir = args[2];

                //creates executor with 4 threads
                ExecutorService executor = Executors.newFixedThreadPool(4);

                //fills the filePaths stack with the paths of the files for upload
                getFilePaths(path);

                //creates http client
                httpClient = new HttpClient();
                try {
                    httpClient.start();
                } catch (Exception e) {
                    throw new RuntimeException("issue during httpClient.start()", e);
                }

                //executes threads until the stack is not empty
                while (!filePaths.isEmpty()) {
                    executor.execute(new UploadFiles(filePaths.pop()));
                }
                //shut down executer and waits for termination
                executor.shutdown();
                while (!executor.isTerminated()) {
                }
                //stops the httpclient
                try {
                    httpClient.stop();
                } catch (Exception e) {
                    throw new RuntimeException("issue during httpClient.stop()", e);
                }
            }
            //goes here if something's wrong with user entered path
            else{
                System.out.println("Invalid Paths");
            }
        }
        else{
                System.out.println("You need to specify three paths: The path to be uploaded; The path to be uploaded to; The temporary path");
        }
    }
}
