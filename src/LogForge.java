import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class LogForge {

    static class ServiceStats {
        private String serviceName;
        private int infoCount;
        private int warnCount;
        private int errorCount;
        private int total;

        public ServiceStats(String sname) {
            this.serviceName = sname;
            this.total = 0;
            this.errorCount = 0;
            this.infoCount = 0;
            this.warnCount = 0;
        }

        public void recordLog(String level) {
            total++;
            if (level.equals("INFO")) {
                infoCount++;
            } else if (level.equals("ERROR")) {
                errorCount++;
            } else if (level.equals("WARN")) {
                warnCount++;
            }
        }

        public double getErrorRate() {
            if (total == 0) return 0.0;
            return ((double) errorCount / total) * 100.0;
        }

        public void displayServiceStats(PrintWriter writer) {
            writer.println(serviceName + " total=" + total + " info=" + infoCount + " warn=" + warnCount + " error=" + errorCount);
        }

        public void displayWorstServiceStats(PrintWriter writer) {
            writer.printf("%s total=%d info=%d warn=%d error=%d errorRate=%.5f%%\n",
                    serviceName, total, infoCount, warnCount, errorCount, getErrorRate());
        }
    }

    static class LogEntry {
        private String timeStamp;
        private int reqId;
        private String service;
        private String level;
        private String message;

        public LogEntry(String timeStamp, int reqId, String service, String level, String message) {
            this.timeStamp = timeStamp;
            this.reqId = reqId;
            this.service = service;
            this.level = level;
            this.message = message;
        }

        public String getTimeStamp() {
            return timeStamp;
        }

        public String getOnlyTime() {
            if (timeStamp != null && timeStamp.length() >= 19) {
                return timeStamp.substring(11, 19);
            }
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

        void display_log_entry(PrintWriter writer) {
            writer.println("Time Stamp: " + getOnlyTime());
            writer.println("Service: " + service);
            writer.println("Level: " + level);
            writer.println("Request Id: " + reqId);
            writer.println("Message: " + message);
        }
    }

    static class incident {
        private String service;
        private String firstTimeStamp;
        private String lastTimeStamp;
        private int count;

        public incident(String s, String f, String l, int c) {
            this.service = s;
            this.firstTimeStamp = f;
            this.lastTimeStamp = l;
            this.count = c;
        }

        public String getService() {
            return service;
        }

        public String getFirstTimeStamp() {
            return firstTimeStamp;
        }

        public String getLastTimeStamp() {
            return lastTimeStamp;
        }

        public int getCount() {
            return count;
        }

        public String getOnlyTime(String ts) {
            if (ts != null && ts.length() >= 19) {
                return ts.substring(11, 19);
            }
            return ts;
        }

        public void displayIncident(int index, PrintWriter writer) {
            writer.println("Incident #" + index + ": " + service + " (" + count + " errors) from "
                    + getOnlyTime(firstTimeStamp) + " to " + getOnlyTime(lastTimeStamp));
        }
    }

    static class RequestStats {
        private int reqId;
        private int recordCount;
        private int errorCount;
        private String[] services;
        private int serviceCount;

        public RequestStats(int id) {
            this.reqId = id;
            this.recordCount = 0;
            this.errorCount = 0;
            this.services = new String[5];
            this.serviceCount = 0;
        }

        public int getReqId() {
            return reqId;
        }

        public int getRecordCount() {
            return recordCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public String[] getServices() {
            return services;
        }

        public int getServiceCount() {
            return serviceCount;
        }

        public void displayRequestStats(PrintWriter writer) {
            String status = (errorCount > 0) ? "FAILED" : "SUCCESS";
            writer.println("Request " + reqId + " : " + status);
            writer.println("Records: " + recordCount);
            writer.println("Errors: " + errorCount);
            writer.print("Services:");
            for (int i = 0; i < serviceCount; i++) {
                writer.print(" " + services[i]);
            }
            writer.println("\n");
        }
    }

    public static void main(String[] args) {

        String inputFileName;
        if (args.length >= 1) {
            inputFileName = args[0];
        } else {
            inputFileName = "system.log";
        }

        String outputFileName;
        if (args.length >= 2) {
            outputFileName = args[1];
        } else {
            outputFileName = "logforge_report.txt";
        }


        FileReader fr = null;
        BufferedReader br = null;
        PrintWriter writer = null;

        int total = 0;
        int info = 0;
        int warn = 0;
        int error = 0;
        int valid = 0;
        int invalid = 0;

        LogEntry[] log = new LogEntry[5];
        int logCount = 0;

        ServiceStats[] services = new ServiceStats[5];
        int serviceCount = 0;

        RequestStats[] requests = new RequestStats[5];
        int requestCount = 0;

        File filePath = new File(inputFileName);
        try {
            fr = new FileReader(filePath);
            br = new BufferedReader(fr);

            String line;
            while ((line = br.readLine()) != null) {
                total++;
                String level = getLevel(line);
                if (isValidRecord(line)) {
                    valid++;

                    String timestamp = getField(line, 0);
                    String service = getField(line, 1);
                    String levell = getField(line, 2);
                    int reqid = parsePositiveInt(getField(line, 3));
                    String msg = getField(line, 4);

                    LogEntry entry = new LogEntry(timestamp, reqid, service, levell, msg);

                    if (logCount == log.length) {
                        log = resizeLog(log);
                    }
                    log[logCount] = entry;
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

                } else {
                    invalid++;
                }
            }
            br.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
            return;
        }

        // Q8: Sort Log Entries Chronologically
        sortLogEntries(log, logCount);

        // Perform analysis on sorted entries
        for (int i = 0; i < logCount; i++) {
            LogEntry entry = log[i];
            String service = entry.getService();
            String levell = entry.getLevel();
            int reqid = entry.getReqId();

            // Service Stats
            ServiceStats servicecheck = null;
            for (int k = 0; k < serviceCount; k++) {
                if (services[k].serviceName.equals(service)) {
                    servicecheck = services[k];
                    break;
                }
            }
            if (servicecheck == null) {
                ServiceStats newStat = new ServiceStats(service);
                if (serviceCount == services.length) {
                    services = resizeServices(services);
                }
                services[serviceCount] = newStat;
                servicecheck = newStat;
                serviceCount++;
            }
            servicecheck.recordLog(levell);

            // Request Stats
            RequestStats requestcheck = null;
            for (int k = 0; k < requestCount; k++) {
                if (requests[k].getReqId() == reqid) {
                    requestcheck = requests[k];
                    break;
                }
            }
            if (requestcheck == null) {
                RequestStats newReq = new RequestStats(reqid);
                if (requestCount == requests.length) {
                    requests = resizeRequests(requests);
                }
                requests[requestCount] = newReq;
                requestcheck = newReq;
                requestCount++;
            }
            recordRequestLog(requestcheck, service, levell);
        }

        // Open Report File Writer
        try {
            writer = new PrintWriter(new FileWriter(outputFileName));

            // Summary Section
            writer.println("Total records: " + total);
            writer.println("Valid Records: " + valid);
            writer.println("Invalid Records: " + invalid);
            writer.println("ERROR: " + error);
            writer.println("INFO: " + info);
            writer.println("WARN: " + warn);

            sortServicesStats(services, serviceCount);

            writer.println("\n--- VERIFYING LOG ENTRY OBJECTS ---");
            int verifyLimit = logCount < 5 ? logCount : 5;
            for (int i = 0; i < verifyLimit; i++) {
                writer.println("Entry #" + (i + 1) + ":");
                log[i].display_log_entry(writer);
                writer.println("---------------------------------");
            }

            writer.println("\n--- VERIFYING Service Stats ENTRY OBJECTS ---");
            for (int i = 0; i < serviceCount; i++) {
                writer.println("Entry #" + (i + 1) + ":");
                services[i].displayServiceStats(writer);
                writer.println("---------------------------------");
            }

            writer.println("\n--- WORST SERVICES (SORTED BY ERROR RATE) ---");
            for (int i = 0; i < serviceCount; i++) {
                services[i].displayWorstServiceStats(writer);
            }

            writer.println("\n--- DETECTED INCIDENTS ---");
            detectIncident(log, logCount, writer);

            writer.println("\n--- REQUEST STATS ---");
            for (int i = 0; i < requestCount; i++) {
                requests[i].displayRequestStats(writer);
            }

            writer.println("EOD");

            writer.close();
        } catch (IOException ex) {
            System.out.println("Error writing report file: " + ex.getMessage());
        }
    }

    public static int compareTimestamps(String t1, String t2) {
        long sec1 = timestampToSeconds(t1);
        long sec2 = timestampToSeconds(t2);
        if (sec1 < sec2) return -1;
        if (sec1 > sec2) return 1;
        return 0;
    }

    public static void sortLogEntries(LogEntry[] entries, int count) {
        if (count <= 1) return;

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count - i - 1; j++) {
                int cmp = compareTimestamps(entries[j].getTimeStamp(), entries[j + 1].getTimeStamp());

                boolean shouldSwap = false;
                if (cmp > 0) {
                    shouldSwap = true;
                } else if (cmp == 0) {
                    shouldSwap = true;
                }

                if (shouldSwap) {
                    LogEntry temp = entries[j];
                    entries[j] = entries[j + 1];
                    entries[j + 1] = temp;
                }
            }
        }
    }

    public static LogEntry[] resizeLog(LogEntry[] old) {
        LogEntry[] newLog = new LogEntry[old.length * 2];
        for (int i = 0; i < old.length; i++) {
            newLog[i] = old[i];
        }
        return newLog;
    }

    public static ServiceStats[] resizeServices(ServiceStats[] old) {
        ServiceStats[] newStat = new ServiceStats[old.length * 2];
        for (int i = 0; i < old.length; i++) {
            newStat[i] = old[i];
        }
        return newStat;
    }

    public static RequestStats[] resizeRequests(RequestStats[] old) {
        RequestStats[] newReq = new RequestStats[old.length * 2];
        for (int i = 0; i < old.length; i++) {
            newReq[i] = old[i];
        }
        return newReq;
    }

    public static Boolean checkServiceName(String a, String b) {
        int min = a.length() < b.length() ? a.length() : b.length();

        for (int i = 0; i < min; i++) {
            if (a.charAt(i) < b.charAt(i)) return false;
            if (a.charAt(i) > b.charAt(i)) return true;
        }

        return a.length() > b.length();
    }

    public static void sortServicesStats(ServiceStats[] stats, int count) {
        if (count < 1) return;

        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count - i - 1; j++) {
                double error1 = stats[j].getErrorRate();
                double error2 = stats[j + 1].getErrorRate();

                boolean shouldSwap = false;
                if (error1 < error2) {
                    shouldSwap = true;
                }

                if (error1 == error2) {
                    if (checkServiceName(stats[j].serviceName, stats[j + 1].serviceName)) {
                        shouldSwap = true;
                    }
                }
                if (shouldSwap) {
                    ServiceStats temp = stats[j];
                    stats[j] = stats[j + 1];
                    stats[j + 1] = temp;
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

    public static Boolean isValidRecord(String Line) {
        int pipeCount = 0;
        for (int i = 0; i < Line.length(); i++) {
            if (Line.charAt(i) == '|') {
                pipeCount++;
            }
        }
        if (pipeCount != 4) {
            return false;
        }
        String Level = getLevel(Line);
        if (!Level.equals("INFO") && !Level.equals("ERROR") && !Level.equals("WARN")) {
            return false;
        }

        String requestId = getRequestId(Line);
        if (requestId.length() == 0) {
            return false;
        }
        boolean hasNonZero = false;
        for (int i = 0; i < requestId.length(); i++) {
            char num = requestId.charAt(i);
            if (num < '0' || num > '9') {
                return false;
            }
            if (num != '0') {
                hasNonZero = true;
            }
        }
        if (!hasNonZero) {
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

    public static long getTimeDifference(String t1, String t2) {
        long sec1 = timestampToSeconds(t1);
        long sec2 = timestampToSeconds(t2);
        return sec2 - sec1;
    }

    public static long timestampToSeconds(String timestamp) {
        int year = parsePositiveInt(timestamp.substring(0, 4));
        int month = parsePositiveInt(timestamp.substring(5, 7));
        int day = parsePositiveInt(timestamp.substring(8, 10));
        int hour = parsePositiveInt(timestamp.substring(11, 13));
        int min = parsePositiveInt(timestamp.substring(14, 16));
        int sec = parsePositiveInt(timestamp.substring(17, 19));

        long totalDays = (year - 2000) * 365L + (month * 30L) + day;
        return totalDays * 86400L + hour * 3600L + min * 60L + sec;
    }

    public static incident[] resizeIncident(incident[] old) {
        incident[] newIncident = new incident[old.length * 2];
        for (int i = 0; i < old.length; i++) {
            newIncident[i] = old[i];
        }
        return newIncident;
    }

    public static void detectIncident(LogEntry[] log, int logCount, PrintWriter writer) {
        incident[] incidents = new incident[5];
        int incidentCount = 0;

        String[] processedString = new String[5];
        int processedCount = 0;

        for (int i = 0; i < logCount; i++) {
            String service = log[i].getService();

            boolean alreadyProcessed = false;
            for (int p = 0; p < processedCount; p++) {
                if (processedString[p].equals(service)) {
                    alreadyProcessed = true;
                    break;
                }
            }
            if (alreadyProcessed) continue;

            if (processedCount == processedString.length) {
                processedString = resizeString(processedString);
            }
            processedString[processedCount++] = service;

            String firstGroupTime = null;
            String lastGroupTime = null;
            int groupCount = 0;

            for (int j = 0; j < logCount; j++) {
                LogEntry entry = log[j];
                if (entry.getService().equals(service) && entry.getLevel().equals("ERROR")) {
                    String currentTime = entry.getTimeStamp();
                    if (firstGroupTime == null) {
                        firstGroupTime = currentTime;
                        lastGroupTime = currentTime;
                        groupCount = 1;
                    } else {
                        long diff = getTimeDifference(lastGroupTime, currentTime);
                        if (diff <= 60) {
                            lastGroupTime = currentTime;
                            groupCount++;
                        } else {
                            if (groupCount >= 3) {
                                if (incidentCount == incidents.length) {
                                    incidents = resizeIncident(incidents);
                                }
                                incidents[incidentCount++] = new incident(service, firstGroupTime, lastGroupTime, groupCount);
                            }
                            firstGroupTime = currentTime;
                            lastGroupTime = currentTime;
                            groupCount = 1;
                        }
                    }
                }
            }

            if (firstGroupTime != null && groupCount >= 3) {
                if (incidentCount == incidents.length) {
                    incidents = resizeIncident(incidents);
                }
                incidents[incidentCount++] = new incident(service, firstGroupTime, lastGroupTime, groupCount);
            }
        }

        if (incidentCount == 0) {
            writer.println("No incidents detected.");
        } else {
            for (int i = 0; i < incidentCount; i++) {
                incidents[i].displayIncident(i + 1, writer);
            }
        }
    }

    public static void recordRequestLog(RequestStats req, String service, String level) {
        req.recordCount++;
        if (level.equals("ERROR")) {
            req.errorCount++;
        }
        boolean isoccur = false;
        for (int i = 0; i < req.serviceCount; i++) {
            if (req.services[i].equals(service)) {
                isoccur = true;
                break;
            }
        }
        if (!isoccur) {
            if (req.serviceCount == req.services.length) {
                req.services = resizeString(req.services);
            }
            req.services[req.serviceCount] = service;
            req.serviceCount++;
        }
    }

    public static String[] resizeString(String[] old) {
        String[] newString = new String[old.length * 2];
        for (int i = 0; i < old.length; i++) {
            newString[i] = old[i];
        }
        return newString;
    }

    public static int parsePositiveInt(String str) {
        int num = 0;
        for (int i = 0; i < str.length(); i++) {
            num = num * 10 + (str.charAt(i) - '0');
        }
        return num;
    }

    public static String getTimestamp(String Line) {
        String time = "";
        for (int i = 0; i < Line.length(); i++) {
            if (Line.charAt(i) == '|') {
                break;
            }
            time += Line.charAt(i);
        }
        return time;
    }

    public static String getRequestId(String Line) {
        String req = "";
        int pipeCount = 0;
        for (int i = 0; i < Line.length(); i++) {
            if (Line.charAt(i) == '|') {
                pipeCount++;
                continue;
            }
            if (pipeCount == 3) {
                req += Line.charAt(i);
            } else if (pipeCount == 4) {
                break;
            }
        }
        return req;
    }

    public static String getField(String Line, int target) {
        int pipeCount = 0;
        String result = "";
        for (int i = 0; i < Line.length(); i++) {
            char c = Line.charAt(i);
            if (c == '|') {
                if (pipeCount == target) {
                    return result;
                }
                pipeCount++;
                result = "";
            } else {
                result += c;
            }
        }
        if (pipeCount == target) {
            return result;
        }
        return "";
    }
}