import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Auto_restart {
    public static void main(String[] args) {
        String patch = "";
        int timer = 10000;
        if (args.length < 2){
            System.err.println("USAGE: java Auto_restart [.js�ɮצ�m] [�������j�ɶ�(��)]");
            System.exit(1);
        }
        patch = args[0];
        if(args[1] != "") {
            timer = Integer.parseInt(args[1]) * 1000;
        }
        if(patch.equals("")){
            System.err.println(".js�ɮצ�m�����~");
            System.exit(1);
        }
        System.out.println(LocalDateTime.now() + "�۰ʭ��Ҩt�αҰ�\n����js��m��: " + patch + "\n�����ɶ��]�w��" + timer/1000 + "��");
        while (true) {
            try {
                List<String> beforepid = getPIDListByPidName("node");
                System.out.println(LocalDateTime.now() + " �Ұʫe\"node\"��PID�C��:" + beforepid);
                System.out.println(LocalDateTime.now() + " �Ұ� \"" + patch + "\"");
                Runtime.getRuntime().exec("cmd /c start node " + patch);
                List<String> afterpid = getPIDListByPidName("node");
                System.out.println(LocalDateTime.now() + " �Ұʫ�\"node\"��PID�C��:" + afterpid);
                String monipid = "error";
                if((beforepid.size() == 0) && (afterpid.size() == 1)){
                    monipid = afterpid.get(0);
                }else {
                    for (String after : afterpid) {
                        for (String before : beforepid) {
                            if (!Objects.equals(after, before)) {
                                monipid = after;
                            }
                        }
                    }
                }
                if(!Objects.equals(monipid, "error")) {
                    System.out.println(LocalDateTime.now() + " �ھڤ�ﵲ�G�����XPID�i��O" + monipid);
                }else {
                    System.err.println(LocalDateTime.now() + "*�L�k���\���,���קK���`���N�����۰ʭ��Ҩt��");
                    System.exit(0);
                }
                if(!isExitPid(monipid)){
                    System.err.println(LocalDateTime.now() + "*�즸�����N�䤣��PID��" + monipid + "��\"node\"�i�{,���קK���`���N�����۰ʭ��Ҩt��");
                    System.exit(0);
                }
                boolean monitor = true;
                while (monitor){
                    if(isExitPid(monipid)){
                        System.out.println(LocalDateTime.now() + " ��������PID��" + monipid + "��\"node\"�i�{");
                        Thread.sleep(timer);
                    }else {
                        System.err.println(LocalDateTime.now() + "*�䤣��PID��" + monipid + "��\"node\"�i�{ ,�N��10���յۭ���");
                        monitor = false;
                        Thread.sleep(10000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            if(Objects.equals(pid, l)){
                life = true;
            }
        }
        return  life;
    }
}
