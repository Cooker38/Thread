package com.atguigu.Thread;

import java.net.HttpURLConnection;
import java.net.URL;

public class Task {
    public String taskName;
    public long fileLen = 0; //文件总长度
    public final int bufLen = 8192; //缓冲区长度
    public int numThread = 0; //线程数
    public String urlName; //下载的url
    public String saveName; //保存的文件路径
    public ThrDownload[] thrDownloads; //线程实力
    public ThrCount thrCount; //统计线程
    public boolean isPart = false; //是否已经下载了部分(断点续传)
    public long[] startParts; //断点续传后每个线程的开始位置
    public DownLoadDialog dialog;
    public boolean running = true;

    public Task(String taskName, DownLoadDialog dialog){
        this.taskName = taskName;
        this.dialog = dialog;
    }

    public boolean init(final String urlName,final String saveName,final int numThread,final boolean isPart){
        this.isPart = isPart;
        this.urlName = urlName;
        this.saveName = saveName;
        this.numThread = numThread;
        try{
            URL url = new URL(urlName);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            this.fileLen = con.getContentLength();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }

        if(this.fileLen == -1)  //资源五Content_Length
            return false;

        this.thrDownloads = new ThrDownload[numThread];
        for(int i = 0;i<numThread;i++)
            thrDownloads[i] = new ThrDownload(i,fileLen,numThread,isPart,startParts,urlName,saveName,bufLen);
        this.thrCount = new ThrCount(this);
        return true;
    }

    //断点续传
    public boolean initContinue(final String urlName,final String saveName,final int numThread,final long[] startParts,final boolean isPart){
        this.startParts = startParts;
        this.isPart = true;
        return this.init(urlName,saveName,numThread,isPart);
    }

}
