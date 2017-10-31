package net.flow9.firebasebasic2;

/**
 * Created by pc on 10/31/2017.
 */

public class User {
    String id;
    String token;

    public User(){
        // default for firebase realtime database
    }

    public User(String id, String token){
        this.id = id;
        this.token = token;
    }
}
