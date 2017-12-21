/*
*/
import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

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
            if(s.charAt(0)=='$' && s.charAt(s.length()-1)=='$' && s.charAt(1)==s.charAt(s.length()-2)){
                switch(s.charAt(1)){
                    case '|':  //login
                            ret=1;
                            break;
                    case 'c': //create
                            ret=2;
                            break;
                    case 'j': //join
                            ret=3;
                            break;
                    default:
                            break;
                }
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
            }else if(type==3 && aux.length==1){
                this.out.println("UQJoin");
                System.out.println("UQJoin");
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

class waitQueue{
    private int[] rankQueue; //array with ids of threads of users based on rank
    private ReentrantLock rlock;
    private Condition enoughPlayers;

    public waitQueue(){
        this.rankQueue = new int[10];
        this.rlock =new ReentrantLock();
        this.enoughPlayers = this.rlock.newCondition();
    }
   
    public void joinQueue(int rank){
        this.rlock.lock();

        try{
            this.rankQueue[rank] ++;

            if(rank == 0)
                while(this.rankQueue[rank]+this.rankQueue[rank+1]<10)
                    this.enoughPlayers.await();
            else if(rank == 9)
                while(this.rankQueue[rank-1]+this.rankQueue[rank]<10)
                    this.enoughPlayers.await();
            else
                while(this.rankQueue[rank-1] + this.rankQueue[rank] < 10 && this.rankQueue[rank]+this.rankQueue[rank+1]<10)
                    this.enoughPlayers.await();

            this.rankQueue[rank] --;
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.rlock.unlock();
        }
    }
}

class Game{
    private ArrayList<User> team1;
    private ArrayList<User> team2;
    
    public Game(ArrayList<User> t1, ArrayList<User> t2){
        this.team1 = t1;
        this.team2 = t2;
    }

    public void  updateRanks (ArrayList<User> t, int r){
        int oldR;
        for(User u: t){
            oldR = u.getRank();
            u.updateRank(oldR+r);
        }
    }

    public void playGame(){
        Random rand = new Random();
        int result = rand.nextInt(2);
        //if 0 team 1 win, if 1 team 2 win
        if(result==0){
            updateRanks(this.team1,1);
            updateRanks(this.team2,-1);
        }else{
            updateRanks(this.team2,1);
            updateRanks(this.team1,-1);
        }
    }
}
