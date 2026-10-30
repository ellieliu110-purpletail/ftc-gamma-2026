package org.firstinspires.ftc.teamcode;



import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import java.util.Locale;

// tile is 23 1/2 inch square, one Ellie is equal to 23 1.2 inches
@TeleOp(name = "Pathfinder1", group = "Gamma")
    public class F_G_try extends LinearOpMode {
    boolean y_facing_forward = true;
    boolean y_facing_backward = false;
    boolean x_facing_forward = false;
    boolean x_facing_backward = false;

        private void initializeMotors()
        {
            int septagon;
            septagon = 47/2;
            pathfinding(1,0, 3, 2);
        }
    private DcMotor frontLeftMotor;


    private DcMotor backLeftMotor;
    private DcMotor frontRightMotor;
    private DcMotor backRightMotor;

    private void initializeMotorsDrive() {
        telemetry.addLine("Initializing Motors...");
        telemetry.update();


        frontLeftMotor = hardwareMap.dcMotor.get("frontLeftMotor");
        backLeftMotor = hardwareMap.dcMotor.get("backLeftMotor");
        frontRightMotor = hardwareMap.dcMotor.get("frontRightMotor");
        backRightMotor = hardwareMap.dcMotor.get("backRightMotor");

        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.FORWARD);
        frontRightMotor.setDirection(DcMotor.Direction.FORWARD);
        backRightMotor.setDirection(DcMotor.Direction.REVERSE);

        frontLeftMotor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);
        frontRightMotor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(
                DcMotor.ZeroPowerBehavior.BRAKE);

    }
    private void drive(double forward, double turn) {
        // Mecha-num forward + rotation

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
        if (frontLeftMotor != null && backLeftMotor != null && frontRightMotor != null && backRightMotor != null) {
        frontLeftMotor.setPower(0.0);
        backLeftMotor.setPower(0.0);
        frontRightMotor.setPower(0.0);
        backRightMotor.setPower(0.0);
        }
    }

        private void pathfinding(int x1, int y1, int xdes, int ydes){


            for(int i=x1; i<=(xdes-x1); i++) {
                telemetry.addLine(String.format(Locale.US, "Path: x=%d", i));
                if (x_facing_forward) {
                    drive(0.5,0);
                    x_facing_forward = true;

                }
                else if (x_facing_backward) {
                    drive(-0.5,0);
                    x_facing_backward = true;

                }
                else if (y_facing_forward) {
                    drive(0,0.5);
                    drive(0.5,0);
                    y_facing_forward = false;

                }
                else if (y_facing_backward) {
                    drive(0,-0.5);
                    drive(0.5,0);
                    y_facing_backward = false;

                }


            }

            for (int j = y1; j <= (ydes - y1); j++) {
                telemetry.addLine(String.format(Locale.US, "Path: Y=%d", j));


                stopMotors();
                if (y_facing_forward) {
                    drive(0.5,0);
                    y_facing_forward = true;

                }
                else if (y_facing_backward) {
                    drive(-0.5,0);
                    y_facing_backward = true;

                }
                else if (x_facing_forward) {
                    drive(0,0.5);
                    drive(0.5,0);
                    x_facing_forward = false;

                }
                else if (x_facing_backward) {
                    drive(0,-0.5);
                    drive(0.5,0);
                    x_facing_backward = false;

                }


            }
        }

        @Override public void runOpMode() throws InterruptedException {
            // Initialize hardware ONCE
            initializeMotorsDrive();
            initializeMotors();
            telemetry.update();
            waitForStart();
        }
    }

