package edu.aubg.client;

import java.io.File;

public class UploadFiles implements Runnable {

    //holds the path of the file to be uploaded
    private String temp;

    public UploadFiles(String s){
        this.temp = s;
    }

    public void run(){

            //prints the path and the status of the upload
            System.out.print(temp + " ");
            System.out.println(MyClient.tryUpload(temp));

            //when the file is uploaded, it gets deleted
            new File(temp).delete();
    }
}
