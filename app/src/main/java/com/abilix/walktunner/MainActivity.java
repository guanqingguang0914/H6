package com.abilix.walktunner;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.WindowManager;

import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.abilix.robot.servo.ServoSetPosService;
import com.abilix.robot.walktunner.Walk;
import com.abilix.robot.walktunner.sensor.SensorImuService;
import com.abilix.walktunner.udp.ClientUdp_YQ;
import com.abilix.walktunner.utils.Utilities;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    public Walk walk;

    private Timer mTimer;
    private TimerTask mTimerTask;

    @BindView(R.id.tv)
    public TextView ip;

    private byte iCount;
    private byte[] pID;
    private int mServoCount = 23;

    private static int FORWARD_SPEED = 20;
    private static int BACKWARD_SPEED = 20;
    private static int LEFT_SPEED = 10;
    private static int RIGHT_SPEED = 10;
    private static int TURN_SPEED = 20;
    private static int FLAG_RIGHT = 2;
    private static int FLAG_LEFT = 1;


    public ClientUdp_YQ clientUdp_yq;

    public Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 76: //右转弯
                    walk.startTurnWalk(FORWARD_SPEED, TURN_SPEED, FLAG_RIGHT);
                    break;
                case 77: //左转弯
                    walk.startTurnWalk(FORWARD_SPEED, TURN_SPEED, FLAG_LEFT);
                    break;
                case 80://  前进
                    Toast.makeText(MainActivity.this,"80",Toast.LENGTH_LONG).show();
                    walk.startForwardWalk(0);
                    break;
                case 81: //左移
                    walk.startLeftWalk(LEFT_SPEED);
                    break;
                case 82: //后退
                    walk.startBackwardWalk(BACKWARD_SPEED);
                    break;
                case 83: //右移
                    walk.startRightWalk(RIGHT_SPEED);
                    break;
                case 84: //停止
                    walk.stopwalk();
                    break;
                case 85: //减小半径
                    TURN_SPEED +=5;
                    Toast.makeText(MainActivity.this,"85===>"+TURN_SPEED,Toast.LENGTH_LONG).show();
                    if (TURN_SPEED >= 50) {
                        TURN_SPEED = 50;
                    }
                    break;
                case 86: //增大半径
                    Toast.makeText(MainActivity.this,"86===>"+TURN_SPEED,Toast.LENGTH_LONG).show();
                    TURN_SPEED -=5;
                    if (TURN_SPEED <= 0) {
                        TURN_SPEED = 0;
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ButterKnife.bind(this);
        initdata();
        WifiManager wifiManager = (WifiManager) this.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String sip = Utilities.intToIp(ipAddress);
        ip.setText(sip);
        clientUdp_yq = new ClientUdp_YQ(MainActivity.this);
        new Thread(clientUdp_yq).start();
    }

    private void initdata() {
        startService(new Intent(MainActivity.this, SensorImuService.class));
        startService(new Intent(MainActivity.this, ServoSetPosService.class));
        walk = Walk.getInstance();
        startTimer();
        iCount = (byte) mServoCount; // 23
        pID = new byte[mServoCount];
        for (int n = 0; n < mServoCount; n++) {
            pID[n] = (byte) (n); // 0~22
        }
    }

    @OnClick({R.id.forward, R.id.backward, R.id.left, R.id.right, R.id.right_forward, R.id.right_backward, R.id.left_forward, R.id.stop, R.id.finish})
    public void execButtonClick(View v) {
        switch (v.getId()) {
            case R.id.forward:
                walk.startForwardWalk(FORWARD_SPEED);
                break;
            case R.id.backward:
                walk.startBackwardWalk(BACKWARD_SPEED);
                break;
            case R.id.left:
                walk.startLeftWalk(LEFT_SPEED);
                break;
            case R.id.right:
                walk.startRightWalk(RIGHT_SPEED);
                break;
            case R.id.right_forward:
                walk.startTurnWalk(0, 20, 2);
                break;
            case R.id.left_forward:
                walk.startTurnWalk(0, 20, 1);
                break;
            case R.id.right_backward:
                walk.startRightBackwardWalk(RIGHT_SPEED, BACKWARD_SPEED);
                break;
            case R.id.stop:
                walk.stopwalk();
                break;
            case R.id.finish:
                finishAll();
                break;
        }
    }

    private void startTimer() {
        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                int[] data = walk.getMotorValue();
                int[] data2 = new int[23];
                for (int i = 0; i < data2.length; i++) {
                    if (i == 0) {
                        data2[i] = 0;
                    } else if (i > 0 && i <= 18) {
                        data2[i] = data[i - 1];
                    } else if (i == 19 || i == 20) {
                        data2[i] = 512;
                    } else if (i == 21) {
                        data2[i] = 562;
                    } else if (i == 22) {
                        data2[i] = (int) (0.5 * (data[i - 3] + 590)); //10为舵机偏差
//                        data2[i] = 512;
                    }
                }
                byte[] data3 = new byte[46];
                for (int i = 0; i < data2.length; i++) {
                    data3[i * 2] = (byte) (data2[i] & 0xFF);
                    data3[i * 2 + 1] = (byte) ((data2[i] >> 8) & 0xFF);
                }
                try {
                    ServoSetPosService.Servo_SetPosAll(data3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        mTimer.schedule(mTimerTask, 100, 41);
    }

    private void stopTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, SensorImuService.class));
        stopService(new Intent(MainActivity.this, ServoSetPosService.class));
        stopTimer();
    }

    private void finishAll() {
        stopService(new Intent(MainActivity.this, SensorImuService.class));
        stopService(new Intent(MainActivity.this, ServoSetPosService.class));
        stopTimer();
        finish();
    }
}

