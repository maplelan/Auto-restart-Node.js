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
            System.err.println("USAGE: java Auto_restart [.js檔案位置] [偵測間隔時間(秒)]");
            System.exit(1);
        }
        patch = args[0];
        if(args[1] != "") {
            timer = Integer.parseInt(args[1]) * 1000;
        }
        if(patch.equals("")){
            System.err.println(".js檔案位置有錯誤");
            System.exit(1);
        }
        System.out.println(LocalDateTime.now() + "自動重啟系統啟動\n執行js位置為: " + patch + "\n偵測時間設定為" + timer/1000 + "秒");
        while (true) {
            try {
                List<String> beforepid = getPIDListByPidName("node");
                System.out.println(LocalDateTime.now() + " 啟動前\"node\"的PID列表:" + beforepid);
                System.out.println(LocalDateTime.now() + " 啟動 \"" + patch + "\"");
                Runtime.getRuntime().exec("cmd /c start node " + patch);
                List<String> afterpid = getPIDListByPidName("node");
                System.out.println(LocalDateTime.now() + " 啟動後\"node\"的PID列表:" + afterpid);
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
                    System.out.println(LocalDateTime.now() + " 根據比對結果推測出PID可能是" + monipid);
                }else {
                    System.err.println(LocalDateTime.now() + "*無法成功比對,為避免死循環將關閉自動重啟系統");
                    System.exit(0);
                }
                if(!isExitPid(monipid)){
                    System.err.println(LocalDateTime.now() + "*初次偵測就找不到PID為" + monipid + "的\"node\"進程,為避免死循環將關閉自動重啟系統");
                    System.exit(0);
                }
                boolean monitor = true;
                while (monitor){
                    if(isExitPid(monipid)){
                        System.out.println(LocalDateTime.now() + " 有偵測到PID為" + monipid + "的\"node\"進程");
                        Thread.sleep(timer);
                    }else {
                        System.err.println(LocalDateTime.now() + "*找不到PID為" + monipid + "的\"node\"進程 ,將於10秒後試著重啟");
                        monitor = false;
                        Thread.sleep(10000);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
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
            if(Objects.equals(pid, l)){
                life = true;
            }
        }
        return  life;
    }
}
