import java.io.*;
import java.net.*;

//Class that each gamer will execute
public class Client{
    private String username; //username of user authenticated
    private Socket socket; //socket connected to the server
    private boolean isAuth; //the user is authenticated or not
    private BufferedReader in; //input from the server 
    private PrintWriter out; // output to the server

    public static void main(String args[]){
        Client cli = new Client();
        cli.testCli();
        cli.closeClient();
    }
    
    //instance Client creation method
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
    
    //test method
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
        if((current = this.in.readLine()) != null){
            if(current.equals("Authenticated")){
                this.isAuth = true;
                this.username = uName;
                System.out.println("Authentication successful");
            }else if(current.equals("NotAuth")){
                //TODO
                this.isAuth=false;
                System.out.println("Wrong credentials");
            }
        }   
    }

    private void authenticate (){
        String username = null, password = null;
        
        try{
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
            //create message to send with username password and email
            sb.append("$c").append(uName).append(";").append(password).append(";").append(email).append("c$");

            //send data for user creation separated by semi-colon
            this.out.println(sb.toString());
           
            //wait for server response
            if((current = this.in.readLine()) != null){
                if(current.equals("UCreated")){
                    // TODO
                    System.out.println("User Created");
                }else if(current.equals("UExists")){
                    System.out.println("Username already exists"); //debug
                    //TODO
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }    

    public void joinGame(){
        if(this.isAuth){
            String current;
            StringBuilder sb = new StringBuilder();
            //create menssage with username
            sb.append("$j").append(this.username).append("j$");
            try{
                //send data to server
                this.out.println(sb.toString());
                //wait for server response
                if((current = this.in.readLine()) != null){
                    if(current.equals("UQJoin")){
                        //TODO
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            } 
        }
    }

    public void closeClient(){
        this.isAuth = false;
        try{
            //safely close IO streams
            this.socket.shutdownInput();
            this.socket.shutdownOutput();
            this.socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
