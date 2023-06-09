package cn.fhyjs.thjntm.util;

import cn.fhyjs.thjntm.Config;
import cn.fhyjs.thjntm.interfaces.ITickable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
public class Ticker {
    public static List<ITickable> l=new ArrayList<>();
    private static Timer timer=null;
    public void AddTickAble(ITickable tickable){
        l.add(tickable);
    }
    public Ticker(){
        if (timer!=null)
            timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TickTask(), 0, 1000/Config.FPS); // 每秒执行一次
    }

    private static class TickTask extends TimerTask {
        @Override
        public void run() {
            for (ITickable tickable : l){
                tickable.update();
            }
        }
    }
}
