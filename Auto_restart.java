import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;

public class Auto_restart {
    public static void main(String[] args) {
        if (args.length < 2){
            System.out.println("USAGE: java Auto_restart [.js檔案位置] [偵測間隔時間(秒)]");
            System.exit(1);
        }
        String patch = args[0];
        int timer = 10000;
        if(args[1] != "") {
            timer = Integer.parseInt(args[1]) * 1000;
        }
        while (true) {
            boolean runtime = isRunning("node.exe");
            System.out.println(LocalDateTime.now() + " : " + (runtime ? "已偵測到Node.exe運行" : "未偵測到Node.exe運行"));
            if (!runtime) {
                try {
                    System.out.println("啟動 " + patch);
                    Runtime.getRuntime().exec("cmd /c start node " + patch);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(timer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public static boolean isRunning(String exeName) {
        Process proc;
        try {
            proc = Runtime.getRuntime().exec("tasklist");
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String info = br.readLine();
            while (info != null) {
                //System.out.println(info);
                if (info.indexOf(exeName) >= 0) {
                    return true;
                }
                info = br.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println(false);
        return false;
    }
}
