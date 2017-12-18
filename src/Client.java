import java.io.*;
import java.net.*;

//Class that each gamer will execute
public class Client{
    private String username; //username of user authenticated
    private Socket socket; //socket connected to the server
    private boolean isAuth; //the user is authenticated or not

    public static void main(String args[]){
        Client cli = new Client();
        cli.testCli();
    }

    public Client(){
        try{
            this.socket=new Socket("127.0.0.1",9999);
            this.isAuth=false;
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void testCli(){
        this.createUser("user1","weakpassword","user1@emailDomain.com");
        try{
            this.authAttempt("user1","weakpassword");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //attempt to authenticate with a username and a password
    private void authAttempt(String username, String password) throws IOException{
        
        //open input and output
        PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
        
        String current;

        //send username and password to the server encapsulated on server-client sintax
        out.println("$l" + username + ";" + password + "l$");
        
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

    private void createUser(String uName, String email, String password){
        try{
            PrintWriter out = new PrintWriter(this.socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

            String current;
            StringBuilder sb = new StringBuilder();
            sb.append("$c").append(username).append(";").append(password).append(";").append(email).append("c$");

            //send data for user creation separated by semi-colon
            out.println(sb.toString());
            while((current = in.readLine()) != null){
                //safely close IO streams
                this.socket.shutdownInput();
                this.socket.shutdownOutput();
                
                if(current.equals("UCreated")){
                    this.isAuth = true;
                    this.username = username;
                }
            }
        }catch(Exception e){

        }
    }    
}
