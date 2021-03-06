package com.sanilk.harmonia.networking.threads;

import com.sanilk.harmonia.entities.User;
import com.sanilk.harmonia.networking.JSONParser;
import com.sanilk.harmonia.networking.NetworkHandler;
import com.sanilk.harmonia.response_interfaces.SignUpResponseInterface;
import com.sanilk.harmonia.responses.SignUpResponse;

import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class SignUpThread implements MyThread {
    Thread t;
    boolean needToStop=false;
    private User user;

    private SignUpResponseInterface signUpResponseInterface;

    private final static long MILLISECONDS=1000;

    public SignUpThread(User user, SignUpResponseInterface responseInterface){
        t=new Thread(this);
        this.user=user;
        this.signUpResponseInterface=responseInterface;
    }

    public void startThread(){
        t.start();
    }

    @Override
    public void run() {
        while (true) {
            try {
                if(needToStop){
                    break;
                }
                Thread.sleep(MILLISECONDS);
                URL url = new URL(NetworkHandler.ADDRESS);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoInput(true);
                connection.setDoOutput(true);

                DataOutputStream dos = new DataOutputStream(connection.getOutputStream());

                JSONObject object=new JSONObject();
                object.put("request_type", "CREATE_USER");
                object.put("user_name", user.getName());
                object.put("password", user.getPassword());
                object.put("email", user.getEmail());
                object.put("first_name", "tempFirstName");
                object.put("last_name", "tempLastName");

                String jsonObject=object.toString();

                dos.writeUTF(jsonObject);
                dos.flush();
                dos.close();

                DataInputStream din=new DataInputStream(connection.getInputStream());
                String response=din.readUTF();
                din.close();

                connection.disconnect();

                JSONParser jsonParser=new JSONParser();
                SignUpResponse signUpResponse=(SignUpResponse)jsonParser.parse(response);

                signUpResponseInterface.responseReceived(signUpResponse);

                //check for response
                //break;

            } catch (Exception e) {
                e.printStackTrace();
                signUpResponseInterface.onFailure();
            }
            break;
        }
    }

    public void stopThread(){
        needToStop=true;
    }

}
