import java.io.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Auto_restart {
    public static void main(String[] args) {
        try {
            String patch = "D:\\MapleLan資料\\Code\\Discode_robot\\Yacht_Dice\\Yacht_Dice.js";
            int timer = 60 * 1000, cutlog = 8 * 3600;
            boolean log = true, fst = true;
            LocalDateTime startLDT = LocalDateTime.now();
            File file = new File(".\\logs\\" + logtime(startLDT) + "_log(Start).txt");
            if (args.length < 1){
                System.out.println("USAGE: java Auto_restart [.js檔案位置] [偵測間隔時間(秒)] [是否存儲log] [log最長時間斷點(小時)]");
                System.exit(1);
            }
            patch = args[0];
            if(args.length > 1) {
                if (!Objects.equals(args[1], "")) {
                    timer = Integer.parseInt(args[1]) * 1000;
                }
            }
            if(args.length > 2) {
                if (Objects.equals(args[2], "false")) {
                    log = false;
                }
            }
            if(args.length > 3) {
                if (!Objects.equals(args[3], "")) {
                    cutlog = Integer.parseInt(args[3]) * 3600;
                    if(cutlog == 0){
                        cutlog = 3600;
                    }
                }
            }
            if(patch.equals("")){
                System.out.println(".js檔案位置有錯誤");
                System.exit(1);
            }
            logprintln(LocalDateTime.now() + "自動重啟系統啟動\n執行js位置為: " + patch + "\n偵測時間設定為" + timer/1000 + "秒", log, file);
            logprintln(log ? ("會輸出log,每隔" + cutlog/3600 + "小時做一次分割") : "不輸出log", log, file);
            while (true) {
                startLDT = LocalDateTime.now();
                if(log){
                    if(fst){
                        fst = false;
                    }else{
                        file = new File(".\\logs\\" + logtime(startLDT) +"_log(Crash).txt");
                        log(LocalDateTime.now() + " 重新啟動\"" + patch + "\"", file);
                    }
                }
                List<String> beforepid = getPIDListByPidName("node");
                logprintln(LocalDateTime.now() + " 啟動前名稱為\"node\"的PID列表:" + beforepid, log, file);
                logprintln(LocalDateTime.now() + " 啟動\"" + patch + "\"", log, file);
                Runtime.getRuntime().exec("cmd /c start node " + patch);
                List<String> afterpid = getPIDListByPidName("node");
                logprintln(LocalDateTime.now() + " 啟動後名稱為\"node\"的PID列表:" + afterpid, log, file);
                String monipid = "error";
                if((beforepid.size() == 0) && (afterpid.size() == 1)){
                    monipid = afterpid.get(0);
                }else {
                    for (String after : afterpid) {
                        for (String before : beforepid) {
                            if (!Objects.equals(after, before)) {
                                monipid = after;
                                break;
                            }
                        }
                    }
                }
                if(!Objects.equals(monipid, "error")) {
                    logprintln(LocalDateTime.now() + " 根據比對結果推測出PID可能是" + monipid, log, file);
                }else {
                    logprintln(LocalDateTime.now() + "*無法成功比對,為避免死循環將關閉自動重啟系統", log, file);
                    System.exit(0);
                }
                if(!isExitPid(monipid)){
                    logprintln(LocalDateTime.now() + "*初次偵測就找不到PID為" + monipid + "的node進程,為避免死循環將關閉自動重啟系統", log, file);
                    System.exit(0);
                }
                boolean monitor = true;
                while (monitor){
                    if(isExitPid(monipid)){
                        logprintln(LocalDateTime.now() + " 有偵測到PID為" + monipid + "的node進程", log, file);
                        if(log) {
                            if (Timebetween(startLDT) >= cutlog) {
                                log(LocalDateTime.now() + " 已經過" + cutlog / 3600 + "小時,log分段", file);
                                startLDT = LocalDateTime.now();
                                file = new File(".\\logs\\" + logtime(startLDT) + "_log(Cut).txt");
                                log(LocalDateTime.now() + " 已經過" + cutlog / 3600 + "小時,log分段", file);
                            }
                        }
                        Thread.sleep(timer);
                    }else {
                        logprintln(LocalDateTime.now() + "*找不到PID為" + monipid + "的node進程 ,將於10秒後試著重啟", log, file);
                        monitor = false;
                        Thread.sleep(10000);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
    //log是否輸出
    public static void logprintln(String s, boolean log, File f){
        try {
            if(log){
                if(!f.getParentFile().exists()){
                    f.getParentFile().mkdirs();
                }
                BufferedWriter out = new BufferedWriter(new FileWriter(f,true));
                out.write(s + "\r\n");
                out.flush();
                out.close();
                System.out.println(s);
            }else{
                System.out.println(s);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    //純輸出log
    public static void log(String s, File f){
        try {
            if(!f.getParentFile().exists()){
                f.getParentFile().mkdirs();
            }
            BufferedWriter out = new BufferedWriter(new FileWriter(f,true));
            out.write(s + "\r\n");
            out.flush();
            out.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    // 根據PidName獲取當前的Pid的list集合
    public static List<String> getPIDListByPidName(String pidName) throws Exception {
        List<String> pidList = new ArrayList<>();
        InputStream is = null;
        InputStreamReader ir = null;
        BufferedReader br = null;
        String line = null;
        String[] array = (String[]) null;
        try {
            String imageName = pidName + ".exe";
            Process p = Runtime.getRuntime().exec("TASKLIST /NH /FO CSV /FI \"IMAGENAME EQ " + imageName + "\"");
            is = p.getInputStream();
            ir = new InputStreamReader(is);
            br = new BufferedReader(ir);
            while ((line = br.readLine()) != null) {
                if (line.contains(imageName)) {
                    array = line.split(",");
                    line = array[1].replaceAll("\"", "");
                    pidList.add(line);
                }
            }
        } catch (IOException localIOException) {
            throw new Exception("獲取程序ID出錯！");
        } finally {
            if (br != null) {
                br.close();
            }
            if (ir != null) {
                ir.close();
            }
            if (is != null) {
                is.close();
            }
        }
        return pidList;
    }
    //判斷pid是否退出
    public static boolean isExitPid(String pid) throws Exception {
        List<String> list = getPIDListByPidName("node");
        boolean life = false;
        for (String l : list){
            if (Objects.equals(pid, l)) {
                life = true;
                break;
            }
        }
        return  life;
    }
    //距離現在幾秒
    public static int Timebetween(LocalDateTime LDT){
        ZoneOffset zone8hr = ZoneOffset.ofHours(8);
        Instant instant = LDT.toInstant(zone8hr);
        Instant nowInstant = LocalDateTime.now().toInstant(zone8hr);
        Duration duration = Duration.between(instant, nowInstant);
        return (int)Math.floor(duration.getSeconds());
    }
    //輸出log專用時間
    public static String logtime(LocalDateTime LDT){
        return LDT.getYear() + "-" + String.format("%02d", LDT.getMonthValue()) + "-" + String.format("%02d", LDT.getDayOfMonth()) + "_T_" + String.format("%02d", LDT.getHour()) + "-" + String.format("%02d", LDT.getMinute()) + "-" + String.format("%02d", LDT.getSecond()) + "--" + String.format("%03d", (LDT.getNano()/1000000));
    }
}
