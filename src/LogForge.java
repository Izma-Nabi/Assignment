import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogForge {

    public static void main(String[] args) {
//        if (args.length < 1) {
//            System.out.println("Usage: java LogForge system.log");
//            return;
//        }
        FileReader fr = null;
        BufferedReader br = null;
        int total = 0;
        int info = 0;
        int warn = 0;
        int error = 0;

//        File filePath = new File(args[0]);
        File filePath = new File("system.log");
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                total++;
                String level = getLevel(line);
                if (level.equals("INFO")) {
                    info++;
                }
                if (level.equals("ERROR")) {
                    error++;
                }
                if (level.equals("WARN")) {
                    warn++;
                }
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return;
        }

        System.out.println("Total records: " + total);
        System.out.println("INFO: " + info);
        System.out.println("WARN: " + warn);
        System.out.println("ERROR: " + error);
    }

    public static String getLevel(String line) {
        int pipeCount = 0;
        int start = -1;
        int end = -1;
        for (int i = 0; i < line.length(); i++) {
            if (line.charAt(i) == '|') {
                pipeCount++;
                if (pipeCount == 2) {
                    start = i + 1;
                }
                if (pipeCount == 3) {
                    end = i;
                }
            }
        }
        String result = "";
        if (start != -1 && end != -1) {
            for (int i = start; i < end; i++) {
                result += line.charAt(i);
            }
        }
        return result;
    }
}

