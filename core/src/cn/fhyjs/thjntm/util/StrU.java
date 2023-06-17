package cn.fhyjs.thjntm.util;

import java.util.Map;

public class StrU {
    public static boolean IsMapKeyHas(String has, Map<?,?> map){
        boolean r=false;
        for (Object o : map.keySet()) {
            if (String.valueOf(o).contains(has)) {
                r = true;
                break;
            }
        }
        return r;
    }
}
