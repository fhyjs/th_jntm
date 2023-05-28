package cn.fhyjs.thjntm.util;

import javax.swing.*;
import java.awt.*;

public class ProgressBar extends Thread{
    public JFrame jf;
    JProgressBar bar = new JProgressBar(JProgressBar.HORIZONTAL,0,100);
    public JTextField txt1 = new JTextField();
    public int progress = 0;
    public ProgressBar(String windowTitle,String txt){
        super();
        jf = new JFrame(windowTitle);
        txt1.setText(txt);
    }
    @Override
    public void run() {
        //TODO 创建进度条
        jf.setPreferredSize(new Dimension(250,100));
        //设置进度条的属性
        bar.setStringPainted(true);
        bar.setBorderPainted(true);
        txt1.setPreferredSize(new Dimension(220,20));
        bar.setPreferredSize(new Dimension(200, 20));
        txt1.setEditable(false);
        jf.setLayout(new FlowLayout());
        jf.setAlwaysOnTop(true); //窗体置顶
        jf.add(txt1);
        jf.setUndecorated(true);
        jf.add(bar);
        jf.setLocationRelativeTo(null);// 使窗口显示在屏幕中央
        jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        jf.pack();
        jf.setVisible(true);
        while (true) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            bar.setValue((int) (progress));
            if (progress >= 100) {
                break;
            }
        }
        jf.dispose();
    }
}
