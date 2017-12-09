import java.io.*;
import java.net.*;

//Class that each gamer will execute
public class Client {
    private String username;

    private Socket authenticate(String username, String password) throws IOException{
        Socket s = new Socket("127.0.0.1",9999);

        PrintWriter out = new PrintWriter(s.getOutputStream(), true);
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        
        String current;
        out.println(username + ";" + password);

        while((current = in.readLine()) != null){
            in.close();
            out.close();
            if(current.equals("NotAccepted")){
                s.close();
                s = null;
            }
        }
        return s;
    }
}
