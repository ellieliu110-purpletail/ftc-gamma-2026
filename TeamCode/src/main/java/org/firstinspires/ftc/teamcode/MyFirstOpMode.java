package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;

@TeleOp
public class MyFirstOpMode extends OpMode {

    private CRServo myServo;
    private ColorSensor colorSensor;

    @Override
    public void init() {
        myServo = hardwareMap.get(CRServo.class, "rev_servo_0");
        colorSensor = hardwareMap.get(ColorSensor.class, "color_sensor_0");

        myServo.setPower(0.0);

        telemetry.addData("light sensor", colorSensor.alpha());
    }

    @Override
    public void loop() {
        telemetry.addData("light sensor", colorSensor.alpha());
        int lightIntensity = colorSensor.alpha();

        if (lightIntensity > 600){
            //turn a direction
            turnServo(false);
            telemetry.addData("direction", ">600");
        }
        else {
            //turn the other
            turnServo(true);
            telemetry.addData("direction", "<=600");
        }
    }

    private void turnServo(boolean direction){
        int lightIntensity = colorSensor.alpha();
        if (direction) {
            telemetry.addData("servo direction", direction);
            double position = myServo.getPower();
            position = position + 0.1;
            telemetry.addData("position", position);
            myServo.setPower(position);
        }
        else {
            telemetry.addData("servo direction", direction);
            double position = myServo.getPower();
            position = position - 0.1;
            telemetry.addData("position", position);
            myServo.setPower(position);
        }
    }
}
