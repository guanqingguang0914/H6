#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <unistd.h>
#include <termios.h>
#include <signal.h>
#include <libgen.h>
#include "cmd_process.h"
#include "mjpg_streamer.h"
#include "jni.h"
#include "walk_jni.h"

#include <android/log.h>
#define  LOG_TAG    "walk_jni"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


#define INI_FILE_PATH       "/sdcard/www/config.ini"
//#define INI_FILE_PATH       "/sdcard/Abilix/walktunner/config.ini"

using namespace Robot;

float gyro_x=0.0, gyro_y=0.0, accel_x=0.0, accel_y=0.0;
float pose_roll=0.0, pose_pitch=0.0, pose_yaw=0.0;

LinuxCM730 linux_cm730("/dev/ttyUSB0");
CM730 cm730(&linux_cm730);

void change_current_dir()
{
    char exepath[1024] = {0};
    if(readlink("/proc/self/exe", exepath, sizeof(exepath)) != -1)
        chdir(dirname(exepath));
}

void sighandler(int sig)
{
    struct termios term;
    tcgetattr( STDIN_FILENO, &term );
    term.c_lflag |= ICANON | ECHO;
    tcsetattr( STDIN_FILENO, TCSANOW, &term );

    exit(0);
}

int main(int argc, char *argv[])
{
    signal(SIGABRT, &sighandler);
    signal(SIGTERM, &sighandler);
    signal(SIGQUIT, &sighandler);
    signal(SIGINT, &sighandler);

    change_current_dir();

    minIni* ini = new minIni(INI_FILE_PATH);

    mjpg_streamer* streamer = new mjpg_streamer(0, 0);
    httpd::ini = ini;


    if(MotionManager::GetInstance()->Initialize(&cm730) == false)
    {
        printf("Fail to initialize Motion Manager!\n");
        //这一步先跳过，继续下一步的调试（2017/06/23）
        //return 0;
    }
    MotionManager::GetInstance()->LoadINISettings(ini);
    Walking::GetInstance()->LoadINISettings(ini);

    MotionManager::GetInstance()->AddModule((MotionModule*)Walking::GetInstance());
    LinuxMotionTimer *motion_timer = new LinuxMotionTimer(MotionManager::GetInstance());
    motion_timer->Start();

    DrawIntro(&cm730);
    MotionManager::GetInstance()->SetEnable(true);

    return 0;
}

/*
 * Class:     com_abilix_robot_walktunner_MainActivity
 * Method:    startWalk
 * Signature: ()V
 */
JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_initWalk
  (JNIEnv * env , jobject){
    main(0, NULL);
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_startForwardWalk
  (JNIEnv * env , jobject obj,jint speed){
  Walking::GetInstance()->Start();
  Walking::GetInstance()->X_MOVE_AMPLITUDE = speed;
  Walking::GetInstance()->Y_MOVE_AMPLITUDE = 0;
  Walking::GetInstance()->A_MOVE_AMPLITUDE = 0;
  Walking::GetInstance()->PERIOD_TIME = 1000;
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_startBackwardWalk
  (JNIEnv * env , jobject obj,jint speed){
    Walking::GetInstance()->Start();
    Walking::GetInstance()->X_MOVE_AMPLITUDE = -speed;
    Walking::GetInstance()->Y_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->A_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->PERIOD_TIME = 1000;
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_startLeftWalk
  (JNIEnv * env , jobject obj,jint speed){
    Walking::GetInstance()->Start();
    Walking::GetInstance()->X_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->Y_MOVE_AMPLITUDE = speed;
    Walking::GetInstance()->A_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->PERIOD_TIME = 1000;
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_startRightWalk
  (JNIEnv * env , jobject obj,jint speed){
    Walking::GetInstance()->Start();
    Walking::GetInstance()->X_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->Y_MOVE_AMPLITUDE = -speed;
    Walking::GetInstance()->A_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->PERIOD_TIME = 1000;
}

JNIEXPORT void JNICALL Java_com_abilix_robot_walktunner_Walk_startTurnWalk
  (JNIEnv * env, jobject obj,jint speed,jint direction,jint flag){
    Walking::GetInstance()->Start();
    Walking::GetInstance()->X_MOVE_AMPLITUDE = speed;
    Walking::GetInstance()->Y_MOVE_AMPLITUDE = 0;
    if(flag == 1){
       Walking::GetInstance()->A_MOVE_AMPLITUDE = direction;
    }else if(flag == 2){
       Walking::GetInstance()->A_MOVE_AMPLITUDE = -direction;
    }
    Walking::GetInstance()->PERIOD_TIME = 1000;
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_startLeftForwardWalk
  (JNIEnv * env , jobject obj,jint leftSpeed,jint forwardSpeed){
    Walking::GetInstance()->Start();
    Walking::GetInstance()->X_MOVE_AMPLITUDE = forwardSpeed;
    Walking::GetInstance()->Y_MOVE_AMPLITUDE = leftSpeed;
    Walking::GetInstance()->A_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->PERIOD_TIME = 1000;
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_startLeftBackwardWalk
  (JNIEnv * env , jobject obj,jint leftSpeed,jint backwardSpeed){
    Walking::GetInstance()->Start();
    Walking::GetInstance()->X_MOVE_AMPLITUDE = -backwardSpeed;
    Walking::GetInstance()->Y_MOVE_AMPLITUDE = leftSpeed;
    Walking::GetInstance()->A_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->PERIOD_TIME = 1000;
}
JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_startRightForwardWalk
  (JNIEnv * env , jobject obj,jint rightSpeed,jint forwardSpeed){
    Walking::GetInstance()->Start();
    Walking::GetInstance()->X_MOVE_AMPLITUDE = forwardSpeed;
    Walking::GetInstance()->Y_MOVE_AMPLITUDE = -rightSpeed;
    Walking::GetInstance()->A_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->PERIOD_TIME = 1000;
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_startRightBackwardWalk
  (JNIEnv * env , jobject obj,jint rightSpeed,jint backwardSpeed){
    Walking::GetInstance()->Start();
    Walking::GetInstance()->X_MOVE_AMPLITUDE = -backwardSpeed;
    Walking::GetInstance()->Y_MOVE_AMPLITUDE = -rightSpeed;
    Walking::GetInstance()->A_MOVE_AMPLITUDE = 0;
    Walking::GetInstance()->PERIOD_TIME = 1000;
}


JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_updateGyro
  (JNIEnv * env , jobject obj,jfloat x,jfloat y,jfloat z){
    gyro_x = x*30.0;
    gyro_y = y*30.0;
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_updateAccele
  (JNIEnv * env , jobject obj,jfloat x,jfloat y,jfloat z){
    accel_x = x;
    accel_y = y;
}

JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_updateAngle
  (JNIEnv * env , jobject obj,jfloat pitch,jfloat roll,jfloat yaw){
    pose_roll = roll;
    pose_pitch = pitch;
    pose_yaw = yaw;
}



/*
 * Class:     com_abilix_robot_walktunner_MainActivity
 * Method:    stopwalk
 * Signature: ()V
 */
JNIEXPORT void Java_com_abilix_robot_walktunner_Walk_stopwalk
  (JNIEnv * env, jobject obj){
   Walking::GetInstance()->Stop();
}

/*
 * Class:     com_abilix_robot_walktunner_Walk
 * Method:    getMotorValue
 * Signature: ()[B
 */
JNIEXPORT jintArray JNICALL Java_com_abilix_robot_walktunner_Walk_getMotorValue
  (JNIEnv * env, jobject obj){
    jintArray jarr = env->NewIntArray(20);
    //2.获取数组指针
    jint *arr = env->GetIntArrayElements(jarr, NULL);
    //3.赋值
    int currentId = 0;
    for(int i = 1; i < 21; i++){
        if(i == 13){
           currentId = 1;
        }else if(i == 14){
           currentId = 2;
        }else if(i ==15){
           currentId = 3;
        }else if(i == 16){
           currentId = 4;
        }else if(i == 17){
           currentId = 5;
        }else if(i == 18){
           currentId = 6;
        }else if(i == 11){
           currentId = 7;
        }else if(i == 12){
           currentId = 8;
        }else if(i == 3){
           currentId = 9;
        }else if(i == 4){
           currentId = 10;
        }else if(i == 1){
           currentId = 11;
        }else if(i == 2){
           currentId = 12;
        }else if(i == 5){
           currentId = 13;
        }else if(i == 6){
           currentId = 14;
        }else if(i == 7){
           currentId = 15;
        }else if(i == 8){
           currentId = 16;
        }else if(i == 9){
           currentId = 17;
        }else if(i == 10){
           currentId = 18;
        }else if(i == 21){
           currentId = 19;
        }else if(i == 22){
           currentId = 20;
        }
        arr[i-1] = Walking::GetInstance()->m_Joint.GetValue(currentId);
    }
    //4.释放资源
    env->ReleaseIntArrayElements(jarr, arr, 0);
    //5.返回数组
    return jarr;
}
