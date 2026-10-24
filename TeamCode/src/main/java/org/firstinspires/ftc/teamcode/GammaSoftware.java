package org.firstinspires.ftc.teamcode;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "GammaSoftware", group = "Gamma")
public class GammaSoftware extends LinearOpMode
{
    private DcMotor frontLeftMotor;
    private DcMotor backLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backRightMotor;
    private DcMotor intakeMotor;
    private DcMotor launcherMotor;
    private Servo intakeServo;
    private int buffer = 0;

    private void initializeMotors()
    {
        launcherMotor = hardwareMap.dcMotor.get("launcherMotor");
        intakeMotor = hardwareMap.dcMotor.get("intakeMotor");
        intakeServo = hardwareMap.get(Servo.class, "intakeServo");
        launcherMotor.setDirection(DcMotor.Direction.FORWARD);
        launcherMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
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
        double frontLeftPower = (y + x + rx) / denominator;
        double backLeftPower = (y - x + rx) / denominator;
        double frontRightPower = (y - x - rx) / denominator;
        double backRightPower = (y + x - rx) / denominator;

        frontLeftMotor.setPower(frontLeftPower);
        backLeftMotor.setPower(backLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backRightMotor.setPower(backRightPower);

        telemetry.addData("Left Stick Y", gamepad1.left_stick_y);
        telemetry.addData("Front Left", frontLeftPower);
        telemetry.addData("Back Left", backLeftPower);
        telemetry.addData("Front Right", frontRightPower);
        telemetry.addData("Back Right", backRightPower);
        telemetry.update();
    }

    private void doIntake() {
        // A moves intake
        if (gamepad1.a) {
            intakeMotor.setPower(1.0);
            telemetry.addData("Working!", "Working_A");
        } else {
            intakeMotor.setPower(0.0);
        }

        // B moves launcher
        if (gamepad1.b) {
            launcherMotor.setPower(1.0);
            telemetry.addData("Motor", "Working_B");
        } else {
            launcherMotor.setPower(0.0);
        }

        // Buffer goes up when bumper is press, Buffer goes down when rotation is finished
        if (gamepad1.rightBumperWasPressed()) {
            buffer = buffer + 1;
        }

        if (buffer > 0) {
            buffer = buffer - 1;
            intakeServo.setPosition(1);
            sleep(50);
            intakeServo.setPosition(0.5);
            telemetry.addData("Working!", "Working_Right_Bumper");
        }
        telemetry.update();
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
        }

    }

}
