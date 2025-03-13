package com.atguigu.thread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Properties;

public class Download {
    private final JFrame f;
    public static JProgressBar pb;
    public static JTextField tfMessage, tfURL, tfName, tfNumThread;
    private final JButton[] btn;
    private FileDialog save, conDownload;//选择保存位置位置，选择继续下载的文件
    private ArrayList<Task> tasks;
    private boolean isPart = false;
    private long[] startParts;


    public Download() {
        f = new JFrame("多线程下载器");

        save = new FileDialog(f, "选择保存位置", FileDialog.SAVE);
        conDownload = new FileDialog(f, "选择继续下载的配置文件", FileDialog.LOAD);

        pb = new JProgressBar();
        pb = new JProgressBar(0, 100);// 设置进度最小最大值
        pb.setValue(0); // 当前值
        pb.setStringPainted(true);// 绘制百分比文本（进度条中间显示的百分数）
        pb.setIndeterminate(false);
        pb.setPreferredSize(new Dimension(500, 30));

        tfMessage = new JTextField();
        tfMessage.setFocusable(false);
        tfMessage.setPreferredSize(new Dimension(500, 30));
        Box messageBox = Box.createHorizontalBox();
        messageBox.add(new Label("下载进度："));
        messageBox.add(tfMessage);

        tfURL = new JTextField();
        tfURL.setPreferredSize(new Dimension(500, 30));
        Box dirBox = Box.createHorizontalBox();
        dirBox.add(new Label("文件URL："));
        dirBox.add(tfURL);

        tfName = new JTextField();
        tfName.setPreferredSize(new Dimension(500, 30));
        Box nameBox = Box.createHorizontalBox();
        nameBox.add(new Label("保存地址："));
        nameBox.add(tfName);

        tfNumThread = new JTextField();
        tfNumThread.setPreferredSize(new Dimension(500, 30));
        tfNumThread.setText("2");
        Box numThreadBox = Box.createHorizontalBox();
        numThreadBox.add(new Label("线程数量："));
        numThreadBox.add(tfNumThread);

        Box vBox = Box.createVerticalBox();
        vBox.add(Box.createVerticalStrut(10));
        vBox.add(pb);
        vBox.add(Box.createVerticalStrut(20));
        vBox.add(messageBox);
        vBox.add(Box.createVerticalStrut(20));
        vBox.add(dirBox);
        vBox.add(Box.createVerticalStrut(20));
        vBox.add(nameBox);
        vBox.add(Box.createVerticalStrut(20));
        vBox.add(numThreadBox);
        vBox.add(Box.createVerticalStrut(20));

        btn = new JButton[4];
        btn[0] = new JButton("新建下载");
        btn[1] = new JButton("继续下载");
        btn[2] = new JButton("开始");

        Box btnHBox = Box.createHorizontalBox();
        btnHBox.add(btn[0]);
        btnHBox.add(Box.createHorizontalStrut(20));
        btnHBox.add(btn[1]);
        btnHBox.add(Box.createHorizontalStrut(20));
        btnHBox.add(btn[2]);

        vBox.add(btnHBox);

        JPanel jPanel = new JPanel();
        jPanel.add(vBox);

        f.add(jPanel);

        f.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                for (Task task : tasks) {
                    task.dialog.ifRunningDeleteFile();
                }

                System.exit(0);
            }
        });

        f.pack();
        f.setSize(700, 370);
        f.setLocation(700, 350);
        f.setVisible(true);
        f.setResizable(false);

        tasks = new ArrayList<>();

        //新建下载
        btn[0].addActionListener(e -> {
            save.setVisible(true);
            String file = save.getFile();
            String directory = save.getDirectory();
            if (file != null && file.length() > 0 && directory != null && directory.length() > 0) {
                tfName.setText(directory + file);
                isPart = false;
                startParts = null;
            }
        });

        //继续下载
        btn[1].addActionListener(e -> {
            conDownload.setVisible(true);
            String directory = conDownload.getDirectory();
            String file = conDownload.getFile();
            if (file != null && file.length() > 0 && directory != null && directory.length() > 0){
                String propPath = directory + file;
                if(!continueTask(propPath)){
                    JOptionPane.showMessageDialog(f, "配置文件错误!", "下载失败", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            isPart = true;
        });

        //开始
        btn[2].addActionListener(e -> {
            String saveFilePath = tfName.getText().trim();
            String saveFileName = saveFilePath.substring(saveFilePath.lastIndexOf('\\') + 1);
            String url = tfURL.getText().trim();
            String numThreadStr = tfNumThread.getText();
            int numThread = 0;
            if((numThread = checkStartBtn(url,saveFilePath,numThreadStr)) == -1)
                return;
            DownLoadDialog dialog;
            if(!isPart){
                dialog = new DownLoadDialog(f, saveFileName,url,saveFilePath,numThread,false,null,tasks);
            }else {
                dialog = new DownLoadDialog(f,saveFileName,url,saveFilePath,numThread,true,startParts,tasks);
            }
            dialog.setVisible(true);
            new ThrCountAll(tasks).start();
            clear();
        });
    }

    private boolean continueTask(String propPath){
        if(!propPath.endsWith(".cfg")){
            return false;
        }
        try {
            Properties prop = new Properties();
            prop.load(new FileReader(propPath));
            String urlName = prop.getProperty("urlName");
            String saveName = prop.getProperty("saveName");
            String numThread = prop.getProperty("numThread");
            String[] starts = prop.getProperty("starts").split("#");
            startParts = new long[starts.length];
            for(int i = 0;i<starts.length;i++){
                startParts[i] = Long.parseLong(starts[i]);
            }

            tfURL.setText(urlName);
            tfName.setText(saveName);
            tfNumThread.setText(numThread);
        }catch (Exception e){
            return false;
        }

        return true;
    }
    private int checkStartBtn(String url,String saveFilePath,String numThreadStr){
        int numThread;
        if (url.equals("")) {
            JOptionPane.showMessageDialog(f, "URL不能为空!", "输入错误", JOptionPane.ERROR_MESSAGE);
            return -1;
        } else if (saveFilePath.equals("")) {
            JOptionPane.showMessageDialog(f, "文件路径不能为空!", "输入错误", JOptionPane.ERROR_MESSAGE);
            return -1;
        } else {
            try {
                numThread = Integer.parseInt(numThreadStr);
                if (numThread <= 0 || numThread >10) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(f, "线程数量应为正整数且小于10!", "输入错误", JOptionPane.ERROR_MESSAGE);
                return -1;
            }
        }

        return numThread;
    }

    private void clear(){
        tfName.setText("");
        tfURL.setText("");
        tfNumThread.setText("2");
        isPart = false;
        startParts = null;
    }

    public static void main(String[] args) {
        new Download();
    }
}
