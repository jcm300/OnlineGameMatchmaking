/*
*/
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

class Worker implements Runnable{
    private Socket skt;
    private GameData gdt;
    private BufferedReader in;
    private PrintWriter out;

    public Worker(Socket skt, GameData gdt){
        this.skt = skt;
        this.gdt = gdt;
        try{
            this.in = new BufferedReader(new InputStreamReader(this.skt.getInputStream()));
            this.out = new PrintWriter(this.skt.getOutputStream(), true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    private int MessageType(String s){
        int ret = 0;
        if(s!=null){
            if(s.charAt(0)=='$' && s.charAt(s.length()-1)=='$'){
                if(s.charAt(1)=='|' && s.charAt(s.length()-2)=='|') ret=1;
                else if(s.charAt(1)=='c' && s.charAt(s.length()-2)=='c') ret=2;
            }
        }
        return ret;
    }
    
    private void parseLine(String s){
        int type = MessageType(s);
        if(type!=0){
            s = s.substring(2, s.length()-2);
            String[] aux = s.split(";");
            if(type==1 && aux.length==2){
                if(gdt.passwordMatch(aux[0],aux[1])){
                    this.out.println("Authenticated"); 
                    System.out.println("Authenticated");
                }else{
                    this.out.println("NotAuth");
                    System.out.println("NotAuth");
                }
            }else if(type==2 && aux.length==3){
                if(gdt.addUser(aux[0],aux[1],aux[2])){
                    this.out.println("UCreated");
                    System.out.println("UCreated");
                }else{
                    this.out.println("UExists");
                    System.out.println("UExists");
                }
            }
        }   
    }

    public void run(){
        String inS;
            
        System.out.println("Connection Received");
        try{    
            while((inS = this.in.readLine()) != null){
                parseLine(inS);
            }       
        
            this.skt.shutdownInput();
            this.skt.shutdownOutput();
            this.skt.close();
            System.out.println("Connection Closed");
        }catch(Exception e){
            e.printStackTrace();
        } 
    }
}

class Server{
    private int prt;
    private GameData gdt;

    public static void main(String args[]){
        Server mSrv = new Server(9999);
        mSrv.run();
    }

    public Server(int port){
        this.prt = port;
        this.gdt = new GameData();
    }

    public void run(){
        try{
            ServerSocket sSkt = new ServerSocket(this.prt);
            while(true){
                Socket skt = sSkt.accept();

                Thread wrk = new Thread(new Worker(skt,gdt));
                wrk.run();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}


class GameData{
    private Map<String,User> users;

    public GameData(){
        this.users = new HashMap<>();
    }

    public User getUser(String uName){
        return this.users.get(uName).clone();
    }

    public synchronized boolean addUser(String uName, String pass, String mail){
        if(this.userExists(uName)) return false;
        this.users.put(uName,new User(uName,pass,mail));
        return true; 
    }

    public boolean passwordMatch(String uName,String pass){
        User aux = this.users.get(uName);

        if(aux != null) return aux.getPassword().equals(pass);
        else return false;
    }

    public int getRank(String username){
        User aux = this.users.get(username);

        if(aux == null) return -1;
        else return aux.getRank();
    }

    public boolean userExists(String uName){
        return this.users.containsKey(uName);
    }

    //public List<String> getHeros(){}
}
