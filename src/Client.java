import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;


//Class that each gamer will execute
public class Client{
    private String username; //username of user authenticated
    private Socket socket; //socket connected to the server
    private boolean isAuth; //the user is authenticated or not
    private BufferedReader in; //input from the server 
    private PrintWriter out; // output to the server

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
                this.isAuth=false;
                System.out.println("Wrong credentials");
            }else if(current.equals("AlreadyAuthenticated")){
                this.isAuth=false;
                System.out.println("Already Authenticated");
            }
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
                    System.out.println("User Created");
                }else if(current.equals("UExists")){
                    System.out.println("Username already exists");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void deauthenticate(){
        this.out.println("$d" + this.username + "d$");
    }

    public void joinGame(){
        if(this.isAuth){
            String current;
            StringBuilder sb = new StringBuilder();
            //create menssage with username
            sb.append("$j").append(this.username).append("j$");
            try{
                System.out.println("Wait... searching for players...");
                //send data to server
                this.out.println(sb.toString());
                //wait for server response
                if((current = this.in.readLine()) != null){
                    if(current.equals("UQJoin")){
                        boolean stop = false;
                        AtomicBoolean stopThread = new AtomicBoolean(false);
                        Thread rC = new Thread(new ResponseClient(this.out, stopThread));
                        rC.start();
                        while(!stop && (current=this.in.readLine())!=null){
                            if(current.equals("Gstart")) stop = true;
                            else System.out.println(current);
                        }
                        stopThread.set(true);
                    }else if(current.equals("UQNotJoin")){
                        System.out.println("Error fetching joining queue, please try again");
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

    public int mainMenu(Scanner s){
        System.out.println("Menu:");
        System.out.println("1: Registration");
        System.out.println("2: Authentication");
        System.out.println("0: Close");
        int ret;
        try{ 
            ret = s.nextInt();
            s.nextLine();
            if(ret>2) ret = -1;
        }catch(Exception e){
            ret = -1;
        }
        return ret;
    }

    public void registrationMenu(Scanner s){
        String username = "",email = "", password = "";

        System.out.println("Registration Menu");
        System.out.print("Username: ");
        username = s.next();
        System.out.print("Email: ");
        email = s.next();
        System.out.print("Password: ");
        password = s.next(); 
        System.out.print("Write 'confirm' to confirm data: ");
        if(s.next().equals("confirm")) this.createUser(username,email,password);       
    }

    public void authenticationMenu(Scanner s){
        String username = "", password = "";
        boolean flag = false;

        System.out.println("Authentication Menu");
        try{
            while(!this.isAuth && !flag){
                System.out.print("Username: ");
                username = s.next();
                if(username.equals("exit")) flag = true;
                else{
                    System.out.print("Password: ");
                    password = s.next(); 
                    this.authAttempt(username,password);
                    if(!this.isAuth) System.out.println("Write 'exit' to cancel");
                }       
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void playMenu(Scanner s){
        int c=-1;
        while(c==-1){
            while(c!=0 && c!=1){
                System.out.println("Play Menu:");
                System.out.println("1: Join to a room");
                System.out.println("0: Go back");
                try{
                    c = s.nextInt();
                    s.nextLine();
                }catch(Exception e){
                    c = -1;
                }
            }
            if(c==1){
                this.joinGame();
                c=-1;
            }
        }   
    }

    public void interfaceCli(){
        Scanner s = new Scanner(System.in);
        int choice = -1;
        
        while(choice==-1){
            choice = this.mainMenu(s);
        
            switch(choice){
                case 0:
                    break;
                case 1:
                    this.registrationMenu(s);
                    choice = -1;
                    break;
                case 2:
                    this.authenticationMenu(s);
                    if(this.isAuth){
                        this.playMenu(s);
                        this.isAuth = false;
                        this.deauthenticate();
                    }
                    choice = -1;
                    break;
            }
        }
        s.close();
    }

    public static void main(String args[]){
        Client cli = new Client();
        cli.interfaceCli();
        //cli.closeClient();
    }
}

class ResponseClient implements Runnable{
    private PrintWriter out;
    private AtomicBoolean stop;

    public ResponseClient(PrintWriter out, AtomicBoolean stopP){
        this.out = out;
        this.stop = stopP;
    }

    public void run(){
        String current;
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        try{
            while(!this.stop.get() && (current = in.readLine())!=null){
                this.out.println(current);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
