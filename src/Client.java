import java.io.*;
import java.net.*;

//Class that each gamer will execute
public class Client{
    private String username; //username of user authenticated
    private Socket socket; //socket connected to the server
    private boolean isAuth; //the user is authenticated or not
    private BufferedReader in;
    private PrintWriter out;

    public static void main(String args[]){
        Client cli = new Client();
        cli.testCli();
        
        cli.isAuth = false;
        try{
            //safely close IO streams
            cli.socket.shutdownInput();
            cli.socket.shutdownOutput();
            cli.socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public Client(){
        try{
            this.socket=new Socket("127.0.0.1",9999);
            //open input and output
            this.out = new PrintWriter(this.socket.getOutputStream(), true);
            this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
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
    private void authAttempt(String uName, String password) throws IOException{
        
        String current;
        
        //send username and password to the server encapsulated on server-client sintax
        this.out.println("$|" + uName + ";" + password + "|$");
        
        //wait for server response
        while((current = this.in.readLine()) != null && !this.isAuth){
            if(current.equals("Authenticated")){
                this.isAuth = true;
                this.username = uName;
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
            String current;
            StringBuilder sb = new StringBuilder();
            sb.append("$c").append(uName).append(";").append(password).append(";").append(email).append("c$");

            //send data for user creation separated by semi-colon
            this.out.println(sb.toString());
            
            boolean cU = false;
            while((current = this.in.readLine()) != null && cU){
                if(current.equals("UCreated")){
                    //TODO
                    cU = true;
                }else if(current.equals("UExists")){
                    //TODO
                    cU = true;
                }
            }
        }catch(Exception e){

        }
    }    
}
