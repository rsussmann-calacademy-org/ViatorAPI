package co.launchable.api.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Scanner;

/**
 * Created by Michael on 3/24/2015.
 */
public class CommandLineCapture {
    public static String captureCommand(String command) {
        StringBuffer sb = new StringBuffer();
        try {
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader  = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    public static void main(String args[]) {
        String out = CommandLineCapture.captureCommand("tail /opt/apache-tomcat-7.0.59/logs/catalina.out");
        System.out.print(out);
    }
}
