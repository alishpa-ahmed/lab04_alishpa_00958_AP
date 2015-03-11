

import java.util.*;
import java.io.*;
import java.net.*;

public class webserver {
  private static ServerSocket serverSocket;

  public static void main(String[] args) throws IOException {
    serverSocket=new ServerSocket(8080);
    System.out.println("Webserver started");
    while (true) {
      try {
        System.out.println("Waiting for client to connect ");// Wait for a client to connect
        Socket s=serverSocket.accept(); // Wait for a client to connect
        new ClientHandler(s);  // Handle the client in a separate thread
      }
      catch (Exception x) {
        System.out.println(x);
      }
    }
  }
}

// A ClientHandler reads an HTTP request and responds
class ClientHandler extends Thread {
  private Socket socket; 
  // Start the thread in the constructor
  public ClientHandler(Socket s) {
    socket=s;
    start();
  }

  // Read the HTTP request, respond, and close the connection
  public void run() {
    try {
    	System.out.println("Making connection ");
      // Open connections to the socket
      BufferedReader in=new BufferedReader(new InputStreamReader(
        socket.getInputStream()));
      PrintStream out=new PrintStream(new BufferedOutputStream(
        socket.getOutputStream()));
      System.out.println("reading file name ");

      String s=in.readLine();
      System.out.println(s);  
      
      String filename="";
      StringTokenizer st=new StringTokenizer(s);
      try {
        if (st.hasMoreElements() && st.nextToken().equalsIgnoreCase("GET")
            && st.hasMoreElements())
          filename=st.nextToken();
        else
          throw new FileNotFoundException();  // file not found


        if (filename.endsWith("/")) //if no file requested diplay basic page
          filename+="basic.html";
        
        while (filename.indexOf("/")==0)
          filename=filename.substring(1);

        filename=filename.replace('/', File.separator.charAt(0)); //handling file seperators

        if (filename.indexOf("..")>=0 || filename.indexOf(':')>=0|| filename.indexOf('|')>=0){
        	out.println("HTTP/1.1 400 Bad Request\r\n"+ "Content-type: text/html\r\n\r\n"+
                  "<html><head></head><body>"+filename+" Bad Request</body></html>\n");
                out.close();
          }  // Bad request

        if (new File(filename).isDirectory()) { //if a directory is requested with missing trail /
          filename=filename.replace('\\', '/');
          out.print("HTTP/1.1 301 Moved Permanently\r\n"+
            "Location: /"+filename+"/\r\n\r\n");
          out.close();
          return;
        }

        InputStream f=new FileInputStream(filename);
        String mimeType="text/plain";
        if (filename.endsWith(".html") || filename.endsWith(".htm"))
          mimeType="text/html";
        else if (filename.endsWith(".jpg") || filename.endsWith(".jpeg"))
          mimeType="image/jpeg";
        else if (filename.endsWith(".gif"))
          mimeType="image/gif";
        else if (filename.endsWith(".pdf"))
            mimeType="text/pdf";      
        else if (filename.endsWith(".class"))
          mimeType="application/octet-stream";
        out.print("HTTP/1.1 200 OK\r\n"+
          "Content-type: "+mimeType+"\r\n\r\n");
        
        byte[] a=new byte[4096];
        int n;
        while ((n=f.read(a))>0)
          out.write(a, 0, n);
        out.close();
      }
      
      catch (FileNotFoundException x) {
        out.println("HTTP/1.1 404 Not Found\r\n"+
          "Content-type: text/html\r\n\r\n"+
          "<html><head></head><body> File not found !   "+filename+"</body></html>\n");
        out.close();
      }
    }
    catch (IOException x) {
      System.out.println(x);
    }
  }
}
