package cn.fhyjs.thjntm.util;

import java.awt.*;

public class CUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        Frame frame = new ErrorFramne(t,e);
       // frame.setPreferredSize(new Dimension(600,500));
        frame.setAlwaysOnTop(true);
        frame.setVisible(true);
    }
}
