/*
*/
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

//Worker which represents one client on the server
class Worker implements Runnable{
    private Socket skt; //socket connected to the client
    private GameData gdt; //all data users
    private BufferedReader in; //input from the client
    private PrintWriter out; //output to the client
    
    //create and initiate a Worker
    public Worker(Socket skt, GameData gdt){
        this.skt = skt;
        this.gdt = gdt;
        try{
            //open input and output
            this.in = new BufferedReader(new InputStreamReader(this.skt.getInputStream()));
            this.out = new PrintWriter(this.skt.getOutputStream(), true);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    //finds what type of message was sent from client
    private int MessageType(String s){
        int ret = 0; //value returned if type was not found
        if(s!=null){
            //checks if the begining and end of the message has $
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
    // interprets the message recieved based on it's type
    private void parseLine(String s){
        int type = MessageType(s);
        if(type!=0){
            //remove used syntax from message
            s = s.substring(2, s.length()-2);
            //splits the message on its components
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
                if(this.gdt.validUser(aux[0])){
                    String uName=aux[0];
                    this.out.println("UQJoin");
                    System.out.println("UQJoin");
                    Game g=this.gdt.joinWQueue(uName);
                    int myTeam=g.setup(uName);
                    this.out.println("Joined team" +myTeam);
                    System.out.println("Joined team" +myTeam);
                }else{ 
                    this.out.println("UQNotJoin");
                    System.out.println("UQNotJoin");
                }
            }
        }   
    }

    public void run(){
        String inS;
            
        System.out.println("Connection Received");
        try{
            //read messages from client
            while((inS = this.in.readLine()) != null){
                parseLine(inS);
            }       
            
            //safely close IO streams
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
    private int prt; //port server
    private GameData gdt; //all data users

    public static void main(String args[]){
        Server mSrv = new Server(9999);
        try{
            ServerSocket sSkt = new ServerSocket(mSrv.prt);
            while(true){
                Socket skt = sSkt.accept();

                //create and run a thread for each client
                Thread wrk = new Thread(new Worker(skt,mSrv.gdt));
                wrk.start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    //create a server on specific port
    public Server(int port){
        this.prt = port;
        this.gdt = new GameData();
    }
}

//class that saves all information for the server
class GameData{
    private Map<String,User> users;
    private WaitQueue wQueue;
    private Map<String,Hero> heros;

    public GameData(){
        this.users = new HashMap<>();
        this.wQueue = new WaitQueue();
        this.heros = new HashMap<>();
        this.createHeros(30);
    }

    public void createHeros(int N){
        int i;
        for(i=0;i<N;i++){
            String name = "Hero"+i;
            this.heros.put(name,new Hero(i,name));
        }
    }
    
    //get a user with a specific username
    public User getUser(String uName){
        return this.users.get(uName).clone();
    }
    
    //add a new user
    public synchronized boolean addUser(String uName, String pass, String mail){
        if(this.userExists(uName)) return false;
        this.users.put(uName,new User(uName,mail,pass));
        return true; 
    }
    
    //checks if the username and the password match with what is stored
    public boolean passwordMatch(String uName,String pass){
        User aux = this.users.get(uName);

        if(aux != null) return aux.getPassword().equals(pass);
        else return false;
    }
    
    //get a rank user
    public int getRank(String username){
        User aux = this.users.get(username);

        if(aux == null) return -1;
        else return aux.getRank();
    }

    public boolean validUser(String username){
        int rRank=this.getRank(username);
        if(rRank==-1) return false;
        else return true;
    }

    public Game joinWQueue(String username){
        int rank = this.getRank(username);
        return this.wQueue.joinQueue(rank);
    }
    
    //checks if the user exists
    public boolean userExists(String uName){
        return this.users.containsKey(uName);
    }

    //public List<String> getHeros(){}
}

class WaitQueue{
    private int[] rankQueue;
    private ReentrantLock rlock;
    private Condition[] condLock;
    private int gameNo;                 //records no of games that have been played/started
    private Game nextGame;              //holds the next game being held

    public WaitQueue(){
        this.rankQueue = new int[10];
        this.rlock =new ReentrantLock();
        this.condLock = new Condition[10];
        for(int i=0;i<10;i++) this.condLock[i] = this.rlock.newCondition();
        this.gameNo=0;
        this.nextGame=null;
    }
   
    public Game joinQueue(int rank){
        this.rlock.lock();
        int local=this.gameNo;

        try{
            this.rankQueue[rank] ++;

            if(rank == 0 && this.rankQueue[rank]+this.rankQueue[rank+1]<10)
                while(local==this.gameNo)
                    this.condLock[rank].await();
            else if(rank == 9 && this.rankQueue[rank-1]+this.rankQueue[rank]<10)
                while(local==this.gameNo)
                    this.condLock[rank].await();
            else if(rank != 0 && rank != 9 && this.rankQueue[rank-1] + this.rankQueue[rank] < 10 && this.rankQueue[rank]+this.rankQueue[rank+1]<10)
                while(local==this.gameNo)
                    this.condLock[rank].await();
            else{
                this.gameNo++;
                this.nextGame=new Game();

                this.condLock[rank].signalAll();
                if(this.rankQueue[rank]<10 && (rank==0 || this.rankQueue[rank]+this.rankQueue[rank+1]>=10)){
                    this.condLock[rank+1].signalAll();
                    this.rankQueue[rank+1]=0;
                }else if(this.rankQueue[rank]<10 && (rank == 9 || this.rankQueue[rank-1] + this.rankQueue[rank]>=10)){
                    this.condLock[rank-1].signalAll();
                    this.rankQueue[rank-1]=0;
                }

                this.rankQueue[rank]=0;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.rlock.unlock();
        }
        return this.nextGame;
    }
}

//class that simulate a game and update rank users
class Game{
    private Map<String,Integer> team1; //composition of team 1
    private Map<String,Integer> team2; //composition of team 2
    private ReentrantLock setupLock;
    private ReentrantLock[] teamLock;
    private Condition allReady;
    
    public Game(){
        this.team1 = new HashMap<String,Integer>();
        this.team2 = new HashMap<String,Integer>();        
        this.setupLock = new ReentrantLock();
        this.teamLock = new ReentrantLock[2];
        this.allReady=this.setupLock.newCondition();
    }
    
    //update the rank users
    public void  updateRanks (ArrayList<User> t, int r){
        int oldR;
        for(User u: t){
            oldR = u.getRank();
            u.updateRank(oldR+r);
        }
    }

    public int setup(String uName){
        int r=-1;
        try{
            this.setupLock.lock();
            if(this.team1.size() < 5){
                this.team1.put(uName,-1);
                r=1;
            }
            else{ 
                this.team2.put(uName,-1);
                r=2;
            }
            if(this.team2.size()<5) this.allReady.await();
            else this.allReady.notifyAll();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            this.setupLock.unlock();
        }
        return r;
    }

    public boolean heroPick(String uName,int choice){
        int team;
        boolean success=false;

        if(this.team1.containsKey(uName)) team = 0;
        else team = 1;
        
        this.teamLock[1].lock();
        switch(team){
            case 0:
                if((success=!this.team1.containsValue(choice))) //check if the hero has been chosen   
                    this.team1.put(uName,choice); 
                break;
            case 1:
                if((success=!this.team2.containsValue(choice))) //check if the hero has been chosen
                    this.team2.put(uName,choice); 
                break;
            default:
                break;
        }
        this.teamLock[team].unlock();
        return success;
    }

    //simulate a game
    public void playGame(){
        Random rand = new Random();
        int result = rand.nextInt(2);
        //if 0 team 1 win, if 1 team 2 win
        if(result==0){
            //updateRanks(this.team1,1);
            //updateRanks(this.team2,-1);
        }else{
            //updateRanks(this.team2,1);
            //updateRanks(this.team1,-1);
        }
    }

    public void heroChoice(){

    }
}
