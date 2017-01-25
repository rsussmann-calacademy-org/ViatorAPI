package co.launchable.api.marketo;

import java.io.BufferedReader;
import java.io.FileReader;

/**
 * Created by michaelmcelligott on 2/16/14.
 */
public class ActionLoadLeads {

    public static void main(String[] args) {
        try {

            BufferedReader reader = new BufferedReader(new FileReader(args[0]));

            String line = reader.readLine();
            while (line != null) {
                reader.readLine();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
