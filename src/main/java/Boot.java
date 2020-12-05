

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class Boot {
    public static void main(String[] args) {

        Scanner reader = new Scanner(System.in);
        DB db = new DB();
        while (true) {
            try {
                String commandLine;
                commandLine = reader.nextLine();
                db.handleCommand(commandLine);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }
}
