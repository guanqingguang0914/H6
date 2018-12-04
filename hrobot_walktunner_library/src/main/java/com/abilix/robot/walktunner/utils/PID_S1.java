package com.abilix.robot.walktunner.utils;

/**
 * Created by kevin on 17-7-7.
 */

public class PID_S1 {

    class PID_S{
        double set_pitch;     //设置 pitch
        double current_pitch; //当前的pitch
        double err;           //本次误差
        double integral;      //积分值
        double err_last;      //上次误差
        double Kp,Ki,Kd;      //P/I/D 系数
    }

    PID_S g_left_engine=new PID_S();
    PID_S g_right_engine=new PID_S();


     public PID_S1()
    {
        g_left_engine.set_pitch     = 0.0f;
        g_left_engine.current_pitch = 0.0f;
        g_left_engine.err           = 0.0f;
        g_left_engine.err_last      = 0.0f;
        g_left_engine.integral      = 0.0f;
        //g_left_engine.Kp            = 8.0f;
        g_left_engine.Kp            = 1023/300.0f*1.0f;//4.8f;
        g_left_engine.Ki            = 0.05f;
        //g_left_engine.Ki            = 0.0f;
        g_left_engine.Kd            = 0.00f;

        g_right_engine.set_pitch     = 0.0f;
        g_right_engine.current_pitch = 0.0f;
        g_right_engine.err           = 0.0f;
        g_right_engine.err_last      = 0.0f;
        g_right_engine.integral      = 0.0f;
        //g_right_engine.Kp            = 8.0f;
        g_right_engine.Kp            = 1023/300.0f*1.0f; //4.8f;
        g_right_engine.Ki           = 0.05f;
        //g_right_engine.Ki           = 0.0f;
        g_right_engine.Kd            = 0.00f;
    }

    public double PID_realize(double actual_pitch, double goal_pitch, int steer_engine_id)
    {
        double increment_val;

        if (0 == steer_engine_id)
        {
            g_left_engine.set_pitch     = goal_pitch;
            g_left_engine.current_pitch = actual_pitch;
            g_left_engine.err           = g_left_engine.set_pitch - g_left_engine.current_pitch;
            g_left_engine.integral     += g_left_engine.err;

            increment_val              = g_left_engine.Kp*g_left_engine.err
                    + g_left_engine.Ki*g_left_engine.integral
                    + g_left_engine.Kd*(g_left_engine.err-g_left_engine.err_last);
            g_left_engine.err_last     = g_left_engine.err;
        }
        else
        {
            g_right_engine.set_pitch    = goal_pitch;
            g_right_engine.current_pitch  = actual_pitch;
            g_right_engine.err          = g_right_engine.set_pitch - g_right_engine.current_pitch;
            g_right_engine.integral    += g_right_engine.err;

            increment_val               = g_right_engine.Kp*g_right_engine.err
                    + g_right_engine.Ki*g_right_engine.integral
                    + g_right_engine.Kd*(g_right_engine.err-g_right_engine.err_last);
            g_right_engine.err_last     = g_right_engine.err;
        }
        return increment_val;
    }
}
