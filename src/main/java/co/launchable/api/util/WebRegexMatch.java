package co.launchable.api.util;

/**
 * Created by mike on 1/19/17.
 */
public class WebRegexMatch {

    public static void main(String[] args) {
        String command = args[0];
        String pattern = args[1];

        String out = CommandLineCapture.captureCommand(command);
        if (out.matches(pattern))
            System.exit(0);
        System.exit(1);
    }
}
