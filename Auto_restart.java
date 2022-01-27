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
            String patch = "D:\\MapleLan���\\Code\\Discode_robot\\Yacht_Dice\\Yacht_Dice.js";
            int timer = 60 * 1000, cutlog = 8 * 3600;
            boolean log = true, fst = true;
            LocalDateTime startLDT = LocalDateTime.now();
            File file = new File(".\\logs\\" + logtime(startLDT) + "_log(Start).txt");
            if (args.length < 1){
                System.out.println("USAGE: java Auto_restart [.js�ɮצ�m] [�������j�ɶ�(��)] [�O�_�s�xlog] [log�̪��ɶ��_�I(�p��)]");
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
                System.out.println(".js�ɮצ�m�����~");
                System.exit(1);
            }
            logprintln(LocalDateTime.now() + "�۰ʭ��Ҩt�αҰ�\n����js��m��: " + patch + "\n�����ɶ��]�w��" + timer/1000 + "��", log, file);
            logprintln(log ? ("�|��Xlog,�C�j" + cutlog/3600 + "�p�ɰ��@������") : "����Xlog", log, file);
            while (true) {
                startLDT = LocalDateTime.now();
                if(log){
                    if(fst){
                        fst = false;
                    }else{
                        file = new File(".\\logs\\" + logtime(startLDT) +"_log(Crash).txt");
                        log(LocalDateTime.now() + " ���s�Ұ�\"" + patch + "\"", file);
                    }
                }
                List<String> beforepid = getPIDListByPidName("node");
                logprintln(LocalDateTime.now() + " �Ұʫe�W�٬�\"node\"��PID�C��:" + beforepid, log, file);
                logprintln(LocalDateTime.now() + " �Ұ�\"" + patch + "\"", log, file);
                Runtime.getRuntime().exec("cmd /c start node " + patch);
                List<String> afterpid = getPIDListByPidName("node");
                logprintln(LocalDateTime.now() + " �Ұʫ�W�٬�\"node\"��PID�C��:" + afterpid, log, file);
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
                    logprintln(LocalDateTime.now() + " �ھڤ�ﵲ�G�����XPID�i��O" + monipid, log, file);
                }else {
                    logprintln(LocalDateTime.now() + "*�L�k���\���,���קK���`���N�����۰ʭ��Ҩt��", log, file);
                    System.exit(0);
                }
                if(!isExitPid(monipid)){
                    logprintln(LocalDateTime.now() + "*�즸�����N�䤣��PID��" + monipid + "��node�i�{,���קK���`���N�����۰ʭ��Ҩt��", log, file);
                    System.exit(0);
                }
                boolean monitor = true;
                while (monitor){
                    if(isExitPid(monipid)){
                        logprintln(LocalDateTime.now() + " ��������PID��" + monipid + "��node�i�{", log, file);
                        if(log) {
                            if (Timebetween(startLDT) >= cutlog) {
                                log(LocalDateTime.now() + " �w�g�L" + cutlog / 3600 + "�p��,log���q", file);
                                startLDT = LocalDateTime.now();
                                file = new File(".\\logs\\" + logtime(startLDT) + "_log(Cut).txt");
                                log(LocalDateTime.now() + " �w�g�L" + cutlog / 3600 + "�p��,log���q", file);
                            }
                        }
                        Thread.sleep(timer);
                    }else {
                        logprintln(LocalDateTime.now() + "*�䤣��PID��" + monipid + "��node�i�{ ,�N��10���յۭ���", log, file);
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
    //log�O�_��X
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
    //�¿�Xlog
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
    // �ھ�PidName�����e��Pid��list���X
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
            throw new Exception("����{��ID�X���I");
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
    //�P�_pid�O�_�h�X
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
    //�Z���{�b�X��
    public static int Timebetween(LocalDateTime LDT){
        ZoneOffset zone8hr = ZoneOffset.ofHours(8);
        Instant instant = LDT.toInstant(zone8hr);
        Instant nowInstant = LocalDateTime.now().toInstant(zone8hr);
        Duration duration = Duration.between(instant, nowInstant);
        return (int)Math.floor(duration.getSeconds());
    }
    //��Xlog�M�ήɶ�
    public static String logtime(LocalDateTime LDT){
        return LDT.getYear() + "-" + String.format("%02d", LDT.getMonthValue()) + "-" + String.format("%02d", LDT.getDayOfMonth()) + "_T_" + String.format("%02d", LDT.getHour()) + "-" + String.format("%02d", LDT.getMinute()) + "-" + String.format("%02d", LDT.getSecond()) + "--" + String.format("%03d", (LDT.getNano()/1000000));
    }
}
