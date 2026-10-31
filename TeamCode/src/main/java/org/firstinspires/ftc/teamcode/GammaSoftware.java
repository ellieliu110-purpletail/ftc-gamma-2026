package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "GammaSoftware", group = "Gamma")
public class GammaSoftware extends LinearOpMode {

    private DcMotor frontLeftMotor;
    private DcMotor backLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backRightMotor;

    private DcMotor intakeMotor;
    private DcMotor intakeMotorTwo;

    private DcMotorEx launcherMotor;

    private Servo intakeServo;

    private int buffer = 0;
    private double inSpeed2 = 0;

    // Launcher target velocity (ticks/sec)
    private double launcherVelocity = 1500;

    private void initializeMotors() {

        launcherMotor = hardwareMap.get(DcMotorEx.class, "launcherMotor");
        intakeMotor = hardwareMap.dcMotor.get("intakeMotor");
        intakeMotorTwo = hardwareMap.dcMotor.get("intakeMotorTwo");
        intakeServo = hardwareMap.get(Servo.class, "intakeServo");

        launcherMotor.setDirection(DcMotor.Direction.FORWARD);
        launcherMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        launcherMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        intakeMotorTwo.setDirection(DcMotor.Direction.FORWARD);
        intakeMotorTwo.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);

        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");

        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    private void doDriving() {

        double y = -gamepad1.left_stick_y;
        double x = gamepad1.left_stick_x * 1.1;
        double rx = gamepad1.right_stick_x;

        double denominator =
                Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        frontLeftMotor.setPower(frontLeftPower);
        backLeftMotor.setPower(backLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backRightMotor.setPower(backRightPower);

        telemetry.addData("Front Left", frontLeftPower);
        telemetry.addData("Back Left", backLeftPower);
        telemetry.addData("Front Right", frontRightPower);
        telemetry.addData("Back Right", backRightPower);

        telemetry.addData("Target Velocity", launcherVelocity);
        telemetry.addData("Actual Velocity", launcherMotor.getVelocity());
    }

    private void doIntake() {

        double inSpeed =
                gamepad1.right_trigger - gamepad1.left_trigger;

        if (gamepad1.right_bumper) {
            inSpeed2 = 1.0;
        } else if (gamepad1.left_bumper) {
            inSpeed2 = -1.0;
        } else {
            inSpeed2 = 0;
        }

        intakeMotor.setPower(inSpeed);
        intakeMotorTwo.setPower(inSpeed2);

        // Increase velocity
        if (gamepad1.dpad_up) {
            launcherVelocity += 100;
        }

        // Decrease velocity
        if (gamepad1.dpad_down) {
            launcherVelocity -= 100;
        }

        // Clamp velocity
        launcherVelocity =
                Math.max(0, Math.min(3000, launcherVelocity));

        // Run launcher
        if (gamepad1.a) {
            launcherMotor.setVelocity(launcherVelocity);
            telemetry.addData("Motor", "Running");
        } else {
            launcherMotor.setVelocity(0);
        }

        if (gamepad1.b) {
            buffer++;
        }

        if (buffer > 0) {
            buffer--;
            intakeServo.setPosition(0.5);
            sleep(350);
            intakeServo.setPosition(0.0);
            telemetry.addData("Working!", "Servo Fired");
        }
    }

    @Override
    public void runOpMode() throws InterruptedException {
        initializeMotors();
        telemetry.addLine("Ready!");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            doDriving();
            doIntake();
            telemetry.update();
        }
    }
}