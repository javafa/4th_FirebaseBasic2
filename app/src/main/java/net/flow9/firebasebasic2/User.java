package net.flow9.firebasebasic2;

/**
 * Created by pc on 10/31/2017.
 */

public class User {
    public String id;
    public String token;
    public String email;

    public User(){
        // default for firebase realtime database
    }

    public User(String id, String token, String email){
        this.id = id;
        this.token = token;
        this.email = email;
    }
}
