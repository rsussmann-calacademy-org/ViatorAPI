package co.launchable.api.util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class LineCounter {
    public void count(File dir) throws Exception {
        int lineCount = 0;
        int fileCount = 0;
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            if (file.getName().endsWith(".java")) {
                fileCount++;
                BufferedReader reader = new BufferedReader(new FileReader(file));
                while (reader.readLine() != null) {
                    lineCount++;
                }
            }
        }
        System.out.println(lineCount + " lines in " + fileCount + " files");
    }

    public static void main(String args[]) {
        LineCounter lineCounter = new LineCounter();
        File directoryBefore = new File("C:\\\\dev\\projects\\MyraAndroid3\\MyRA\\src\\com\\a1\\myrapp");
        File directoryAfter= new File("C:\\\\dev\\projects\\MyraAndroid2\\MyRA\\src\\com\\a1\\myrapp");
        File directoryBeforeAdapter = new File("C:\\\\dev\\projects\\MyraAndroid3\\MyRA\\src\\com\\a1\\myrapp\\adapter");
        File directoryAfterAdapter= new File("C:\\\\dev\\projects\\MyraAndroid2\\MyRA\\src\\com\\a1\\myrapp\\adapter");
        try {
            lineCounter.count(directoryBefore);
            lineCounter.count(directoryAfter);
            lineCounter.count(directoryBeforeAdapter);
            lineCounter.count(directoryAfterAdapter);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
