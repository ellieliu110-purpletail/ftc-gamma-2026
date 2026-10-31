package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "GammaSoftware", group = "Gamma")
public class GammaSoftware extends LinearOpMode
{
    private DcMotor frontLeftMotor;
    private DcMotor backLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backRightMotor;
    private DcMotor intakeMotor;
    private DcMotorEx launcherMotor;
    private Servo intakeServo;
    private int buffer = 0;
    private double launcherVelocity = 1200.0; // Target velocity in ticks/sec
    private boolean lastDpadUp = false;
    private boolean lastDpadDown = false;

    private void initializeMotors()
    {
        launcherMotor = hardwareMap.get(DcMotorEx.class, "launcherMotor");
        intakeMotor = hardwareMap.dcMotor.get("intakeMotor");
        intakeServo = hardwareMap.get(Servo.class, "intakeServo");
        launcherMotor.setDirection(DcMotor.Direction.FORWARD);
        launcherMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        launcherMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        intakeMotor.setDirection(DcMotor.Direction.FORWARD);
        intakeMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");

        // Reverse the left side
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        // Stop immediately when power is zero
        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    private void doDriving() {
        // Forward/backward
        double y = -gamepad1.left_stick_y;

        // Strafe left/right
        double x = gamepad1.left_stick_x * 1.1;

        // Rotate
        double rx = gamepad1.right_stick_x;

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

        telemetry.addData("Left Stick Y", gamepad2.left_stick_y);
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

        // DPAD Up increases target velocity of the launcher motor on single press
        if (gamepad1.dpad_up && !lastDpadUp) {
            launcherVelocity = launcherVelocity + 100.0;
        }
        lastDpadUp = gamepad1.dpad_up;

        // DPAD Down decreases target velocity of the launcher motor on single press
        if (gamepad1.dpad_down && !lastDpadDown) {
            launcherVelocity = launcherVelocity - 100.0;
        }
        lastDpadDown = gamepad1.dpad_down;

        // Sets target velocity to 0 if it is less than 0
        if (launcherVelocity < 0.0){
            launcherVelocity = 0.0;
        }

        // A moves launcher at target velocity
        if (gamepad1.a) {
            launcherMotor.setVelocity(launcherVelocity);
            telemetry.addData("Motor", "Working_B");
        } else {
            launcherMotor.setVelocity(0.0);
        }

        // Buffer goes up when bumper is pressed
        if (gamepad1.bWasPressed()) {
            buffer = buffer + 1;
        }
        //  Buffer goes down when rotation is finished
        if (buffer > 0) {
            buffer = buffer - 1;
            intakeServo.setPosition(0.5);
            sleep(350);
            intakeServo.setPosition(0.0);
            telemetry.addData("Working!", "Working_Right_Bumper");
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
