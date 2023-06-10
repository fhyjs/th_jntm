package cn.fhyjs.thjntm.ctrls;

import cn.fhyjs.thjntm.resources.FileManager;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class CMain {
    Map<Integer,Map<String,String>> cmd=new HashMap<>();
    public CMain(String fn){
        try {
            String[] con = new String(Objects.requireNonNull(FileManager.getByteContent(fn)), StandardCharsets.UTF_8).replaceAll("\r","").split("\n");
            for (String s : con) {
                int tick = -1;
                Map<String,String> parm=new HashMap<>();
                String[] t = s.split(";");
                for (String s1 : t) {
                    String[] t1 = s1.split("=");
                    if (t1[0].equals("tick")){
                        tick= Integer.parseInt(t1[1]);
                    }
                    parm.put(t1[0],t1[1]);
                }
                if (tick==-1){
                    throw new RuntimeException("parm doesn't include \"tick\"");
                }
                cmd.put(tick,parm);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public boolean exec(int tick){
        if (!cmd.containsKey(tick)) return false;
        Map<String,String> parm=cmd.get(tick);
        return true;
    }
}
