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

    public Worker(Socket skt){
        this.skt = skt;
    }

    public void run(){
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(this.skt.getInputStream()));
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(this.skt.getOutputStream()));
            String inS;

            while(!this.skt.isClosed()){
                inS =in.readLine();
                if(/*user verified*/) out.println("Authenticated");
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

                Thread wrk = new Thread(new Worker(skt));
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

}
