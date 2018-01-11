/*
 * @(#)HttpTester.java	2018-01-10
 *
 * Copyright 2010 Fiberhome. All rights reserved.
 */
package com.jlwan.livex;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author jlwan
 * @version 1.0, 2018-01-10
 * @since 1.0
 */
public class HttpTester {

    private int roomMin;
    private int roomMax;
    private String lineName;
    private int threadCount = 10;
    private IMsgHandler msgHandler;

    public HttpTester(int roomMin, int roomMax, String lineName, int threadCount, IMsgHandler msgHandler) {
        this.roomMin = roomMin;
        this.roomMax = roomMax;
        this.lineName = lineName;
        this.threadCount = threadCount;
        this.msgHandler = msgHandler;
    }

    public void test(final List<String> lstOmmitUrls) {
        final AtomicInteger currentRoom = new AtomicInteger(roomMin - 1);
        final AtomicBoolean isFounded = new AtomicBoolean(false);
        final AtomicInteger doneRoom = new AtomicInteger();
        final AtomicInteger doneThread = new AtomicInteger();
        final int roomCnt = roomMax - roomMin + 1;
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    while (true) {
                        if (isFounded.get()) {
                            break;
                        }
                        int room = currentRoom.incrementAndGet();
                        if (room > roomMax) {
                            break;
                        }
                        String url = createUrl(lineName, room);
                        if (lstOmmitUrls != null && lstOmmitUrls.contains(url)) {
                            continue;
                        }
                        boolean isValid = isValidUrl(url);
                        msgHandler.onProgress(doneRoom.incrementAndGet(), roomCnt);
                        if (isValid) {
                            isFounded.set(true);
                            msgHandler.onSuccess(url);
                            break;
                        }
                    }
                    if (doneThread.incrementAndGet() == threadCount && !isFounded.get()) {
                        msgHandler.onFailed();
                    }

                }
            }, lineName + "-thread-" + i);
            thread.start();
        }
    }

    private String createUrl(String lineName, int room) {
        String originalUrl = "https://neunlds" + room + ".akamaized.net/nlds/nba/" + lineName + "/as/live/nlncp/" + lineName + "_hd.m3u8";
        String url = originalUrl;
        return url;
    }

    public boolean isValidUrl(String url) {
        HttpURLConnection connection = null;
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            connection = (HttpURLConnection) realUrl.openConnection();
            // 设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            //connection.setReadTimeout(3 * 10000);
            // 建立实际的连接
            //connection.connect();

            int responseCode = connection.getResponseCode();
            msgHandler.print("[" + responseCode + "]:" + url);
            return 200 == responseCode;


            // 获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
//            // 遍历所有的响应头字段
//            for (String key : map.keySet()) {
//                System.out.println(key + "--->" + map.get(key));
//            }
//            // 定义 BufferedReader输入流来读取URL的响应
//            in = new BufferedReader(new InputStreamReader(
//                    connection.getInputStream()));
//            String line;
//            while ((line = in.readLine()) != null) {
//                result += line;
//            }
        } catch (Exception e) {
            msgHandler.print("[ERROR]:" + url + ", ex=" + e);
            //e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return false;
    }
}
