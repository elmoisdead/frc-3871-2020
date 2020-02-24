package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.hal.sim.mockdata.RoboRioDataJNI;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.buttons.Button;
import edu.wpi.first.wpilibj.networktables.*;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.SerialPort;

public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "aaaaaa";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  Timer time = new Timer();
  SerialPort comm = new SerialPort(19200, SerialPort.Port.kUSB);
  Joystick j1 = new Joystick(0);
  Joystick j2 = new Joystick(1);

  CANSparkMax m1 = new CANSparkMax(1, MotorType.kBrushless);
  CANSparkMax m2 = new CANSparkMax(2, MotorType.kBrushless);
  CANSparkMax m3 = new CANSparkMax(3, MotorType.kBrushless);
  CANSparkMax m4 = new CANSparkMax(4, MotorType.kBrushless);
  CANSparkMax m5 = new CANSparkMax(5, MotorType.kBrushless);
  CANSparkMax m6 = new CANSparkMax(6, MotorType.kBrushless);

  VictorSP g1 = new VictorSP(0);
  VictorSP g2 = new VictorSP(1);
  Compressor c = new Compressor(0);
  Solenoid s1 = new Solenoid(0);
  Solenoid s2 = new Solenoid(1);
  Solenoid s3 = new Solenoid(2);
  Solenoid s4 = new Solenoid(3);

  // double x;
  // double y;
  // double t;
  // int dpad;

  double something;
  double autoStopDist = 15;

  // boolean b = false;
  boolean b1 = false;
  boolean debounce = false;

  int grabDist() { //My function for our arduino sensor platform
    comm.writeString("i\n"); //ask Arduino for sensor
    Timer.delay(.025); // wait for Arduino
    final int dst;
    final String unparsed = comm.readString(); //Read what the Arduino sent us

    if (unparsed.matches("^[0-9]+$")) { //make sure its a Number, or else bad things happen
      dst = Integer.parseInt(unparsed); // String to Integer
    } else {
      dst = 0; //default to 0 if something goes wrong
    }
    // System.out.println(dst);
    return dst;
  }

  public void drive(double s, double t, double rr) { //Drive function so we dont have to type the same thing six times, Works like `drive([speed],[turn],[ramp rate])`
    m1.setIdleMode(CANSparkMax.IdleMode.kCoast);
    m2.setIdleMode(CANSparkMax.IdleMode.kCoast);
    m3.setIdleMode(CANSparkMax.IdleMode.kCoast);
    m4.setIdleMode(CANSparkMax.IdleMode.kCoast);
    m5.setIdleMode(CANSparkMax.IdleMode.kCoast);
    m6.setIdleMode(CANSparkMax.IdleMode.kCoast);

    m1.setOpenLoopRampRate(rr);
    m2.setOpenLoopRampRate(rr);
    m3.setOpenLoopRampRate(rr);
    m4.setOpenLoopRampRate(rr);
    m5.setOpenLoopRampRate(rr);
    m6.setOpenLoopRampRate(rr);
    m1.set(-s + t);
    m3.set(-s + t);
    m5.set(-s + t);
    m2.set(s + t);
    m4.set(s + t);
    m6.set(s + t);
  }

  public void driveBrake(double s, double t, double rr) { //same thing as above, but uses kBrake instead of kCoast
    m1.setIdleMode(CANSparkMax.IdleMode.kBrake);
    m2.setIdleMode(CANSparkMax.IdleMode.kBrake);
    m3.setIdleMode(CANSparkMax.IdleMode.kBrake);
    m4.setIdleMode(CANSparkMax.IdleMode.kBrake);
    m5.setIdleMode(CANSparkMax.IdleMode.kBrake);
    m6.setIdleMode(CANSparkMax.IdleMode.kBrake);

    m1.setOpenLoopRampRate(rr);
    m2.setOpenLoopRampRate(rr);
    m3.setOpenLoopRampRate(rr);
    m4.setOpenLoopRampRate(rr);
    m5.setOpenLoopRampRate(rr);
    m6.setOpenLoopRampRate(rr);
    m1.set(-s + t);
    m3.set(-s + t);
    m5.set(-s + t);
    m2.set(s + t);
    m4.set(s + t);
    m6.set(s + t);
  }

  public void up() { //dump
    s3.set(false);
    s4.set(true);
  }

  public void down() { //dump'nt
    s3.set(true);
    s4.set(false);
  }

  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("low", kDefaultAuto);
    m_chooser.addOption("high", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    down(); //dunno if works, but here to make sure
  }

  @Override
  public void robotPeriodic() {
  }

  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();

    System.out.println("Auto selected: " + m_autoSelected);

    grabDist(); //sometimes the distance measurement can be offset by one, so if we run auto after we have run it before, it will immediately dump. this is a bodgy way of preventing that.
    while (something != autoStopDist) { // while we are not in range, do:
      something = grabDist(); //get distance to wall
      final double spd = ((something - autoStopDist)/200); //figure out the difference so we know where to stop and divide by 200 so we don't sanic into the wall and break everything
      System.out.println(spd); //debug
      driveBrake(Math.max(Math.min(spd, .075), -.075), 0, 0); //drive + add a min/max so the robot doesn't go too fast when we are far away/too close
    }
    something = 0; //set variable to 0 so we don't immediately stop driving if we do auto twice.
    driveBrake(0, 0, 0); //stop
    Timer.delay(2); // wait 2 sec
    up(); //dump
    Timer.delay(3); // wait 3 sec
    down(); //undump
  }

  @Override
  public void autonomousPeriodic() {

  }

  @Override
  public void teleopPeriodic() { 
    final double x = j1.getX();
    final double y = j1.getY();
    final double t = j1.getRawAxis(2);
    final double t2 = j1.getRawAxis(3);
    final boolean b = j1.getRawButton(2);
    // final boolean boot = j1.getRawButton(3);
    final int dpad = j1.getPOV();

    if (b && !debounce && !b1) { //debouce to keep the solenoid from ocillating when the button is held down
      b1 = true;
      debounce = true;
    } else if (!b && debounce) {
      debounce = false;
    } else if (b && !debounce && b1) {
      debounce = true;
      b1 = false;
    }
    if (!b1) {  //gear shifting
      s2.set(true);
      s1.set(false);
      drive(-y, x * .25, 0.2);    // --------
    } else {                      //        |
      s2.set(false);              //        Different steering profiles for high/low gears
      s1.set(true);               //        |
      drive(-y, x * .125, 0.2);   // --------
    }

    switch (dpad) { //dpad control for winch
    case 0:
      g1.set(-.5);
      break;
    case 180:
      g1.set(.5);
      break;
    default:
      g1.set(0);
    }
    switch (dpad) { //dpad control for elevator
    case 90:
      g2.set(.75);
      break;
    case 270:
      g2.set(-.75);
      break;
    default:
      g2.set(0);
    }
    if (t > .8) {   //semi-auto for 180 turn
      drive(0, .3, .5);
      Timer.delay(.75);
      driveBrake(0, 0, 0);
    }
    if (t2 > .8) { // control for dooomper
      up();
    } else {
      down();
    }

  }

  @Override
  public void testPeriodic() {
  }

}
//hi