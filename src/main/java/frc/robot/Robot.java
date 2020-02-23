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

  int grabDist() {
    comm.writeString("i\n");
    time.delay(.025);
    final int dst;
    final String unparsed = comm.readString();

    if (unparsed.matches("^[0-9]+$")) {
      dst = Integer.parseInt(unparsed);
    } else {
      dst = 0;
    }
    // System.out.println(dst);
    return dst;
  }

  public void drive(double s, double t, double rr) {
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

  public void driveBrake(double s, double t, double rr) {
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

  public void up() {
    s3.set(false);
    s4.set(true);
  }

  public void down() {
    s3.set(true);
    s4.set(false);
  }

  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("low", kDefaultAuto);
    m_chooser.addOption("high", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    down();
  }

  @Override
  public void robotPeriodic() {
  }

  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();

    System.out.println("Auto selected: " + m_autoSelected);

    grabDist();
    while (something != autoStopDist) {
      something = grabDist();
      final double spd = (something - autoStopDist);
      System.out.println(spd / 200);
      driveBrake(Math.max(Math.min(spd / 200, .075), -.075), 0, 0);
    }
    something = 0;
    driveBrake(0, 0, 0);
    time.delay(2);
    up();
    time.delay(3);
    down();
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

    if (b && !debounce && !b1) {
      b1 = true;
      debounce = true;
    } else if (!b && debounce) {
      debounce = false;
    } else if (b && !debounce && b1) {
      debounce = true;
      b1 = false;
    }
    if (!b1) {
      s2.set(true);
      s1.set(false);
      drive(-y, x * .25, 0.2);
    } else {
      s2.set(false);
      s1.set(true);
      drive(-y, x * .125, 0.2);
    }

    switch (dpad) {
    case 0:
      g1.set(-.5);
      break;
    case 180:
      g1.set(.5);
      break;
    default:
      g1.set(0);
    }
    switch (dpad) {
    case 90:
      g2.set(.75);
      break;
    case 270:
      g2.set(-.75);
      break;
    default:
      g2.set(0);
    }
    if (t > .8) {
      drive(0, .3, .5);
      time.delay(.75);
      driveBrake(0, 0, 0);
    }
    if (t2 > .8) {
      up();
    } else {
      down();
    }

  }

  @Override
  public void testPeriodic() {
  }

}
