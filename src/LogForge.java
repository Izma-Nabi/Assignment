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
        int valid=0;
        int invalid=0;

//        File filePath = new File(args[0]);
        File filePath = new File("system.log");
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                total++;
                String level = getLevel(line);
                if(isValidRecord(line)) {
                    valid++;
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
                else{
                    invalid++;
                }
            }
            br.close();
        }
        catch (IOException ex) {
            System.out.println(ex.getMessage());
            return;
        }
        System.out.println("Total records: " + total);
        System.out.println("Valid Records: " + valid);
        System.out.println("Invalid Records: " + invalid);
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
    public static Boolean isValidRecord(String Line){
        int pipeCount=0;
        for (int i=0;i<Line.length();i++){
            if(Line.charAt(i)=='|'){
                pipeCount++;
            }
        }
        if(pipeCount!=4){
            return false;
        }
        String Level = getLevel(Line);
        if(!Level.equals("INFO")&& !Level.equals("ERROR") && !Level.equals("WARN")){
            return false;
        }

        String requestId= getRequestId(Line);
        if(requestId.length()==0){
            return false;
        }
        boolean hasNonZero = false;
        for(int i=0;i<requestId.length();i++){
            char num=requestId.charAt(i);
            if(num<'0' || num>'9'){
                return false;
            }
            if (num != '0'){
                hasNonZero = true;
            }
        }
        if (!hasNonZero){
            return false;
        }
        String timestamp = getTimestamp(Line);
        if (timestamp.length() != 19) return false;
        if (timestamp.charAt(4) != '-' || timestamp.charAt(7) != '-' ||
                timestamp.charAt(10) != ' ' || timestamp.charAt(13) != ':' ||
                timestamp.charAt(16) != ':') {
            return false;
        }
        int month = (timestamp.charAt(5) - '0') * 10 + (timestamp.charAt(6) - '0');
        if (month < 1 || month > 12) {
            return false;
        }

        return true;
    }

    public static String getTimestamp(String Line){
        String time="";
        for(int i=0;i<Line.length();i++){
            if(Line.charAt(i) == '|'){
                break;
            }
            time+=Line.charAt(i);
        }
        return time;
    }

    public static String getRequestId(String Line){
        String req="";
        int pipeCount=0;
        for(int i=0;i<Line.length();i++){
            if(Line.charAt(i)=='|'){
                pipeCount++;
                continue;
            }
            if(pipeCount==3){
                req+=Line.charAt(i);
            }
            else if(pipeCount==4){
                break;
            }
        }
        return req;
    }
}

