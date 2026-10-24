
package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.rev.RevHubOrientationOnRobot;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.IMU;
import com.qualcomm.robotcore.util.Range;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.YawPitchRollAngles;

/**
 * TeleOp / Auto Alignment OpMode using Limelight 3A vision sensor and REV Control Hub IMU.
 *
 * Drives to an AprilTag and aligns the robot using:
 *  - Limelight MegaTag2 orientation update from IMU yaw angle.
 *  - Limelight 3A AprilTag 3D camera-space target pose (X = strafe offset, Z = forward distance).
 *  - Limelight tx (horizontal error angle) for heading alignment.
 *  - Mecanum 3-DOF drive control (Forward, Strafe, Turn).
 *
 * Controls:
 *  - Hold Gamepad1 A or Left Bumper: Auto-drive & align to target AprilTag.
 *  - Manual control using Gamepad1 Joysticks when auto-align is not active.
 */
@TeleOp(name = "LimeLight Auto Align", group = "Gamma")
public class LimeLight extends LinearOpMode {

    // Drivetrain motors
    private DcMotor frontLeftMotor;
    private DcMotor backLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backRightMotor;

    // Sensors
    private Limelight3A limelight;
    private IMU imu;

    // Alignment Target & Tuning Parameters
    private static final double DESIRED_DISTANCE_METERS = 0.30; // 30 cm target distance to AprilTag
    private static final double DISTANCE_TOLERANCE_METERS = 0.02; // 2 cm tolerance
    private static final double STRAFE_TOLERANCE_METERS = 0.02;   // 2 cm tolerance
    private static final double ANGLE_TOLERANCE_DEGREES = 1.0;   // 1 degree tolerance

    // Gain Constants (P-Control)
    private static final double SPEED_GAIN = 0.80;  // Forward speed gain
    private static final double STRAFE_GAIN = 0.80; // Strafe speed gain
    private static final double TURN_GAIN = 0.025;  // Turn speed gain (per degree tx)

    // Power Limits
    private static final double MAX_AUTO_SPEED = 0.40;
    private static final double MAX_AUTO_STRAFE = 0.40;
    private static final double MAX_AUTO_TURN = 0.30;

    private void initializeMotors() {
        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");

        // Set motor directions matching robot configuration
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }

    private void stopMotors() {
        frontLeftMotor.setPower(0);
        backLeftMotor.setPower(0);
        frontRightMotor.setPower(0);
        backRightMotor.setPower(0);
    }

    /**
     * Drives the mecanum robot with 3-DOF vectors: forward (y), strafe (x), and turn (rx).
     */
    private void driveMecanum(double forward, double strafe, double turn) {
        double denominator = Math.max(Math.abs(forward) + Math.abs(strafe) + Math.abs(turn), 1.0);

        double frontLeftPower = (forward + strafe + turn) / denominator;
        double backLeftPower = (forward - strafe + turn) / denominator;
        double frontRightPower = (forward - strafe - turn) / denominator;
        double backRightPower = (forward + strafe - turn) / denominator;

        frontLeftMotor.setPower(frontLeftPower);
        backLeftMotor.setPower(backLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backRightMotor.setPower(backRightPower);
    }

    @Override
    public void runOpMode() throws InterruptedException {
        initializeMotors();

        // Initialize Limelight 3A
        limelight = hardwareMap.get(Limelight3A.class, "limeLight");

        // Initialize Control Hub IMU
        imu = hardwareMap.get(IMU.class, "imu");
        RevHubOrientationOnRobot hubOrientation = new RevHubOrientationOnRobot(
                RevHubOrientationOnRobot.LogoFacingDirection.UP,
                RevHubOrientationOnRobot.UsbFacingDirection.FORWARD);
        imu.initialize(new IMU.Parameters(hubOrientation));

        // Configure Limelight Pipeline 0 (AprilTag pipeline)
        limelight.pipelineSwitch(0);

        telemetry.addLine("Initialized - Limelight 3A + IMU Auto Align");
        telemetry.addLine("Hold Gamepad1 A or Left Bumper to Auto Align to Tag");
        telemetry.update();

        waitForStart();

        if (isStopRequested()) {
            return;
        }

        limelight.start();

        try {
            while (opModeIsActive()) {
                // 1. Get current robot orientation from IMU and update Limelight (for MegaTag2)
                YawPitchRollAngles angles = imu.getRobotYawPitchRollAngles();
                double robotYaw = angles.getYaw(AngleUnit.DEGREES);
                limelight.updateRobotOrientation(robotYaw);

                // 2. Fetch latest vision result from Limelight
                LLResult result = limelight.getLatestResult();
                boolean targetFound = false;

                int tagId = -1;
                double tagX = 0;      // Lateral offset in meters (right is positive)
                double tagZ = 0;      // Forward depth in meters
                double tx = 0;        // Horizontal angle offset in degrees

                if (result != null && result.isValid() && !result.getFiducialResults().isEmpty()) {
                    LLResultTypes.FiducialResult tag = result.getFiducialResults().get(0);
                    Pose3D cameraToTag = tag.getTargetPoseCameraSpace();

                    if (cameraToTag != null) {
                        targetFound = true;
                        tagId = tag.getFiducialId();
                        tagX = cameraToTag.getPosition().x;
                        tagZ = cameraToTag.getPosition().z;
                        tx = result.getTx();
                    }
                }

                // 3. Telemetry status display
                telemetry.addData("IMU Yaw", "%.1f deg", robotYaw);
                if (targetFound) {
                    telemetry.addData("AprilTag ID", tagId);
                    telemetry.addData("Distance (Z)", "%.3f m", tagZ);
                    telemetry.addData("Strafe Offset (X)", "%.3f m", tagX);
                    telemetry.addData("Heading Angle (tx)", "%.2f deg", tx);
                } else {
                    telemetry.addLine("No AprilTag detected");
                }

                // 4. Drive Logic: Automatic Alignment or Manual Driver Control
                boolean autoAlignRequested = gamepad1.a || gamepad1.left_bumper;

                if (autoAlignRequested) {
                    if (targetFound) {
                        double distanceError = Math.abs(tagZ) - DESIRED_DISTANCE_METERS;
                        double strafeError = tagX;
                        double turnError = tx;

                        double drivePower = 0;
                        double strafePower = 0;
                        double turnPower = 0;

                        if (Math.abs(distanceError) > DISTANCE_TOLERANCE_METERS) {
                            drivePower = Range.clip(distanceError * SPEED_GAIN, -MAX_AUTO_SPEED, MAX_AUTO_SPEED);
                        }

                        if (Math.abs(strafeError) > STRAFE_TOLERANCE_METERS) {
                            strafePower = Range.clip(strafeError * STRAFE_GAIN, -MAX_AUTO_STRAFE, MAX_AUTO_STRAFE);
                        }

                        if (Math.abs(turnError) > ANGLE_TOLERANCE_DEGREES) {
                            turnPower = Range.clip(turnError * TURN_GAIN, -MAX_AUTO_TURN, MAX_AUTO_TURN);
                        }

                        driveMecanum(drivePower, strafePower, turnPower);

                        boolean distanceAligned = Math.abs(distanceError) <= DISTANCE_TOLERANCE_METERS;
                        boolean strafeAligned = Math.abs(strafeError) <= STRAFE_TOLERANCE_METERS;
                        boolean turnAligned = Math.abs(turnError) <= ANGLE_TOLERANCE_DEGREES;

                        if (distanceAligned && strafeAligned && turnAligned) {
                            telemetry.addLine(">>> TARGET ALIGNED & IN POSITION <<<");
                        } else {
                            telemetry.addData("Auto Aligning", "Drive: %.2f, Strafe: %.2f, Turn: %.2f",
                                    drivePower, strafePower, turnPower);
                        }
                    } else {
                        stopMotors();
                        telemetry.addLine("Auto align active - Searching for tag...");
                    }
                } else {
                    // Manual teleop drive when auto align button is not held
                    double forward = -gamepad1.left_stick_y;
                    double strafe = gamepad1.left_stick_x * 1.1;
                    double turn = gamepad1.right_stick_x;

                    driveMecanum(forward, strafe, turn);
                    telemetry.addLine("Manual Driving Mode (Hold A or LB to Auto Align)");
                }

                telemetry.update();
                sleep(20);
            }
        } finally {
            stopMotors();
            limelight.stop();
        }
    }
}
