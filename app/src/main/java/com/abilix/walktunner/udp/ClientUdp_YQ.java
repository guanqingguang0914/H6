package com.abilix.walktunner.udp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.Message;
import android.util.Log;


import com.abilix.walktunner.MainActivity;

import java.io.FileOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;


public class ClientUdp_YQ implements Runnable {
    private MainActivity serialActivity;
    public static FileOutputStream frecord;

    // private byte[] data = new byte[100]; // 接收服务器反馈数据
    private byte[] data = new byte[6400]; // 接收服务器反馈数据
    public static DatagramPacket getpacket;
    public DatagramPacket sendpacket;
    public DatagramSocket socket;
    public DatagramSocket socket2;
    public InetAddress getAddress;
    public static final int serverPort = 50000;
    public static final int serverPort2 = 51000;

    public static int doingIndex = -10;
    public static int doingIndex31 = -10;
    public static int doingIndex33 = -10;
    public static int receiveIndex = 0;
    public static boolean wifiControl = false;
    public static boolean stopSend = false;
    private static String lastBinName = "";

    WifiManager wifiManager;
    WifiAutoConnectManager wac;
    String ssid = "AbiilxWifi";
    String psw = "12345678";


    ConnectivityManager manager;

    public ClientUdp_YQ(MainActivity act) {
        this.serialActivity = act;
        setNetwork();
        StartTimer1();
//        DataBuffer.getFileList(new File(DataBuffer.DATA_PATH), DataBuffer.fileList);
    }

    public void setNetwork() {
        try {
            wifiManager = (WifiManager) this.serialActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            try {
                wac = new WifiAutoConnectManager(wifiManager);
                wac.connect(ssid, psw, psw.equals("") ? WifiAutoConnectManager.WifiCipherType.WIFICIPHER_NOPASS
                        : WifiAutoConnectManager.WifiCipherType.WIFICIPHER_WPA);
                Log.i("ClientUdp_YQ", "ClientUdp_YQ 已连接上:" + ssid + " :" + psw);
            } catch (Exception e) {
                e.printStackTrace();
                Log.i("ClientUdp_YQ", "ClientUdp_YQ 未连接上WIFI: " + e.getMessage());
                wifiControl = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean checkNetworkState() {
        boolean flag = false;
        // 得到网络连接信息
        manager = (ConnectivityManager) this.serialActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        // 去进行判断网络是否连接
        if (manager.getActiveNetworkInfo() != null) {
            flag = manager.getActiveNetworkInfo().isAvailable();
        }
        if (!flag) {
            wifiControl = false;
        } else
            wifiControl = true;
        return flag;
    }

    private Timer mTimer1;
    private TimerTask mTimerTask1;
    private long m_millisecond1 = 1000; // 200ms定时器
    private int counttimer;

    private void StartTimer1() {
        mTimer1 = new Timer();
        mTimerTask1 = new TimerTask() {
            @Override
            public void run() {
                try {
                    checkNetworkState();
                    if (!wifiControl) {

                        if (counttimer < 5) {
                            counttimer++;
                        } else {
                            counttimer = 0;
                            setNetwork();
                            //这里加一个运动状态判断 不在运动中
//                            if(!ParseBinFileUtil.getInstance().isRobotMoving()){
//                                byte[] eyedata = {(byte)0xff,0,0};
//                                byte [] eyesend = sendProtocol((byte)0x03,(byte)0xA3,(byte)0x74,eyedata);
//                                serialActivity.WriteToS(eyesend);
//                            }else {
//                                byte[] eyedata = {0,0,(byte)0xff};
//                                byte [] eyesend = sendProtocol((byte)0x03,(byte)0xA3,(byte)0x74,new byte[]{0,0,0});
//                                serialActivity.WriteToS(eyesend);
//                            }
                        }
                    }
                } catch (Exception e) {
                    Log.i("ClientUdp_YQ", "ClientUdp_YQ 显示当前帧出错" + e);
                }
            }
        };
        mTimer1.schedule(mTimerTask1, 0, m_millisecond1);
    }

    @Override
    public void run() {
        try {
            socket = new DatagramSocket(serverPort); // //首先创建一个DatagramSocket对象
        } catch (Exception e) {
            Log.i("ClientUdp_YQ", "ClientUdp_YQ 创建socket出错");
        }
        while (true) {
            try {
                Log.i("ClientUdp_YQ", "while(true))");
                getpacket = new DatagramPacket(data, data.length);// //创建一个DatagramSocket对象，并指定监听的端口号
                // ;
                socket.receive(getpacket);
                int packetlen = getpacket.getLength();
                int datalen = bytesToInt2(data, 1) + 3;
                Log.i("ClientUdp_YQ", "接收大小：" + packetlen + "发送大小：" + datalen);
                Log.e("helei","收到"+data[0]+" "+data[1]+" "+data[2]);
                // Log.i("ClientUdp_YQ","00000ClientUdp_YQ收到的数据  "+(data[5]&0xff)+"    all "+getHexstr(data,datalen));//打印数据
                if (packetlen < 12 )
                    continue;
                if ((data[0] & 0xff) == 0xff && (data[datalen - 1] & 0xff) == 0xAA && (data[8] & 0xff) == 3) {
                    if ((data[5] & 0xff) == 32 || ((data[5] & 0xff) != 32) && doingIndex != (data[3] & 0xff))
                        if (XORcheckSend(data, datalen) == data[datalen - 2]) {
                            if ((data[5] & 0xff) != 32)
                                doingIndex = (data[3] & 0xff);
                            if ((data[5] & 0xff) != 31 && (data[5] & 0xff) != 32 && (data[5] & 0xff) != 33) {// 31/32/33立马执行
                                receiveIndex = (data[4] & 0xff);
                                // new Thread(){//旧stm32不能用
                                // @Override
                                // public void run() {
                                // try {
                                // mOutputStream.write(readAngle());
                                // } catch (Exception e) {
                                // Log.i("ClientUdp_YQ","ClientUdp_YQ 收发舵机角度值出错"+e);
                                // }
                                // }
                                // }.start();
                                int sleeptime0 = (data[6] & 0xff);
                                if (sleeptime0 < 20)
                                    sleeptime0 = 100;
                                Thread.sleep((sleeptime0 - receiveIndex) * 50);// 100*50
                            }
                            Log.i("ClientUdp_YQ", "00000ClientUdp_YQ 接收到群发信息" + (data[5] & 0xff));
                            switch ((data[5] & 0xff)) {
                                case 0:// 释放
                                    //serialActivity.handler.sendEmptyMessage(6);
                                    break;
                                case 1:// 固定
                                    //serialActivity.handler.sendEmptyMessage(7);
                                    break;
                                case 2:// 归零
                                    /*if (lastBinName.equals("") || lastBinName.equals("TBboys_End")
                                            || lastBinName.equals("zero")) {
                                        serialActivity.handler.sendEmptyMessage(5);
                                        lastBinName = "zero";
                                    }*/
                                    break;
                                case 3:// 动作执行广播
                                    String binName = bytesToString(data, 10, datalen - 3);

                                    if (lastBinName.equals("")
                                            || (lastBinName.equals("TBboys_End") && binName.equals("TBboys_Start"))
                                            || (lastBinName.equals("TBboys_Start") && !binName.equals("TBboys_Start"))
                                            || (lastBinName.equals("zero") && binName.equals("TBboys_Start"))
                                            || ((!lastBinName.equals("") && !lastBinName.equals("TBboys_Start")
                                            && !lastBinName.equals("TBboys_End") && !lastBinName.equals("zero")) && !binName
                                            .equals("TBboys_Start"))) {// 相冲突状态不执行
                                        Message msg1 = Message.obtain();
                                        msg1.what = 0;
                                        msg1.arg1 = 0;
                                        msg1.obj = binName;
                                        //serialActivity.handler.sendMessage(msg1);
                                        lastBinName = binName;
                                    }

                                    break;
                                case 4:// 打开电量检测
                                   // serialActivity.handler.sendEmptyMessage(8);
                                    break;
                                case 5:// 关闭电量检测
                                    //serialActivity.handler.sendEmptyMessage(9);
                                    break;
                                case 6:// 停止动作
                                   // serialActivity.handler.sendEmptyMessage(10);
                                    break;
                                case 7:// 打开最大声音
                                    //DataBuffer.StopMusic = false;
                                    //serialActivity.handler.sendEmptyMessage(11);
                                    break;
                                case 8:// 最小声音
                                   // DataBuffer.StopMusic = true;
                                    //serialActivity.handler.sendEmptyMessage(12);
                                    break;
                                case 9:// 打开帧显示
                                    //serialActivity.handler.sendEmptyMessage(13);
                                    break;
                                case 10:// 关闭帧显示
                                    //serialActivity.handler.sendEmptyMessage(14);
                                    break;
                                case 20:// 下载bin文件前先发送bin文件信息
                                    break;
                                case 21:// 下载的bin文件内容
                                    break;
                                case 31:// 回复
                                    if (doingIndex31 == (data[3] & 0xff))
                                        continue;
                                    else {
                                        doingIndex31 = (data[3] & 0xff);
                                    }
                                    Log.i("ClientUdp_YQ", "00000ClientUdp_YQ 接收到群发信息000000000" + stopSend);
                                    if (!stopSend) {
                                        stopSend = true;
                                        if (socket2 == null)
                                            socket2 = new DatagramSocket(); // 本地端口号和地址
                                        final InetAddress Address = getpacket.getAddress();
                                        Log.i("ClientUdp_YQ", "00000ClientUdp_YQ 接收到群发信息    " + Address);
                                        new Thread() {// 回复消息
                                            @Override
                                            public void run() {
                                                int shitNum = 1;
                                                // 修改协议，添加数据位，机器人返回自己的序列号 lz
                                                // 2017-3-13
                                                // byte[] sendData = null;
                                                // SharedPreferences sp =
                                                // serialActivity.getSharedPreferences("H_Digit",
                                                // Context.MODE_PRIVATE);
                                                // int i = sp.getInt("random_digit",
                                                // -1);
                                                // if (i == -1) {
                                                byte[] sendData = sendDateByte(101);
                                                // } else {
                                                // byte[] bs = intToByteArray(i);
                                                // Log.e("lz", "bs:" + bs);
                                                // sendData = sendDateByte(101, bs);
                                                // }
                                                Long stopTime = System.currentTimeMillis();
                                                while (stopSend && (System.currentTimeMillis() - stopTime) < 5000) {
                                                    try {
                                                        sendpacket = new DatagramPacket(sendData, sendData.length, Address,
                                                                serverPort2); // 目标端口号和地址
                                                        if (shitNum == 1)
                                                            Thread.sleep(10 + (int) (Math.random() * 200 / shitNum));
                                                        if (stopSend)
                                                            socket2.send(sendpacket); // 调用socket对象的send方法，发送数据
                                                        Log.i("ClientUdp_YQ", "IP: "+Address +"回复101");
                                                        Thread.sleep(50 + (int) (Math.random() * 200 / shitNum));
                                                        if (shitNum < 20)
                                                            shitNum = shitNum * 2;
                                                    } catch (Exception e) {
                                                        Log.i("ClientUdp_YQ", "ClientUdp_YQ 回复101出错" + e);
                                                    }
                                                }
                                                stopSend = false;
                                            }
                                        }.start();
                                    }
                                    break;
                                case 32:// 停止回复
                                    stopSend = false;
                                    break;
                                case 33:// 发送下载文件信息 让机器人建立tcp连接接收文件
                                    if (doingIndex33 == (data[3] & 0xff))
                                        continue;
                                    else {
                                        doingIndex33 = (data[3] & 0xff);
                                    }
//                                    if (!DataBuffer.hasclient && DataProcess.GetManger().initGetFile(data, datalen)) {// 初始化完成
//                                        new Thread(new ClientRunnable()).start();
//                                        Log.i("ClientUdp_YQ", "ClientUdp_YQ 建立tcp连接准备接收文件 ");
//                                    }
                                    break;

                                case 40:// 分组动作执行
                                    // json["group","1";"name","TBboys_End";"time",1]
                                    SharedPreferences sp = serialActivity.getSharedPreferences("H_Digit",
                                            Context.MODE_PRIVATE);
                                    String group = sp.getString("group", "0");
                                    if ("0".equals(group)) {
                                       // LogMgr.e("没有分组，不能执行分组动作");
                                        return;
                                    }

                                    int len = datalen - 3 - 10 + 1;
                                    byte[] str = new byte[len];
                                    System.arraycopy(data, 10, str, 0, len);
                                    if (str.length <= 0) {
                                       // LogMgr.e("没有分组数据");
                                        return;
                                    }
                                    byte[] bs = new byte[20];
                                    int delayTime = 0;
                                    String fileName = "";
                                    for (int i = 0; i < str.length / 20; i++) {
                                        System.arraycopy(str, i * 20, bs, 0, bs.length);
                                        int j = bs[0] & 0xff;
                                        if (String.valueOf(j).equals(group)) {
                                            delayTime = bytesToInt3(bs, 1);
                                            int k = bs[4] & 0xff;
                                            fileName = bytesToString(bs, 5, 5 + k - 1);
                                            // serialActivity.handler.sendEmptyMessage(1);
                                            break;
                                        }
                                    }

                                    if (lastBinName.equals("")
                                            || (lastBinName.equals("TBboys_End") && fileName.equals("TBboys_Start"))
                                            || (lastBinName.equals("TBboys_Start") && !fileName.equals("TBboys_Start"))
                                            || (lastBinName.equals("zero") && fileName.equals("TBboys_Start"))
                                            || ((!lastBinName.equals("") && !lastBinName.equals("TBboys_Start")
                                            && !lastBinName.equals("TBboys_End") && !lastBinName.equals("zero")) && !fileName
                                            .equals("TBboys_Start"))) {// 相冲突状态不执行
                                        Message msg1 = Message.obtain();
                                        msg1.what = 0;
                                        msg1.arg1 = 0;
                                        msg1.obj = fileName;
                                        //serialActivity.handler.sendMessageDelayed(msg1, delayTime);
                                        lastBinName = fileName;
                                    }
                                    break;
                            }
                            // RecordFile(getHexstr(data,datalen));//打印数据保存文件夹
                        }
                }else if((data[0] & 0xff) == 0xAA && (data[1] & 0xff) == 0x55){//这里前进后退不走群控协议。
                    Log.e("helei","--------"+data[2]);
                    switch(data[2]){
                        case 80: //前进
                        case 81: //左转
                        case 82: //后退
                        case 83: //右转
                        case 84: //停止
                        case 76: //右转弯
                        case 77: //左转弯
                        case 85: //转弯半径减小
                        case 86: //转弯半径增大
                            Message msg1 = Message.obtain();
                            msg1.what =data[2];
                            serialActivity.handler.sendMessage(msg1);
                            Log.e("helei","收到指令 "+data[2]);
                            break;

                    }

                }
            } catch (Exception e) {
                Log.e("ClientUdp_YQ", "00000ClientUdp_YQ 接收广播出错" + e);
            }
        }
    }

    /**
     * int转2个字节数组
     * <p>
     * 高位在前低位在后
     *
     * @param x
     * @return
     */
    public static byte[] intToByteArray(int x) {
        byte[] b = new byte[2];
        b[0] = (byte) ((x >> 8) & 0xFF);
        b[1] = (byte) (x & 0xFF);
        return b;
    }

    /**
     * 没有数据位打包
     *
     * @param cmd
     * @return
     */
    public byte[] sendDateByte(int cmd) {// 指令打包
        byte[] data0 = new byte[12]; // 发送数据
        data0[0] = (byte) 0xff;// 报头
        data0[1] = (byte) (0xff & ((12 - 3) >> 8));// 长度高位
        data0[2] = (byte) (0xff & (12 - 3));// 长度低位
        data0[3] = (byte) (0);//
        data0[4] = (byte) (0);//
        data0[5] = (byte) cmd;// 指令种类
        data0[8] = (byte) 3;// 机器人类型
        data0[11] = (byte) 0xAA;// 报尾
        data0[10] = XORcheckSend(data0, 12);
        return data0;
    }

    /**
     * 有数据位打包
     *
     * @param cmd
     * @param bs
     * @return
     */
    public byte[] sendDateByte(int cmd, byte[] bs) {// 指令打包
        if (bs == null) {
            return new byte[0];
        }
        byte[] data0 = new byte[12 + bs.length]; // 发送数据
        data0[0] = (byte) 0xff;// 报头
        data0[1] = (byte) (0xff & ((12 + bs.length - 3) >> 8));// 长度高位
        data0[2] = (byte) (0xff & (12 + bs.length - 3));// 长度低位
        data0[3] = (byte) (0);//
        data0[4] = (byte) (0);//
        data0[5] = (byte) cmd;// 指令种类
        data0[8] = (byte) 3;// 机器人类型
        System.arraycopy(bs, 0, data0, 10, bs.length);
        data0[11 + bs.length] = (byte) 0xAA;// 报尾
        data0[10 + +bs.length] = XORcheckSend(data0, 12 + bs.length);
        return data0;
    }

    private void RecordFile(String str) {
        String strPath = String.format("%s/test.txt", Environment.getExternalStorageDirectory());
        try {
            frecord = new FileOutputStream(strPath, true);
            frecord.write(str.getBytes());
            frecord.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String bytesToString(byte[] bytes, int begin, int end) {// byte转化为string
        int len = end - begin + 1;
        byte[] str = new byte[len];
        System.arraycopy(bytes, begin, str, 0, len);
        return new String(str);
    }

    public static int bytesToInt2(byte[] bytes, int begin) {// 两个字节转化为int
        // 高位在前低位在后
        return (int) (0x00ff & bytes[begin + 1]) | ((0x00ff & bytes[begin]) << 8);
    }

    /**
     * byte3个字节转int 高位在前
     *
     * @param bytes
     * @param begin
     * @return
     */
    public static int bytesToInt3(byte[] bytes, int begin) {// 3个字节转化为int
        // 高位在前低位在后
        return (int) ((0x00ff & bytes[begin + 2]) | ((0x00ff & bytes[begin + 1]) << 8) | ((0x00ff & bytes[begin]) << 16));
    }

    private String getHexstr(byte[] data, int length) {// 将接收的数据转换为16进制String
        int v;
        String hv = "";
        for (int i = 0; i < length; i++) {
            v = data[i] & 0xFF;
            if (v <= 0x0f)
                hv = hv + " 0" + Integer.toHexString(v);
            else
                hv = hv + " " + Integer.toHexString(v);
        }
        return hv + "\r\n";
    }

    public static byte XORcheckSend(byte[] buf, int len) {// 传参是完整报文,生成CRC
        if (len < 12)
            return -1;

        byte crc = buf[0];
        for (int i = 1; i <= len - 3; i++) {
            crc = (byte) (crc ^ (buf[i]));
        }
        return crc;
    }

    public static byte[] readAngle() {// 旧版不能用
        byte[] sendBuff = new byte[12];
        sendBuff[0] = (byte) 0xaa;
        sendBuff[1] = (byte) 0x55;
        sendBuff[2] = (byte) 0;
        sendBuff[3] = 8;
        sendBuff[4] = (byte) 0x03;
        sendBuff[5] = (byte) 0xa3;
        sendBuff[6] = (byte) 0x61;
        sendBuff[8] = (byte) 3;
        for (int i = 0; i < 11; i++) {
            sendBuff[11] = (byte) (sendBuff[11] + sendBuff[i]);
        }
        return sendBuff;
    }
    //加一个装配数据。
    public byte[] sendProtocol(byte type, byte cmd1, byte cmd2, byte[] data) {
        byte[] sendbuff;
        if (data == null) {
            byte[] buf = new byte[8];
            buf[0] = type;
            buf[1] = cmd1;
            buf[2] = cmd2;
            sendbuff = addProtocol(buf);
        } else {
            byte[] buf = new byte[8 + data.length];
            buf[0] = type;
            buf[1] = cmd1;
            buf[2] = cmd2;
            System.arraycopy(data, 0, buf, 7, data.length);
            sendbuff = addProtocol(buf);
        }
        return sendbuff;
    }

    // 协议封装： AA 55 len1 len2 type cmd1 cmd2 00 00 00 00 (data) check
    public static byte[] addProtocol(byte[] buff) {
        short len = (short) (buff.length);
        byte[] sendbuff = new byte[len + 4];
        sendbuff[0] = (byte) 0xAA; // 头
        sendbuff[1] = (byte) 0x55;
        sendbuff[3] = (byte) (len & 0x00FF); // 长度: 从type到check
        sendbuff[2] = (byte) ((len >> 8) & 0x00FF);
        System.arraycopy(buff, 0, sendbuff, 4, buff.length); // type - data

        byte check = 0x00; // 校验位
        for (int n = 0; n <= len + 2; n++) {
            check += sendbuff[n];
        }
        sendbuff[len + 3] = (byte) (check & 0x00FF);
        return sendbuff;
    }
}
