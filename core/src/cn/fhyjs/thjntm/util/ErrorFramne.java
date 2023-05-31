/*
 * Created by JFormDesigner on Wed May 31 10:00:45 CST 2023
 */

package cn.fhyjs.thjntm.util;

import com.badlogic.gdx.Gdx;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;
import javax.swing.*;

/**
 * @author administer
 */
public class ErrorFramne extends JFrame {
    public ErrorFramne() {
        initComponents();
    }
    public ErrorFramne(Thread t,Throwable e) {
        initComponents();
        textArea1.setText(Trace.getStackTraceAsString(e));
        textPane1.setText(t.toString());
    }

    private void copy(ActionEvent e) {
        StringSelection stringSelection = new StringSelection(textArea1.getText());
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private void PTC(ActionEvent e) {
        System.err.println(textArea1.getText());
    }

    private void Exit(ActionEvent e) {
        System.exit(-1);
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
        scrollPane1 = new JScrollPane();
        textArea1 = new JTextArea();
        scrollPane2 = new JScrollPane();
        textPane1 = new JTextPane();
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();

        //======== this ========
        setTitle("ERROR");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== scrollPane1 ========
        {

            //---- textArea1 ----
            textArea1.setEditable(false);
            scrollPane1.setViewportView(textArea1);
        }
        contentPane.add(scrollPane1);
        scrollPane1.setBounds(0, 25, 600, 400);

        //======== scrollPane2 ========
        {

            //---- textPane1 ----
            textPane1.setEditable(false);
            scrollPane2.setViewportView(textPane1);
        }
        contentPane.add(scrollPane2);
        scrollPane2.setBounds(0, 0, 600, scrollPane2.getPreferredSize().height);

        //---- button1 ----
        button1.setText("Copy");
        button1.setFont(button1.getFont().deriveFont(button1.getFont().getSize() + 4f));
        button1.addActionListener(e -> copy(e));
        contentPane.add(button1);
        button1.setBounds(0, 425, 130, button1.getPreferredSize().height);

        //---- button2 ----
        button2.setText("Exit");
        button2.setFont(button2.getFont().deriveFont(button2.getFont().getSize() + 5f));
        button2.setActionCommand("Exit");
        button2.addActionListener(e -> Exit(e));
        contentPane.add(button2);
        button2.setBounds(475, 425, 125, button2.getPreferredSize().height);

        //---- button3 ----
        button3.setText("Print To Console");
        button3.setFont(button3.getFont().deriveFont(button3.getFont().getSize() + 4f));
        button3.addActionListener(e -> PTC(e));
        contentPane.add(button3);
        button3.setBounds(135, 425, 200, 30);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        pack();
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
    private JScrollPane scrollPane1;
    private JTextArea textArea1;
    private JScrollPane scrollPane2;
    private JTextPane textPane1;
    private JButton button1;
    private JButton button2;
    private JButton button3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
