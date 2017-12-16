/*
*/

import java.net.ServerSocket;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.Map.HashMap;

class Worker implements Runnable{
    private Socket skt;
    private GameData gdt;

    public Worker(Socket skt, GameData gdt){
        this.skt = skt;
        this.gdt = gdt;
    }

    private boolean verifyAuthAttempt(String s){
        boolean ret = false;
        if(inS.charAt(1)=='$' && inS.charAt(2)=='|' && inS.charAt(inS.length()-1)=='$' && inS.charAt(inS.length()-2)=='|'){
            ArrayList<String> aux = inS.split(';');
                if(aux.size()==2)
                    if(gdt.passwordMatch(aux.get(1),aux.get(2))) ret = true;
        }
        return ret;
    }

    private boolean verifyCreateUserAttempt(String s, ArrayList<String> l){
        boolean ret = false;
        if(inS.charAt(1)=='$' && inS.charAt(2)=='c' && inS.charAt(inS.length()-1)=='$' && inS.charAt(inS.length()-2)=='c'){
            l = inS.split(';');
            if(l.size()==3)
                if(addUser(aux.get(1),aux.get(2),aux.get(3))) ret = true;
        }
        return ret;
    }

    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(this.skt.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.skt.getOutputStream()));
            String inS;

            while(!this.skt.isClosed()){
                inS =in.readLine();
                if(verifyAuthAttempt(inS)){    
                    out.println("Authenticated"); 
                }else{
                    ArrayList<String> aux;
                    if(verifyCreateUserAttempt(inS,aux)){
                        out.println("UCreated");
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

    public User getUser(){}

    public boolean addUser(String uName, String pass, String mail){}

    public boolean passwordMatch(String uName,String pass){}

    public int getRank(String username){}

    public boolean userExists(String username){}

    public List<String> getHeros(){}
}
