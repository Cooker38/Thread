package com.atguigu.thread;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Properties;

public class DownLoadDialog extends JDialog {
    public JProgressBar pb;
    public JTextField tfMessage, tfURL, tfSavePath, tfNumThread;
    public JButton btnContinue, btnPause;
    private final Task task;
    private boolean isPart;
    private long[] startParts;
    private final String saveFilePath;
    private final String urlName;
    private final int numThread;


    public DownLoadDialog(JFrame f, String saveFileName, String urlName, String saveFilePath,
                          int numThread, boolean isPart, long[] startParts, ArrayList<Task> tasks) {
        super(f, "下载任务(" + saveFileName + ")", false);
        setBounds(600, 260, 700, 370); // 设置对话框位置和大小
        setResizable(false);

        this.urlName = urlName;
        this.saveFilePath = saveFilePath;
        this.numThread = numThread;
        this.isPart = isPart;
        this.startParts = startParts;
        this.task = new Task(saveFileName, this);
        tasks.add(task);

        initView();

        begin();

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ifRunningDeleteFile();
                dispose();
            }
        });

        btnPause.addActionListener(e -> {
            if (!pauseTask(task)) {
                JOptionPane.showMessageDialog(this, "暂停失败!", "下载错误", JOptionPane.ERROR_MESSAGE);
            } else {
                btnPause.setEnabled(false);
                btnContinue.setEnabled(true);
            }
        });

        btnContinue.addActionListener(e -> {
            if (beginContinue()) {
                btnPause.setEnabled(true);
                btnContinue.setEnabled(false);
            }
        });
    }

    private void initView(){
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

        tfSavePath = new JTextField();
        tfSavePath.setPreferredSize(new Dimension(500, 30));
        Box nameBox = Box.createHorizontalBox();
        nameBox.add(new Label("保存地址："));
        nameBox.add(tfSavePath);

        tfNumThread = new JTextField();
        tfNumThread.setPreferredSize(new Dimension(500, 30));
        tfNumThread.setText("5");
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

        btnPause = new JButton("暂停");
        btnContinue = new JButton("继续");
        Box btnHBox = Box.createHorizontalBox();
        btnHBox.add(btnPause);
        btnHBox.add(Box.createHorizontalStrut(20));
        btnHBox.add(btnContinue);
        vBox.add(btnHBox);

        JPanel jPanel = new JPanel();
        jPanel.add(vBox);
        add(jPanel);

        tfURL.setText(urlName);
        tfSavePath.setText(saveFilePath);
        tfNumThread.setText(numThread + "");
        tfMessage.setFocusable(false);
        tfURL.setEnabled(false);
        tfSavePath.setEnabled(false);
        tfNumThread.setEnabled(false);

        btnContinue.setEnabled(false);
    }

    private void begin() {
        if (!isPart) {
            beginNew();
        } else {
            beginContinue();
        }
    }

    private void beginNew() {
        if (!isPart && task.init(urlName, saveFilePath, numThread, false)) {
            for (int i = 0; i < task.numThread; i++) {
                task.thrDownloads[i].start();
            }
            task.thrCount.start();
            task.running = true;
        } else {
            JOptionPane.showMessageDialog(this, "下载失败!", "下载错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean beginContinue() {
        if (isPart && task.initContinue(urlName, saveFilePath, numThread, startParts, true)) {
            task.isPart = true;
            this.isPart = true;
            for (int i = 0; i < task.numThread; i++) {
                task.thrDownloads[i].start();
            }
            task.thrCount.start();
            task.running = true;
            return true;
        } else {
            JOptionPane.showMessageDialog(this, "继续下载失败!", "下载错误", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private boolean pauseTask(Task task) {
        ThrDownload[] thrDownloads = task.thrDownloads;
        int numThread = task.numThread;
        String urlName = task.urlName;
        String saveName = task.saveName;
        ThrCount thrCount = task.thrCount;
        int i;
        for (i = 0; i < numThread; i++) {
            if (thrDownloads[i].isAlive())
                break;
        }
        if (i != numThread) {
            try {
                //将线程停止
                for (int j = 0; j < numThread; j++) {
                    thrDownloads[j].stopThread();
                }
                thrCount.stopThread();
                Properties prop = new Properties();
                prop.setProperty("urlName", urlName);
                prop.setProperty("saveName", saveName);
                prop.setProperty("numThread", numThread + "");
                long tmp = thrDownloads[0].getFinish();
                String starts = tmp + "";
                this.startParts = new long[numThread];
                this.startParts[0] = tmp;
                for (int j = 1; j < numThread; j++) {
                    long temp = thrDownloads[j].getFinish();
                    this.startParts[j] = temp;
                    starts += "#" + temp;
                }
                prop.setProperty("starts", starts);
                File propFile = new File(saveName + ".cfg");
                prop.store(new FileWriter(propFile), "");
                task.running = false;
                task.isPart = true;
                this.isPart = true;
                task.startParts = this.startParts;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    public void ifRunningDeleteFile() {
        if (task.running) {
            for (ThrDownload thrDownload : task.thrDownloads) {
                thrDownload.stopThread();
            }
            task.running = false;
            task.thrCount.stopThread();
            for (ThrDownload thrDownload : task.thrDownloads) {
                try {
                    thrDownload.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            task.thrCount.deleteFile(task.saveName);
            task.thrCount.deleteFile(task.saveName + ".cfg");
        }
    }
}
