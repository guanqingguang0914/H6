package com.abilix.robot.walktunner;

public class Walk {

    private volatile static Walk instance;

    private Walk() {
    }

    public static Walk getInstance() {
        if (instance == null) {
            synchronized (Walk.class) {
                if (instance == null) {
                    instance = new Walk();
                }
            }
        }
        return instance;
    }

    public native void initWalk();

    public native void stopwalk();

    public native void startForwardWalk(int speed);

    public native void startBackwardWalk(int speed);

    public native void startLeftWalk(int speed);

    public native void startRightWalk(int speed);

    public native void startLeftForwardWalk(int leftSpeed,int forwardSpeed);

    public native void startLeftBackwardWalk(int leftSpeed,int backwardSpeed);

    public native void startRightForwardWalk(int rightSpeed,int forwardSpeed);

    public native void startRightBackwardWalk(int rightSpeed,int backwardSpeed);

    public native void startTurnWalk(int speed,int direction,int flag);

    public native int[] getMotorValue();

    public native void updateGyro(float x, float y, float z);

    public native void updateAccele(float x, float y, float z);

    public native void updateAngle(float x, float y, float z);

    static {
        System.loadLibrary("walk_tuner");
    }
}
