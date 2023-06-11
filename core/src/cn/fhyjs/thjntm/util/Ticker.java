package cn.fhyjs.thjntm.util;

import cn.fhyjs.thjntm.Config;
import cn.fhyjs.thjntm.ThGame;
import cn.fhyjs.thjntm.interfaces.ITickable;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
public class Ticker {
    public static List<ITickable> l=new ArrayList<>();
    private static Timer timer;
    private static long pauseTime;
    private static long remainingTime;

    public static void AddTickAble(ITickable tickable){
        l.add(tickable);
    }
    public Ticker(){
        if (timer!=null)
            timer.cancel();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TickTask(), 0, 1000/Config.FPS);
    }
    public static void pauseTimer() {
        timer.cancel(); // 取消任务
        pauseTime = System.currentTimeMillis(); // 记录暂停时间
    }

    public static void resumeTimer() {
        long elapsedTime = System.currentTimeMillis() - pauseTime; // 计算暂停的时间段

        // 恢复剩余时间
        remainingTime = Math.max(0, remainingTime - elapsedTime);
        timer = new Timer();
        timer.scheduleAtFixedRate(new TickTask(), remainingTime, 1000/Config.FPS);
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
