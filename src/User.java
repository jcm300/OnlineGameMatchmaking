/*
 */

public class User {
    private String uName;
    private String email;
    private String password;
    private int rank;
    
    public User(String name, String uEmail, String pass){
        this.uName=name;
        this.email=uEmail;
        this.password=pass;
        this.rank=0;
    }
    
    public void updateRank(int r){
        if(r<0) this.rank=0;
        else if(r>9) this.rank=9;
             else this.rank = r;
    }

    public int getRank(){
        return this.rank;
    }

    public String getPassword(){
        return this.password;
    }

    public User clone(){
        return new User(this.uName,this.email,this.password);
    }
}
