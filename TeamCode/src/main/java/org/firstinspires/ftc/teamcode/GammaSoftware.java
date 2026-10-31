package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "GammaSoftware", group = "Gamma")
public class GammaSoftware extends LinearOpMode
{
    private DcMotor frontLeftMotor;
    private DcMotor backLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backRightMotor;
    private DcMotor intakeMotor;
    private DcMotor intakeMotor2;
    private DcMotorEx launcherMotor;
    private Servo intakeServo;
    private int buffer = 0;
    private double launcherVelocity = 1400.0; // Target velocity in ticks/sec
    private boolean lastDpadUp = false;
    private boolean lastDpadDown = false;

    private void initializeMotors()
    {
        launcherMotor = hardwareMap.get(DcMotorEx.class, "launcherMotor");
        intakeMotor = hardwareMap.dcMotor.get("intakeMotor");
        intakeMotor2 = hardwareMap.dcMotor.get("intakeMotor2");
        intakeServo = hardwareMap.get(Servo.class, "intakeServo");
        launcherMotor.setDirection(DcMotor.Direction.FORWARD);
        launcherMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        launcherMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        intakeMotor2.setDirection(DcMotor.Direction.REVERSE);
        intakeMotor2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");

        // Reverse the left side
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.FORWARD);

        // Stop immediately when power is zero
        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    private void doDriving() {
        // Forward/backward
        double y = -gamepad2.left_stick_y;

        // Strafe left/right
        double x = gamepad2.left_stick_x * 1.1;

        // Rotate
        double rx = gamepad2.right_stick_x;

        // Prevent any motor power from exceeding 1.0
        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rx), 1);

        // Calculate motor powers
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
        telemetry.addData("Launcher Target Velocity", launcherVelocity);
        telemetry.addData("Launcher Actual Velocity", launcherMotor.getVelocity());
    }

    private void doIntake() {
        // Right trigger moves ball in, left trigger moves ball out
        double inSpeed = gamepad1.right_trigger - gamepad1.left_trigger;
        intakeMotor.setPower(inSpeed);
        if (inSpeed > 0.1) {
            telemetry.addData("Working!", "Working_Right_Trigger");
        } else if (inSpeed < -0.1) {
            telemetry.addData("Working!", "Working_Left_Trigger");
        }

        if(gamepad1.left_bumper){
            intakeMotor2.setPower(0.1);
        } else if(gamepad1.right_bumper){
            intakeMotor2.setPower(-1.0);
        } else {
            intakeMotor2.setPower(0.0);
        }

        // DPAD Up increases target velocity of the launcher motor on single press
        if (gamepad1.dpad_up && !lastDpadUp) {
            launcherVelocity = launcherVelocity + 1.0;
        }
        lastDpadUp = gamepad1.dpad_up;

        // DPAD Down decreases target velocity of the launcher motor on single press
        if (gamepad1.dpad_down && !lastDpadDown) {
            launcherVelocity = launcherVelocity - 1.0;
        }
        lastDpadDown = gamepad1.dpad_down;

        // Sets target velocity to 0 if it is less than 0
        if (launcherVelocity < 1400.0){
            launcherVelocity = 1400.0;
        }

        if (launcherVelocity > 1500.0){
            launcherVelocity = 1500.0;

        }

        // A moves launcher at target velocity
        if (gamepad2.a) {
            launcherMotor.setVelocity(launcherVelocity);
            telemetry.addData("Motor", "Working_B");
        } else {
            launcherMotor.setVelocity(0.0);
        }

        if (gamepad1.b) {
            intakeServo.setPosition(1.0);
        } else {
            intakeServo.setPosition(0.0);
        }
    }

    @Override public void runOpMode() throws InterruptedException {
        // Initialize hardware ONCE
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
