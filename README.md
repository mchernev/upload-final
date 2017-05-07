# Instructions to use:

1. clone repository  
2. run _mvn package appassembler:assemble_ in root directory  
3. _cd target/appassembler/bin/_  
4. _./my-server_  
5. In another terminal go to the same place: _cd target/appassembler/bin/_  
6. run _./my-client **uploadPath** **inPath** **tmpPath**_  
where uploadPath, inPath, tmpPath, are the desired paths  
For example: **_./my-client C:\\\Users\\\admin\\\src\\\upload C:\\\Users\\\admin\\\src\\\store C:\\\Users\\\admin\\\src\\\temp_**  
Note: For Windows paths, a double slash should be used in order to escape it.

Files should now disappear from the upload path and appear in the saved path

As of this moment there is a problem with the second and third path. inDir and tmpDir should be hardcoded in UploadService.  
It is possible this problem only exists in Windows.
The master branch is left with the inDir and tempDir hardcoded. (Requires just one input -> the path of the directory to be uploaded)
The develop branch is left using all three inputs. (Requires all 3 input). Hopefully it works on Linux. :)

## Server Side:

The server side uses a semaphore to limit the number of concurrent uploads. I give the semaphore 4 permits.


The server side gives the files randon names and stores them in the directory specified by _inDir_  
Since the files have random names, I have decide to use the first 2 charachters of their name to create a sub-directory in inDir.  
For example if the name of the file is _0d50a524-6904-4b5e-8b05-cbff5c6516c3_, instead of storing it directly in _inDir_, I create a sub-directory in _inDir_ called _0d_ (the first 2 characters of the file name).  
Because the names are random, each two character combination is equaly likely to occur, so all sub-directories should have a simmilar number files in them.

There should not be a concurrency issue using this method.

## Client Side:

The client side goes to the directory intended for upload and gets the paths of all the files in that directory and its sub-directories.  
It uses recursion to enter the sub-directories.  
The paths are stored in a stack.  
To upload file concurrently I use an executor with a fixed ThreadPool of 4.  
While the stack is not empty, file paths are given to the threds.  
Each thread uploads one file path at a time.

To upload the files, the client side uses httpClient to issue a POST request to the server side.

