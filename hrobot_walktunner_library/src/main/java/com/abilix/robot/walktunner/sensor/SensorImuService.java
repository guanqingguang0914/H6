package com.abilix.robot.walktunner.sensor;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;

import com.abilix.robot.walktunner.Walk;
import com.abilix.robot.walktunner.utils.RollFilter;

public class SensorImuService extends Service implements SensorEventListener {

    //Walk
    public Walk walk;

    //Sensor
    private SensorManager sensorManager;
    private Sensor gyroSsensor;
    private Sensor accSensor;

    //==============================Kalman Filter==============================
    public static float[] accValues;
    public static float[] gyroValues;
    float gyroX, gyroY, gyroZ;
    float accelX, accelY, accelZ;
    public static float Angle_Pitch, Gyro_Balance, Gyro_Turn, Angle_roll,Angle_yaw;
    float Acceleration_Z;
    double K1 = 0.02;
    float angleR, angle_dot;
    double Q_angle = 0.001;
    double Q_gyro = 0.003;
    double R_angle = 0.5;
    //double dt=0.010;
    double dt = 0.1;   //0.0025
    char C_0 = 1;
    float Q_bias, Angle_err;
    float PCt_0, PCt_1, E;
    float K_0, K_1, t_0, t_1;
    public float[] Pdot = {0, 0, 0, 0};
    public float[][] PP_new = {{1, 0}, {0, 1}};
    RollFilter mRollFilter;
    //=================================Yaw=========================================
    private static boolean isAngleYawAvaiable = false;
    private float timestamp = 0;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float angle[] = new float[3];
    private static float zeroDrift = 0.011f;

    private enum INIT_STATUS {
        NOT_INIT,
        ON_INITING,
        ON_INITING2,
        INITED,
    }

    private static INIT_STATUS initStatus = INIT_STATUS.NOT_INIT;
    private float zeroDriftSum = 0.0f;
    private int zeroDriftCount = 0;

    private boolean beginYaw = false;
    private float beginYawTime = 0;
    private float beginYawValue = 0.0f;


    private static int ZERO_DRIFT_MAX_COUNT = 1000;   //计算零漂第一阶测试次数  默认值1000,大约耗时6秒钟 500,大约耗时3秒钟
    private int TestYawTime_s = 5;                   //计算零漂第二阶测试时长，单位：秒 默认测试值为15
    private float yawDiffPreMinute = 3.0f;            //除零漂后计算的yaw值，每分钟最大偏yawDiffPreMinute度 默认值：0.1
    private float preYawMaxDiff = TestYawTime_s * yawDiffPreMinute / 60.0f;
    private int ignoreCountWhenCalcuZeroDrift=500;   //不考虑第一次放下机器人时，陀螺仪不准确。 默认值1000,大约耗时6秒钟
    //==============================================================================================================================


    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        onInit();
        walk = Walk.getInstance();
        walk.initWalk();
        mRollFilter = new RollFilter();
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        gyroSsensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(this, gyroSsensor, SensorManager.SENSOR_DELAY_FASTEST);
        accSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gyroValues = event.values;

            //老版本陀螺仪方向
              gyroX = -gyroValues[0];//pitch
              gyroY = gyroValues[2];//roll
              gyroZ = gyroValues[1];//yaw

            //新版本陀螺仪方向
//            gyroX = -gyroValues[0];//pitch
//            gyroY = -gyroValues[2];//roll
//            gyroZ = gyroValues[1];//yaw

            /*if (timestamp != 0) {
                if (initStatus == INIT_STATUS.ON_INITING) {
                    if (--ignoreCountWhenCalcuZeroDrift > 0){
                        return;
                    }
                    zeroDriftSum += event.values[0];
                    ++zeroDriftCount;
                    if (zeroDriftCount >= ZERO_DRIFT_MAX_COUNT) {
                        zeroDrift = zeroDriftSum / zeroDriftCount;
                        initStatus = INIT_STATUS.ON_INITING2;
                        beginYaw = false;
                    }
                } else {
                    final float dT = (event.timestamp - timestamp) * NS2S;
                    angle[0] += (event.values[0] - zeroDrift) * dT;
                    Angle_yaw = (float) Math.toDegrees(angle[0]);
                    if (Angle_yaw > 360) Angle_yaw -= 360;
                    if (Angle_yaw < -360) Angle_yaw += 360;
                    if (initStatus == INIT_STATUS.ON_INITING2) {
                        if (beginYaw == false) {
                            beginYawTime = event.timestamp;
                            beginYawValue = Angle_yaw;
                            beginYaw = true;
                        } else {
                            float t_time = event.timestamp - beginYawTime;
                            if (t_time > 1000000000 * TestYawTime_s) {
                                float diff = Math.abs(Math.abs(Angle_yaw) - Math.abs(beginYawValue));
                                if (preYawMaxDiff >= diff) {
                                    initStatus = INIT_STATUS.INITED;
                                    isAngleYawAvaiable = true;
                                } else {
                                    onInit();
                                }
                            }
                        }
                    }
                }
            }

            timestamp = event.timestamp;*/
            walk.updateGyro(gyroX, gyroY, gyroZ);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accValues = event.values;
            //老版本加速度计方向
            accelX = -accValues[0];
            accelY = accValues[2];
            accelZ = accValues[1];
            //新版本加速度计方向
//            accelX = -accValues[0];
//            accelY = -accValues[2];
//            accelZ = accValues[1];
            walk.updateAccele(accelX, accelY, accelZ);
        }

        if (gyroValues != null && accValues != null) {
            Angle_Pitch = Get_Angle(2, gyroX, gyroZ, accelY, accelZ);
            Angle_roll = mRollFilter.Get_Angle(2, gyroY, gyroZ, accelX, accelZ);
//            Log.e("SensorIMU","Angle_yaw====>"+Angle_yaw);
            walk.updateAngle(Angle_roll, Angle_Pitch, Angle_yaw);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public void onInit()
    {
        zeroDriftSum=0.0f;
        zeroDriftCount=0;
        ignoreCountWhenCalcuZeroDrift=0;
        initStatus= INIT_STATUS.ON_INITING;
        isAngleYawAvaiable = false;
    }



    public static float[] getGyroValues() {
        return gyroValues;
    }

    public static float[] getAccValues() {
        return accValues;
    }

    public static float getAngle_Roll(){
        return Angle_roll;
    }

    public static float getAngle_Pitch() {
        return Angle_Pitch;
    }

    public static float getAngle_yaw(){
        if(isAngleYawAvaiable){
            return Angle_yaw;
        }
        return -1;
    }

    private float Get_Angle(int way, float gyroX, float gyroZ, float accelY, float accelZ) {
        float Accel_Y;
        float Accel_Angle;
        float Accel_Z;
        float Gyro_X;
        float Gyro_Z;

        Gyro_X = gyroX;
        Gyro_Z = gyroZ;
        Accel_Y = accelY;
        Accel_Z = accelZ;
        if (Gyro_X > 32768) Gyro_X -= 65536;
        if (Gyro_Z > 32768) Gyro_Z -= 65536;
        if (Accel_Y > 32768) Accel_Y -= 65536;
        if (Accel_Z > 32768) Accel_Z -= 65536;
        Gyro_Balance = Gyro_X;
        Accel_Angle = (float) (Math.atan2(Accel_Y, Accel_Z) * 180 / (3.1415926));
        //Gyro_X= (float) (Gyro_X/16.4/2);
        if (way == 2) Kalman_Filter(Accel_Angle, Gyro_X);
        else if (way == 3) Yijielvbo(Accel_Angle, Gyro_X);
        Angle_Pitch = angleR;
        Gyro_Turn = Gyro_Z;
        Acceleration_Z = Accel_Z;
        return Angle_Pitch;
    }

    private void Kalman_Filter(float Accel, float Gyro) {
        angleR += (Gyro - Q_bias) * dt;
        Pdot[0] = (float) (Q_angle - PP_new[0][1] - PP_new[1][0]);

        Pdot[1] = -PP_new[1][1];
        Pdot[2] = -PP_new[1][1];
        Pdot[3] = (float) Q_gyro;
        PP_new[0][0] += Pdot[0] * dt;
        PP_new[0][1] += Pdot[1] * dt;
        PP_new[1][0] += Pdot[2] * dt;
        PP_new[1][1] += Pdot[3] * dt;

        Angle_err = Accel - angleR;

        PCt_0 = C_0 * PP_new[0][0];
        PCt_1 = C_0 * PP_new[1][0];

        E = (float) (R_angle + C_0 * PCt_0);

        K_0 = PCt_0 / E;
        K_1 = PCt_1 / E;

        t_0 = PCt_0;
        t_1 = C_0 * PP_new[0][1];

        PP_new[0][0] -= K_0 * t_0;
        PP_new[0][1] -= K_0 * t_1;
        PP_new[1][0] -= K_1 * t_0;
        PP_new[1][1] -= K_1 * t_1;

        angleR += K_0 * Angle_err;
        Q_bias += K_1 * Angle_err;
        angle_dot = Gyro - Q_bias;
    }

    private void Yijielvbo(float angle_m, float gyro_m) {
        angleR = (float) (K1 * angle_m + (1 - K1) * (angleR + gyro_m * 0.010));        // 0.005
    }
}
