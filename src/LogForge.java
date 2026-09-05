import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class LogForge {

    static class ServiceStats{
        private String serviceName;
        private int infoCount;
        private int warnCount;
        private int errorCount;
        private int total=0;

        public ServiceStats(String sname){
            this.serviceName=sname;
            this.total=0;
            this.errorCount=0;
            this.infoCount=0;
            this.warnCount=0;
        }
        public void recordLog(String level){
            total++;
            if(level.equals("INFO")){
                infoCount++;
            }
            else if(level.equals("ERROR")){
                errorCount++;
            }
            else if(level.equals("WARN")){
                warnCount++;
            }
        }

        public double getErrorRate() {
            if (total == 0) return 0.0;
            return ((double) errorCount / total) * 100.0;
        }
        public void displayServiceStats(){
            System.out.println(serviceName + " total=" + total + " info=" + infoCount + " warn=" + warnCount + " error=" + errorCount);
        }
    }
    static class LogEntry{
        private String timeStamp;
        private int reqId;
        private String service;
        private String level;
        private String message;


        public LogEntry(String timeStamp,int reqId, String service, String level, String message){
            this.timeStamp=timeStamp;
            this.reqId=reqId;
            this.service=service;
            this.level=level;
            this.message=message;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public int getReqId() {
            return reqId;
        }

        public String getService() {
            return service;
        }

        public String getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public boolean matchRecord(int requestId) {
            return this.reqId == requestId;
        }

        void display_log_entry(){
            System.out.println("Time Stamp: "+ timeStamp);
            System.out.println("Service: "+ service);
            System.out.println("Level: "+ level);
            System.out.println("Request Id: "+ reqId);
            System.out.println("Message: "+ message);
        }
    }

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

        LogEntry[] log=new LogEntry[5];
        int logCount=0;

        ServiceStats[] services=new ServiceStats[5];
        int serviceCount=0;

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

                    String timestamp=getField(line,0);
                    String service=getField(line,1);
                    String levell=getField(line,2);
                    int reqid = parsePositiveInt(getField(line, 3));
                    String msg=getField(line,4);

                    LogEntry entry=new LogEntry(timestamp,reqid,service,levell,msg);

                    if(logCount==log.length){
                        log=resizeLog(log);
                    }
                    log[logCount]=entry;
                    logCount++;
                    if (level.equals("INFO")) {
                        info++;
                    }
                    if (level.equals("ERROR")) {
                        error++;
                    }
                    if (level.equals("WARN")) {
                        warn++;
                    }

                    ServiceStats servicecheck=null;
                    for(int i=0;i<serviceCount;i++){
                        if(services[i].serviceName.equals(service)){
                            servicecheck=services[i];
                            break;
                        }
                    }
                    if(servicecheck == null){
                        ServiceStats newStat =new ServiceStats(service);
                        if(serviceCount==services.length){
                            services=resizeServices(services);
                        }
                        services[serviceCount]=newStat;
                        servicecheck=newStat;
                        serviceCount++;
                    }
                    servicecheck.recordLog(levell);
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

        sortServicesStats(services,serviceCount);

        System.out.println("\n--- VERIFYING LOG ENTRY OBJECTS ---");
        for (int i = 0; i < 5; i++) {
            System.out.println("Entry #" + (i + 1) + ":");
            log[i].display_log_entry();
            System.out.println("---------------------------------");
        }

        System.out.println("\n--- VERIFYING Servise Stats ENTRY OBJECTS ---");
        for (int i = 0; i < serviceCount; i++) {
            System.out.println("Entry #" + (i + 1) + ":");
            services[i].displayServiceStats();
            System.out.println("---------------------------------");
        }

        System.out.println("\n--- WORST SERVICES (SORTED BY ERROR RATE) ---");
        for (int i = 0; i < serviceCount; i++) {
            services[i].displayServiceStats();
        }
    }

    public static LogEntry[] resizeLog(LogEntry[] old){
        LogEntry[] newLog=new LogEntry[old.length*2];
        for(int i=0;i< old.length;i++){
            newLog[i]=old[i];
        }
        return newLog;
    }

    public static ServiceStats[] resizeServices(ServiceStats[] old){
        ServiceStats[] newStat=new ServiceStats[old.length*2];
        for(int i=0;i<old.length;i++){
            newStat[i]=old[i];
        }
        return newStat;
    }

    public static Boolean checkServiceName(String name1, String name2){
        return name1.compareTo(name2)>0;
    }
    public static void sortServicesStats(ServiceStats[] stats, int count){
        if(count<1) return;

        for(int i=0;i<count;i++){
            for(int j=0;j<count-i-1;j++){
                double error1=stats[j].getErrorRate();
                double error2=stats[j+1].getErrorRate();

                boolean shouldSwap=false;
                if(error1>error2){
                    shouldSwap=true;
                }

                if(error1==error2){
                    if(checkServiceName(stats[j].serviceName,stats[j+1].serviceName)){
                        shouldSwap=true;
                    }
                }
                if(shouldSwap){
                    ServiceStats temp=stats[j];
                    stats[j]=stats[j+1];
                    stats[j+1]=temp;
                }
            }
        }

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

    public static int parsePositiveInt(String str) {
        int num = 0;
        for (int i = 0; i < str.length(); i++) {
            num = num * 10 + (str.charAt(i) - '0');
        }
        return num;
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

    public static String getField(String Line, int target) {
        int pipeCount=0;
        String result= "";
        for(int i=0;i<Line.length();i++){
            char c=Line.charAt(i);
            if(c=='|'){
                if(pipeCount==target){
                    return result;
                }
                pipeCount++;
                result="";
            }
            else{
                result+=c;
            }
        }
        if(pipeCount==target){
            return result;
        }
        return "";
    }
}

