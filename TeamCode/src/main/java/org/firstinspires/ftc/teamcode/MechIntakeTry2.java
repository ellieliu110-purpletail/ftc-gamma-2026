package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "MechIntakeTry2", group = "Gamma")
public class MechIntakeTry2 extends LinearOpMode {
    private DcMotor intakeMotor;
    private DcMotor launcherMotor;
    private Servo intakeServo;
    private void initializeMotors() {
        launcherMotor = hardwareMap.dcMotor.get("launcherMotor");
        intakeMotor = hardwareMap.dcMotor.get("intakeMotor");
        intakeServo = hardwareMap.get(Servo.class, "intakeServo");
        launcherMotor.setDirection(DcMotor.Direction.FORWARD);
        launcherMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    @Override
    public void runOpMode(){
        initializeMotors();
        waitForStart();
        telemetry.update();
        while (opModeIsActive()) {
            if (gamepad1.a){
                intakeMotor.setPower(1.0);
                telemetry.addData("Working!", "Working_A");
            } else {
                // set to 0
                intakeMotor.setPower(0.0);
            }

            if (gamepad1.right_trigger > 0.1) {
                intakeServo.setPosition(gamepad1.right_trigger);
                telemetry.addData("Working!", "Working_Right_Trigger");
            }

            // when press b, trigger launch motor
            if (gamepad1.b) {
                launcherMotor.setPower(1.0);
                telemetry.addData("Working!", "Working_B");
            } else {
                launcherMotor.setPower(0.0);
            }
        }
    }
}
