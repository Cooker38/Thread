package com.atguigu.Thread;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

//下载线程
public class ThrDownload extends Thread {
    private int no;  //线程号
    private RandomAccessFile out; //在固定位置写入读取文件流
    private InputStream in; //网上读取
    private URL url; //下载URL
    private final long start; //开始的位置
    private long end; //结束的位置
    private byte[] b; //读写缓冲区
    private long finish; //本线程应该下载的总字节数
    private volatile boolean running = true;

    public ThrDownload(final int no, final long fileLen, final long numThread, final boolean isPart,
                       final long[] startParts, final String urlName, final String saveName, final int bufLen) {
        this.no = no;
        long blockLen = fileLen / numThread;

        //初始位置
        if (!isPart)
            start = blockLen * no;
        else
            start = blockLen * no + startParts[no];

        //结束位置
        if (no != numThread)
            end = blockLen * (no + 1) - 1;
        else
            end = fileLen - 1;

        finish = start - blockLen * no;
        if (start > end) {
//            running.set(false);
            running = false;
            return;
        }
        try {
            url = new URL(urlName);
            HttpURLConnection conHttp = (HttpURLConnection) url.openConnection();
            conHttp.setRequestProperty("Range", "bytes=" + start + "-" + end);
            in = conHttp.getInputStream();
            if (conHttp.getResponseCode() >= 300)
                throw new Exception("Http响应错误" + conHttp.getResponseCode());
            out = new RandomAccessFile(saveName, "rw");
            out.seek(start);
            b = new byte[bufLen];
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            int l;
            while (running && (l = in.read(b)) != -1) {
//                Thread.sleep(1000);
//                System.out.println("线程" + no + ": " + running);
                out.write(b, 0, l);
                finish += l;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopThread() {
        this.running = false;
//        System.out.println("线程" + no + "已经执行了stopThread方法-----" + running);
    }

    public boolean isRunning() {
        return running;
    }

    public long getFinish() {
        return finish;
    }

}
