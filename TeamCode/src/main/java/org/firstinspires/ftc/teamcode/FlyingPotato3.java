package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp
public class FlyingPotato3 extends OpMode {
    DcMotor backLeftMotor;
    DcMotor backRightMotor;
    DcMotor frontLeftMotor;
    DcMotor frontRightMotor;

    int slow = 1/4;
    int medium = 1/2;
    int fast = 1;
    int speed_counter = 1;
    int speed = 0;
    float left_stick_y_value = gamepad1.left_stick_y;
    float turn_level_x = gamepad1.right_stick_x;

    // one of the operators wants the x y a b buttons for intake out-take and also wants the
    // triggers for launching/throwing
    @Override
    public void init(){
        backLeftMotor = hardwareMap.get(DcMotor.class, "motor0");
        backRightMotor = hardwareMap.get(DcMotor.class, "motor0");
        frontLeftMotor = hardwareMap.get(DcMotor.class, "motor0");
        frontRightMotor = hardwareMap.get(DcMotor.class, "motor0");
    }

    @Override
    public void loop(){
        backLeftMotor.setPower(0.0);
        backRightMotor.setPower(0.0);
        frontLeftMotor.setPower(0.0);
        frontRightMotor.setPower(0.0);

        if (left_stick_y_value < 0){
            backLeftMotor.setPower(speed);
            backRightMotor.setPower(speed);
            frontLeftMotor.setPower(speed);
            frontRightMotor.setPower(speed);
        }

        /*
        public void moveForward(double power, int timeMs) {
            frontLeft.setPower(speed);
            frontRight.setPower(speed);
            backLeft.setPower(speed);
            backRight.setPower(speed);
        }
             */
        if (left_stick_y_value > 0){
            backLeftMotor.setPower(-speed);
            backRightMotor.setPower(-speed);
            frontLeftMotor.setPower(-speed);
            frontRightMotor.setPower(-speed);
        }

        if (turn_level_x < 0){
            backLeftMotor.setPower(speed);
            frontLeftMotor.setPower(speed);
        }

        if (turn_level_x > 0){
            backRightMotor.setPower(speed);
            frontRightMotor.setPower(speed);
        }
        if (gamepad1.y){
            backLeftMotor.setPower(0.0);
            backRightMotor.setPower(0.0);
            frontLeftMotor.setPower(0.0);
            frontRightMotor.setPower(0.0);
        }
        if (gamepad1.left_bumper) {
            speed_counter = speed_counter - 1;
            if (speed_counter == 0) {
                speed_counter = 1;
            }
            if (gamepad1.right_bumper) {
                speed_counter = speed_counter + 1;
                if (speed_counter == 4) {
                    speed_counter = 3;
                }

            }
        }


    }
}
