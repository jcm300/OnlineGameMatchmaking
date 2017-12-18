/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author miguelq
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
