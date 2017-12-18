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

    public Worker(Socket skt, GameData gdt){
        this.skt = skt;
        this.gdt = gdt;
    }

    private boolean verifyAuthAttempt(String s){
        boolean ret = false;
        if(s.charAt(1)=='$' && s.charAt(2)=='|' && s.charAt(s.length()-1)=='$' && s.charAt(s.length()-2)=='|'){
            String[] aux = s.split(";");
                if(aux.length==2)
                    if(gdt.passwordMatch(aux[1],aux[2])) ret = true;
        }
        return ret;
    }

    private boolean verifyCreateUserAttempt(String s){
        boolean ret = false;
        if(s.charAt(1)=='$' && s.charAt(2)=='c' && s.charAt(s.length()-1)=='$' && s.charAt(s.length()-2)=='c'){
            String[] aux = s.split(";");
            if(aux.length==3)
                if(gdt.addUser(aux[1],aux[2],aux[3])) ret = true;
        }
        return ret;
    }

    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(this.skt.getInputStream()));
            PrintWriter out = new PrintWriter(this.skt.getOutputStream(), true);
            String inS;

            while(!this.skt.isClosed()){
                inS =in.readLine();
                if(verifyAuthAttempt(inS)){    
                    out.println("Authenticated"); 
                    System.out.println("Authenticated");
                }else{
                    if(verifyCreateUserAttempt(inS)){
                        out.println("UCreated");
                        System.out.println("UCreated");
                    }
                }
            }       
            
            this.skt.shutdownInput();
            this.skt.shutdownOutput();
            this.skt.close();
        }catch(Exception e){
            e.printStackTrace();
        } 
    }
}

class Server{
    private int prt;
    private GameData gdt;

    public static void main(String args[]){


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
        if(!this.userExists(uName)) return false;
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

    public boolean userExists(String username){
        return this.users.containsKey(username);
    }

    public List<String> getHeros(){}
}
