

import java.io.BufferedReader;

import java.io.InputStreamReader;


public class Boot {
    public static void main(String[] args) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        DB db = new DB();
        try {
            String commandLine;
            while ((commandLine = reader.readLine()) != null) {
                if(!db.handleCommand(commandLine)){
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
