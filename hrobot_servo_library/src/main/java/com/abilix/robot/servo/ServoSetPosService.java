package com.abilix.robot.servo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.abilix.robot.serialport.SerialPortInstance;

public class ServoSetPosService extends Service {

    private static SerialPortInstance serialPortInstance;

    private static byte iCount;
    private static byte[] pID;
    private static int mServoCount = 23;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        serialPortInstance = SerialPortInstance.getInstance();
        iCount = (byte) mServoCount; // 23
        pID = new byte[mServoCount];
        for (int n = 0; n < mServoCount; n++) {
            pID[n] = (byte) (n); // 0~22
        }
    }

    public static void Servo_SetPosAll(byte[] pPos){
        serialPortInstance.sendBuffer(MX_28.Servo_SetPosAll(iCount,pID,pPos));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        serialPortInstance.closeSerialPort();
    }
}
