package co.launchable.api.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Michael on 5/19/2015.
 */
public class OpportunityLogReader {
    public static void main(String args[]) {

        try {
            StringBuffer sb = new StringBuffer();
            BufferedReader reader = new BufferedReader(new FileReader(new File("C:\\Dropbox\\Clients\\AcademyOfScience\\MarketoSynchronization\\OpportunityLog.txt")));
            String line = reader.readLine();
            while (line != null) {
                sb.append(line);
                line = reader.readLine();
            }
            String input = sb.toString();

            String regexMObject = "<id>(\\d*)</id>.*?<attribList>(.*?)</attribList>";
            String regexFivePart = "<value>(.*?)</value>.*?<value>(.*?)</value>.*?<value>(.*?)</value>.*?<value>(.*?)</value>.*?<value>(.*?)</value>";
            String regexFourPart = "<value>(.*?)</value>.*?<value>(.*?)</value>.*?<value>(.*?)</value>.*?<value>(.*?)</value>";

            Pattern patternMObject = Pattern.compile(regexMObject);
            Pattern patternFourPart = Pattern.compile(regexFourPart);
            Pattern patternFivePart = Pattern.compile(regexFivePart);

            Matcher matcher = patternMObject.matcher(input);

            while(matcher.find()) {
                String id = matcher.group(1);
                String attribListValue = matcher.group(2);

                Matcher matcherFive = patternFivePart.matcher(attribListValue);
                if (matcherFive.find()) {
                    System.out.print(id);
                    System.out.print(",");
                    System.out.print(matcherFive.group(1));
                    System.out.print(",");
                    System.out.print(matcherFive.group(2));
                    System.out.print(",");
                    System.out.print(matcherFive.group(3));
                    System.out.print(",");
                    System.out.print(matcherFive.group(4));
                    System.out.print(",");
                    System.out.println(matcherFive.group(5));
                } else {
                    Matcher matcherFour = patternFourPart.matcher(attribListValue);
                    if (matcherFour.find()) {
                        System.out.print(id);
                        System.out.print(",0,");
                        System.out.print(matcherFour.group(1));
                        System.out.print(",");
                        System.out.print(matcherFour.group(2));
                        System.out.print(",");
                        System.out.print(matcherFour.group(3));
                        System.out.print(",");
                        System.out.println(matcherFour.group(4));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
