package com.atguigu.Thread;

import java.io.File;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

//统计下载任务的线程
public class ThrCount extends Thread {
    private double beginTime;
    private double currentTime;
    private Task task;
    private boolean running = true;
    private double speed = 0; //下载速度
    private long oldFinish, nowFinish;

    public ThrCount(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        beginTime = (new Date()).getTime() / 1000.0;
        oldFinish = 0;
        for (int i = 0; i < task.numThread; i++) {
            oldFinish += task.thrDownloads[i].getFinish();
        }
        while (running) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //nowFinish 已下载的大小 byte
            nowFinish = 0;
            for (int i = 0; i < task.numThread; i++) {
                nowFinish += task.thrDownloads[i].getFinish();
            }
            DecimalFormat df = new DecimalFormat("#.##");
            //完成百分比
            int percent = (int) (nowFinish * 100 / task.fileLen);
            //已经下载大小和总大小
            String nowFinishStr = df.format(nowFinish * 1.0 / 1024 / 1024) + "MB";
            String fileLenStr = df.format(task.fileLen * 1.0 / 1024 / 1024) + "MB";
            //下载速度 B/s
            speed = (nowFinish - oldFinish) * 1.0;
            String speedStr = speed > 1024 ? df.format(speed / 1024 / 1024) + "MB/s" : df.format(speed / 1024) + "KB/s";
            //已经下载的时间
            currentTime = (new Date()).getTime() / 1000.0;
            String spendTimeStr = formatTime(currentTime - beginTime, df);
            //剩余时间
            double time = (task.fileLen - nowFinish) * 1.0 / speed;
            String timeStr = formatTime(time, df);

            //绘制GUI
            task.dialog.pb.setValue(percent);
            task.dialog.tfMessage.setText("(" + task.taskName + ")已下载:" + nowFinishStr + "/" + fileLenStr + "   " +
                    "下载速度:" + speedStr + "   " + "耗时:" + spendTimeStr + "   " + "剩余时间:" + timeStr);

            int i;
            for (i = 0; i < task.numThread; i++) {
                if (task.thrDownloads[i].isAlive())
                    break;
            }
            if (i >= task.numThread) {  //全部线程下载完成
                for (i = 0; i < task.numThread; i++)
                    task.thrDownloads[i].stopThread();
                try {
                    deleteFile(task.saveName + ".cfg");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                task.running = false;

                task.dialog.pb.setValue(100);
                task.dialog.tfMessage.setText("(" + task.taskName + ")下载完成!");
                task.dialog.btnContinue.setEnabled(false);
                task.dialog.btnPause.setEnabled(false);
                break;
            }

            oldFinish = nowFinish;
        }
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
        try { //1.5s
            TimeUnit.MILLISECONDS.sleep(1500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        task.dialog.tfMessage.setText("(" + task.taskName + ")已暂停...");
    }

    public double getSpeed() {
        return speed;  // B/s
    }

    public long getNowFinish(){
        return nowFinish;
    }

    public boolean deleteFile(String filePath){
        File file = new File(filePath);
        if(file.exists()){
            return file.delete();
        }
        return false;
    }
}
