package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;

import java.util.Locale;

// tile is 23 1/2 inch square, one unit is 47/2 inches (23.5 inches)
@Autonomous(name = "Pathfinder1", group = "Gamma")
public class F_G_try extends LinearOpMode {

    // Current robot coordinates
    private int currentX = 6;
    private int currentY = 3;

    // 1 unit = 47/2 inches = 23.5 inches
    public static final double UNIT_INCHES = 47.0 / 2.0;

    // Time in milliseconds to travel 1 unit (23.5 inches) at 0.5 power
    private static final long TIME_PER_UNIT_MS = 1000;
    // Time in milliseconds to turn 90 degrees at 0.5 power
    private static final long TIME_TURN_90_MS = 500;

    // Facing direction: 0 = +Y (Forward), 1 = +X (Right), 2 = -Y (Backward), 3 = -X (Left)
    private int facing = 0;

    // Direction flags
    boolean y_facing_forward = true;
    boolean y_facing_backward = false;
    boolean x_facing_forward = false;
    boolean x_facing_backward = false;

    private DcMotor frontLeftMotor;
    private DcMotor backLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backRightMotor;
    private DcMotor intakeMotor;
    private DcMotor intakeMotor2;
    private DcMotorEx launcherMotor;
    private Servo intakeServo;
    private double launcherVelocity = 1400.0; // Target velocity in ticks/sec


    private void initializeMotorsDrive() {
        telemetry.addLine("Initializing Motors...");
        telemetry.update();
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

        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    private void drive(double forward, double turn) {
        // Mecanum forward + rotation
        double fl = forward + turn;
        double bl = forward + turn;
        double fr = forward - turn;
        double br = forward - turn;

        // Normalize motor powers
        double max = Math.max(
                1.0,
                Math.max(
                        Math.max(Math.abs(fl), Math.abs(bl)),
                        Math.max(Math.abs(fr), Math.abs(br))
                )
        );

        frontLeftMotor.setPower(fl / max);
        backLeftMotor.setPower(bl / max);
        frontRightMotor.setPower(fr / max);
        backRightMotor.setPower(br / max);
    }

    private void stopMotors() {
        frontLeftMotor.setPower(0.0);
        backLeftMotor.setPower(0.0);
        frontRightMotor.setPower(0.0);
        backRightMotor.setPower(0.0);
    }

    private void updateFacingFlags() {
        y_facing_forward  = (facing == 0);
        x_facing_forward  = (facing == 1);
        y_facing_backward = (facing == 2);
        x_facing_backward = (facing == 3);
    }

    private void turnToFacing(int targetFacing) {
        if (!opModeIsActive()) return;

        int diff = (targetFacing - facing + 4) % 4;
        if (diff == 1) {
            // Turn Right 90 degrees
            drive(0.0, 0.5);
            sleep(TIME_TURN_90_MS);
            stopMotors();
        } else if (diff == 3) {
            // Turn Left 90 degrees
            drive(0.0, -0.5);
            sleep(TIME_TURN_90_MS);
            stopMotors();
        } else if (diff == 2) {
            // Turn 180 degrees
            drive(0.0, 0.5);
            sleep(TIME_TURN_90_MS * 2);
            stopMotors();
        }

        facing = targetFacing;
        updateFacingFlags();
        sleep(200);
    }

    private void driveUnits(int units) {
        if (!opModeIsActive() || units <= 0) return;

        drive(0.5, 0.0);
        sleep(units * TIME_PER_UNIT_MS);
        stopMotors();
        sleep(10);
    }

    private void launchBalls(){
        launcherMotor.setVelocity(launcherVelocity);
        sleep(500);
        intakeMotor2.setPower(1.0);
        sleep(500);
        launcherMotor.setVelocity(0.0);
        intakeMotor2.setPower(0.0);
    }



    private void pathfinding( int xdes, int ydes) {
        int x1 = 0;
        int y1 = 0;
        currentX = x1;
        currentY = y1;

        telemetry.addLine(String.format(Locale.US, "Starting path: (%d, %d) -> (%d, %d)", x1, y1, xdes, ydes));
        telemetry.update();

        // Move along X axis
        int deltaX = xdes - x1;
        if (deltaX != 0) {
            int targetFacing = (deltaX > 0) ? 1 : 3; // 1 = +X (Right), 3 = -X (Left)
            turnToFacing(targetFacing);

            int steps = Math.abs(deltaX);
            for (int step = 1; step <= steps; step++) {
                if (!opModeIsActive()) return;
                driveUnits(1);
                currentX += (deltaX > 0) ? 1 : -1;
                telemetry.addLine(String.format(Locale.US, "Moving X: Pos=(%d, %d)", currentX, currentY));
                telemetry.update();
            }
        }

        // Move along Y axis
        int deltaY = ydes - y1;
        if (deltaY != 0) {
            int targetFacing = (deltaY > 0) ? 0 : 2; // 0 = +Y (Forward), 2 = -Y (Backward)
            turnToFacing(targetFacing);

            int steps = Math.abs(deltaY);
            for (int step = 1; step <= steps; step++) {
                if (!opModeIsActive()) return;
                driveUnits(1);
                currentY += (deltaY > 0) ? 1 : -1;
                telemetry.addLine(String.format(Locale.US, "Moving Y: Pos=(%d, %d)", currentX, currentY));
                telemetry.update();
            }
        }

        // Set final coordinates when arrived
        currentX = xdes;
        currentY = ydes;

        telemetry.addLine(String.format(Locale.US, "Arrived at destination: (%d, %d)", currentX, currentY));
        telemetry.update();
    }

    @Override
    public void runOpMode() throws InterruptedException {
        initializeMotorsDrive();
        telemetry.addLine("Ready! Press START.");
        telemetry.update();
        waitForStart();
        telemetry.update();
        launchBalls();
        telemetry.addLine("Shot!");
        telemetry.update();
        pathfinding(3, 0);
        turnToFacing(3);
        x_facing_backward = true;
        pathfinding(2,0);
        pathfinding(0, 3);
        launchBalls();
        pathfinding(2, 0);
    }
}
