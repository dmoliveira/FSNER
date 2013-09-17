package lbd.Utils.Connection;

/* 
* This example is from javareference.com 
* for more information visit, 
* http://www.javareference.com 
*/ 

//package

//import statements
import java.io.*;
import java.net.*;

/**
 * This class gets the requested web page
 * and saves it on the local machine
 * 
 * @author Rahul Sapkal(rahul@javareference.com)
 */
public class HTMLPageGrabber
{
    /**
     * The main method. 
     * This method expects the url of the page
     * to be grabbed
     *  
     * @param args
     * @throws Exception
     */
    public static void main (String[] args) throws Exception
    {
        try
        {
            // Page Address user want to grab
            String pageAddr;        
            // WebSite Address - host
            String websiteAddress;
            //html file
            String file;
            //local file
            String localFile;
    
            //check if the page address is passed as an
            //argument
            if(args.length == 1)
              {
                pageAddr = args[0];
            }
            else
            {
                //if no page is specifed set default page to get
               //pageAddr = "http://www.javareference.com/index.jsp";
            	//pageAddr = "http://homepages.dcc.ufmg.br/~dmoliveira/index.html";
            	pageAddr = "http://www.thesaurus.com/browse/bike";
            }
    
            //get the website address (i.e.host address) from the page address
            //using the URL object
            URL url = new URL(pageAddr);
            websiteAddress = url.getHost();
            
            //get the file
            //ex. if the url is http://www.javarefernce.com/index.jsp,
            //then /index.jsp is the file
            file = url.getFile();
            
            //set the local file name as the requested file
            localFile = file;
            
            //if file does not exist, like if the url is just
            //http://www.javareference.com then the file returned is empty
            //in this case set the url as file and local file name to index.html
            if(file.length() == 0)
            {
                file = pageAddr;
                localFile = "index.html";
            }                            
            
            //creating a socket to the website using the website address
            //and port 80
            Socket clientSocket = new Socket(websiteAddress, 80);
            System.out.println("Socket opened to " + websiteAddress + "\n");
            
            //creating a BufferReader object using the input stream reader
            //this will read the content send by the webserver
            BufferedReader inFromServer = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
            
            //Need to create a output stream writer
            //that will talk to the webserver of the website
            OutputStreamWriter outWriter = new OutputStreamWriter(clientSocket.getOutputStream());
            
            //make the GET call to the webserver with the desired url or the file name
            //which you intent to get, also mention the protocol type, which is HTTP/1.0
            //This call will trigger the webserver to throw this page, which will be read
            //by the input stream
            
            //making a get call to the file
            outWriter.write("GET " + file + " HTTP/1.0\r\n\n");
            outWriter.flush();            
            
            localFile = localFile.substring(localFile.lastIndexOf('/') + 1);

            //creating a BufferWriter to create and write into the file locally
            BufferedWriter out = new BufferedWriter(new FileWriter(localFile));

            //This loop reads the file
            boolean more = true;
            String input;
            
            while (more)
            {
                //read one line at a time
                input = inFromServer.readLine();
            
                //print the line if any
                if (input == null) 
                    more = false;
                else
                {
                    System.out.print("*");
                    out.write(input);
                }                    
            }
            
            System.out.println("\nPage received successfully..." + "\n");
            
            out.close();
            
            //close the client socket
            clientSocket.close();
        }
        catch (IOException e)
        {
            System.out.println ("Error getting page " + e);
        }
    }
} 
