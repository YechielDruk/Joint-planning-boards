package server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class PasswordHashing {
    
    //Encrypt the password before saving it to the database
    public static String passwordHashing(String password){
        MessageDigest messageDigest;
        String hashed_password = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA");
            messageDigest.update(password.getBytes());
            byte[] resultat = messageDigest.digest();
            StringBuilder stringBuilder = new StringBuilder();
            for (byte b : resultat) {
                stringBuilder.append(String.format("%02x",b));
            }

        hashed_password = stringBuilder.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hashed_password;
    }
}
