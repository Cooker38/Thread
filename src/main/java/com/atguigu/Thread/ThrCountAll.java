package com.atguigu.Thread;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//统计总下载完成情况的线程
public class ThrCountAll extends Thread {
    private ArrayList<Task> tasks = null;
    private boolean running = true;
    private double beginTime;

    public ThrCountAll(ArrayList<Task> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void run() {
        beginTime = (new Date()).getTime() / 1000.0;
        DecimalFormat df = new DecimalFormat("#.##");
        while (running) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            long allFileLen = 0;
            long allSpeed = 0;
            long allFinish = 0;
            for (Task task : tasks) {
                if (task.running) {
                    allFileLen += task.fileLen;
                    if(task.thrCount != null){
                        allSpeed += task.thrCount.getSpeed();
                        allFinish += task.thrCount.getNowFinish();
                    }

                }
            }

            if(allFileLen == 0 || allSpeed == 0){
                Download.pb.setValue(100);
                Download.tfMessage.setText("下载完成!");
                beginTime = (new Date()).getTime() / 1000.0;
                continue;
            }
            //文件大小
            String allFileLenStr = formatLen(allFileLen, df);
            //已经下载的大小
            String allFinishStr = formatLen(allFinish, df);
            //下载比例
            int percent = (int) (allFinish * 100 / allFileLen);
            //下载速度
            String allSpeedStr = formatSpeed(allSpeed, df);
            //已经下载的时间
            double currentTime = (new Date()).getTime() / 1000.0;
            String spendTimeStr = formatTime(currentTime - beginTime, df);
            //剩余时间
            String timeStr = formatTime((allFileLen - allFinish) * 1.0 / allSpeed, df);

            Download.pb.setValue(percent);
            Download.tfMessage.setText("已下载:" + allFinishStr + "/" + allFileLenStr + "   " +
                    "下载速度:" + allSpeedStr + "   " + "耗时:" + spendTimeStr + "   " + "剩余时间:" + timeStr);

        }
    }

    private String formatLen(long len, DecimalFormat df) {
        if (len < 1024)
            return df.format(len * 1.0) + "B";
        else if (len < 1024 * 1024)
            return df.format(len * 1.0 / 1024) + "KB";
        else if (len < 1024 * 1024 * 1024)
            return df.format(len * 1.0 / 1024 / 1024) + "MB";
        else
            return df.format(len * 1.0 / 1024 / 1024 / 1024) + "GB";
    }

    private String formatSpeed(long len, DecimalFormat df) {
        if (len < 1024)
            return df.format(len * 1.0) + "B/s";
        else if (len < 1024 * 1024)
            return df.format(len * 1.0 / 1024) + "KB/s";
        else if (len < 1024 * 1024 * 1024)
            return df.format(len * 1.0 / 1024 / 1024) + "MB/s";
        else
            return df.format(len * 1.0 / 1024 / 1024 / 1024) + "GB/s";
    }

    private String formatTime(double time, DecimalFormat df) {
        if (time < 60)
            return df.format(time) + "s";
        if (time / 60 < 60)
            return df.format(time / 60) + "m";
        return df.format(time / 60 / 60) + "h";
    }

    public void stopThread() {
        running = false;
    }
}
