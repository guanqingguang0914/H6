/*
 *   Kinematics.cpp
 *
 *   Author: ROBOTIS
 *
 */

#include <math.h>
#include "Kinematics.h"


using namespace Robot;

const double Kinematics::CAMERA_DISTANCE = 33.2; //mm
const double Kinematics::EYE_TILT_OFFSET_ANGLE = 40.0; //degree
const double Kinematics::LEG_SIDE_OFFSET = 25.5; //mm  //31.0(darwin);25.5(H3)
const double Kinematics::THIGH_LENGTH = 65.0; //mm //93.0(darwin); 65.0(H5);68.47(H3)
const double Kinematics::CALF_LENGTH = 65.0; //mm  //93.0(darwin); 65.0(H5);70.50(H3)
const double Kinematics::ANKLE_LENGTH = 44.5; //mm  //33.5(darwin);44.5(H5);36.39(H3)
const double Kinematics::LEG_LENGTH = 174.5; //mm (THIGH_LENGTH + CALF_LENGTH + ANKLE_LENGTH)

Kinematics* Kinematics::m_UniqueInstance = new Kinematics();

Kinematics::Kinematics()
{
}

Kinematics::~Kinematics()
{
}
