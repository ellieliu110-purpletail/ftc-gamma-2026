package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

@TeleOp
public class MyFirstJavaRobot extends OpMode {

    CRServo servo0;
    ColorSensor colorSensor0;
    // double light;
    double power;
    DcMotor motor0;
    @Override
    public void init(){
        servo0 = hardwareMap.get(CRServo.class,"rev_servo_0");
        colorSensor0 = hardwareMap.get(ColorSensor.class, "color_sensor_0");
        servo0.setPower(0.0);
        motor0 = hardwareMap.get(DcMotor.class, "motor0");
    }

    @Override
    public void loop(){
/*       telemetry.addData("light", colorSensor0.alpha());
       light = colorSensor0.alpha() / 200.0;
       servo0.setPower(light);
       telemetry.addData("motor val", light);
       motor0.setPower(light);
       if (light < 0.5) {
           colorSensor0.enableLed(true);
       } else colorSensor0.enableLed(false); */
        motor0.setPower(gamepad1.left_stick_y);
        telemetry.addData("gamepad", gamepad1.left_stick_y);

        if (gamepad1.a){
            motor0.setPower(1);
        }

        if(gamepad1.b)
        {
            motor0.setPower(-1.0);
        }
    }
}
