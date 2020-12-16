

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;


public class Boot {
    public static void main(String[] args) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        DB db = new DB();

        try {
            String commandLine;
            while ((commandLine = reader.readLine()) != null) {
                try{
                    if(!db.handleCommand(commandLine)){
                        break;
                    }
                }
                catch(Exception e){
                    System.err.println("cannot resolve command:"+commandLine);
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
