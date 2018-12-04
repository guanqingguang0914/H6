package com.abilix.robot.servo;

public class MX_28 {

    //==============================calibratePitch=============================
    private boolean isAnglePitchEnable = false;
    private static final double PITCH_TO_ANGLE = 1000 / 300;

    private static int PITCH_WINDOW_SIZE = 100;
    private static int PITCH_MARGIN_SD = 2;
    private static int cal_idx = 0;
    private static int cal_center = 0;
    private static float pitchOffset = 0;
    private static int[] cal_array = new int[PITCH_WINDOW_SIZE];

    private static int[] pPosOriginal = {512, 212, 812, 512, 512, 512, 512, 512, 512, 410, 614, 716, 308, 512, 512, 358, 666, 512, 512, 512, 512, 512, 512};
    private static int[] pPosOffsetSum = new int[23];
    //==========================Timer for adjust Pose==========================

    public static byte[] Servo_SetPosAll(byte iCount, byte[] pID, byte[] pPos) {
        int iLength = 0;
        byte bChecksum = 0;
        int i = 0;

        if (iCount < 1 || iCount > 30){
            return null;
        }
        iLength = 8 + iCount * 3;
        byte[] gSendBuff = new byte[iLength];
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) 0xFE;
        gSendBuff[3] = (byte) (4 + iCount * 3);
        gSendBuff[4] = (byte) 0x83;
        gSendBuff[5] = (byte) 0x1E;
        gSendBuff[6] = 0x02;
        for (i = 0; i < iCount; i++) // 22次
        {
            if (pID[i] < 254) {
                gSendBuff[7 + i * 3] = pID[i];
                gSendBuff[7 + i * 3 + 1] = (byte) (pPos[i * 2]);
                gSendBuff[7 + i * 3 + 2] = (byte) (pPos[i * 2 + 1]);
            } else {
                gSendBuff[7 + i * 3] = 0x00;
                gSendBuff[7 + i * 3 + 2] = 0x00;
                gSendBuff[7 + i * 3 + 1] = 0x00;
            }
        }
        for (i = 2; i < iLength - 1; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[iLength - 1] = bChecksum;
        return DoWriteFrame(gSendBuff, iLength);
    }

    public static byte[] Servo_SetPosSpeedAll(byte iCount, byte[] pID, byte[] pPos, int speed) {
        int iLength = 0;
        byte bChecksum = 0;
        int i = 0;
        if (iCount < 1 || iCount > 30) {
            return null;
        }
        iLength = 8 + iCount * 5;
        byte[] gSendBuff = new byte[iLength];
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) 0xFE;
        gSendBuff[3] = (byte) (4 + iCount * 5);
        gSendBuff[4] = (byte) 0x83;
        gSendBuff[5] = (byte) 0x1E;
        gSendBuff[6] = 0x04;

        for (i = 0; i < iCount; i++) // 22次
        {
            if (pID[i] < 254) {
                gSendBuff[7 + i * 5] = pID[i];
                gSendBuff[7 + i * 5 + 1] = (byte) (pPos[i * 2]);
                gSendBuff[7 + i * 5 + 2] = (byte) (pPos[i * 2 + 1]);
                gSendBuff[7 + i * 5 + 3] = (byte) (speed & 0xFF); // (pPos[i*2]);
                gSendBuff[7 + i * 5 + 4] = (byte) 0x00; // (pPos[i*2+1]);
            } else {
                gSendBuff[7 + i * 5] = 0x00;
                gSendBuff[7 + i * 5 + 2] = 0x00;
                gSendBuff[7 + i * 5 + 1] = 0x00;
                gSendBuff[7 + i * 5 + 3] = 0x00;
                gSendBuff[7 + i * 5 + 4] = 0x00;
            }
        }
        for (i = 2; i < iLength - 1; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[iLength - 1] = bChecksum;
        return DoWriteFrame(gSendBuff, iLength);
    }

    public static byte[] Servo_SetSpeedAll(byte iCount, byte[] pID, byte[] pPos, int speed) {
        int iLength = 0;
        byte bChecksum = 0;
        int i = 0;

        if (iCount < 1 || iCount > 30){
            return null;
        }
        iLength = 8 + iCount * 3;
        byte[] gSendBuff = new byte[iLength];
        gSendBuff[0] = (byte) 0xFF;
        gSendBuff[1] = (byte) 0xFF;
        gSendBuff[2] = (byte) 0xFE;
        gSendBuff[3] = (byte) (4 + iCount * 3);
        gSendBuff[4] = (byte) 0x83;
        gSendBuff[5] = (byte) 0x20;
        gSendBuff[6] = 0x02;
        for (i = 0; i < iCount; i++) // 22次
        {
            if (pID[i] < 254) {
                gSendBuff[7 + i * 3] = pID[i];
                gSendBuff[7 + i * 3 + 1] = (byte) (speed & 0xFF); // (pPos[i*2]);
                gSendBuff[7 + i * 3 + 2] = (byte) 0x00; // (pPos[i*2+1]);
            } else {
                gSendBuff[7 + i * 3] = 0x00;
                gSendBuff[7 + i * 3 + 2] = 0x00;
                gSendBuff[7 + i * 3 + 1] = 0x00;
            }
        }
        for (i = 2; i < iLength - 1; i++) {
            bChecksum += gSendBuff[i];
        }
        bChecksum = (byte) ~bChecksum;
        bChecksum = (byte) (bChecksum & 0xFF);
        gSendBuff[iLength - 1] = bChecksum;
        return DoWriteFrame(gSendBuff, iLength);
    }

    private static byte[] DoWriteFrame(byte[] pBuf, int dwLen) {
        int iLength = dwLen + 8;
        byte[] sendBuff = new byte[iLength];
        sendBuff[0] = (byte) 0xFE;
        sendBuff[1] = (byte) 0x68;
        sendBuff[2] = (byte) 'Z';
        sendBuff[3] = 0x00;
        sendBuff[4] = (byte) ((dwLen >> 8) & 0xFF);
        sendBuff[5] = (byte) (dwLen & 0xFF);
        sendBuff[iLength - 2] = (byte) 0xAA;
        sendBuff[iLength - 1] = (byte) 0x16;
        System.arraycopy(pBuf, 0, sendBuff, 6, dwLen);
        return sendBuff;
//        serialPortInstance.sendBuffer(sendBuff);
    }

    // calibrate pitch(获取一段时间内的pitch值)
    public static float autoCalibratePitch(float pitch) {
        if (cal_idx < PITCH_WINDOW_SIZE) {
            cal_array[cal_idx] = (int) pitch;
            cal_idx++;
        } else {
            double sum = 0.0;
            double sd = 0.0;
            double diff;
            double mean = 0.0;
            cal_idx = 0;
            for (int i = 0; i < PITCH_WINDOW_SIZE; i++) {
                sum += cal_array[i];
            }
            mean = sum / PITCH_WINDOW_SIZE;
            sum = 0.0;
            for (int i = 0; i < PITCH_WINDOW_SIZE; i++) {
                diff = cal_array[i] - mean;
                sum += diff * diff;
            }
            sd = Math.sqrt(sum / PITCH_WINDOW_SIZE);
            if (sd < PITCH_MARGIN_SD) {
                pitchOffset = (float) mean;
            } else {
                pitchOffset = 0;
            }
        }
        return pitchOffset;
    }

    //根据陀螺仪的pith(即：前后偏角)值,计算机器人“正直站立”时,并记录此时6个关节的偏移量.
    public static void calculatePoseOffset(float pitchOffset) {
        double delta = 0;
        delta = pitchOffset * 0.5f * PITCH_TO_ANGLE;
        pPosOffsetSum[8] += (int) delta;
        pPosOffsetSum[9] -= (int) delta;
        delta = pitchOffset * 0.3f * PITCH_TO_ANGLE;
        pPosOffsetSum[10] -= (int) delta;
        pPosOffsetSum[11] += (int) delta;
        delta = pitchOffset * 0.2f * PITCH_TO_ANGLE;
        pPosOffsetSum[12] -= (int) delta;
        pPosOffsetSum[13] += (int) delta;
//        SharedPreferencesUtils sharedUtils = SharedPreferencesUtils.getInstance();
//        sharedUtils.putInt(SharedPreferencesKeys.SERVO_HIP_PITCH_RIGHT,pPosOffsetSum[8]);
//        sharedUtils.putInt(SharedPreferencesKeys.SERVO_HIP_PITCH_LEFT,pPosOffsetSum[9]);
//        sharedUtils.putInt(SharedPreferencesKeys.SERVO_KNEE_RIGHT,pPosOffsetSum[10]);
//        sharedUtils.putInt(SharedPreferencesKeys.SERVO_KNEE_LEFT,pPosOffsetSum[11]);
//        sharedUtils.putInt(SharedPreferencesKeys.SERVO_ANKLE_PITCH_RIGHT,pPosOffsetSum[12]);
//        sharedUtils.putInt(SharedPreferencesKeys.SERVO_ANKLE_PITCH_LEFT,pPosOffsetSum[13]);
//        sharedUtils.commitValue();
    }
}
