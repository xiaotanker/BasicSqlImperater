

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Boot {
    public static void main(String[]args){


        if(args.length<1){
            System.out.println("input needed: commandFile");
        }
        try {
            BufferedReader reader = new BufferedReader(new FileReader(args[0]));
            DB db= new DB();
            String commandLine;
            while((commandLine= reader.readLine())!=null){
                db.handleCommand(commandLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
