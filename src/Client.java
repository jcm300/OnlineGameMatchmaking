import java.io.*;
import java.net.*;

//Class that each gamer will execute
public class Client {
    private String username; //username of user authenticated
    private Socket socket; //socket connected to the server
    private boolean isAuth; //the user is authenticated or not

    //attempt to authenticate with a username and a password
    private void authAttempt(String username, String password) throws IOException{
        
        //open input and output
        PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        
        String current;

        //send username and password to the server encapsulated on server-client sintax
        out.println("$;" + username + ";" + password + ";$");
        
        //wait for server response
        while((current = in.readLine()) != null){
            //safely close IO streams
            this.socket.shutdownInput();
            this.socket.shutdownOutput();
            
            if(current.equals("Authenticated")){
                this.isAuth = true;
                this.username = username;
            }
        }
    }

    private void authenticate (){
        
        String username = null, password = null;
        
        try{
            this.socket = new Socket("127.0.0.1",9999);

            while(!this.isAuth /*or cancel*/){
                //TODO: read username and password
                authAttempt(username,password);
            }
        }catch(Exception e){
            e.printStackTrace();
        }   
    }
}
