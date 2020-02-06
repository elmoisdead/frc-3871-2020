package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.hal.sim.mockdata.RoboRioDataJNI;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.networktables.*;
import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import edu.wpi.first.wpilibj.VictorSP;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Solenoid;

public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "aaaaaa";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
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

  double x;
  double y;
  double t;
  private boolean m_LimelightHasValidTarget = false;
  private double m_LimelightDriveCommand = 0.0;
  private double m_LimelightSteerCommand = 0.0;
  private double ta;
  boolean b = false;
  boolean b1 = false;
  boolean debounce = false;

  public void Update_Limelight_Tracking() {

    final double STEER_K = 0.005;
    final double DRIVE_K = 0.26;
    final double DESIRED_TARGET_AREA = 0.7833;
    final double MAX_DRIVE = 0.5;

    double tv = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tv").getDouble(0);
    double tx = NetworkTableInstance.getDefault().getTable("limelight").getEntry("tx").getDouble(0);
    double ty = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ty").getDouble(0);
    ta = NetworkTableInstance.getDefault().getTable("limelight").getEntry("ta").getDouble(0);

    if (tv < 1.0)

    {
      m_LimelightHasValidTarget = false;
      m_LimelightDriveCommand = 0.0;
      m_LimelightSteerCommand = 0.0;
      return;
    }

    m_LimelightHasValidTarget = true;

    double steer_cmd = tx * STEER_K;
    m_LimelightSteerCommand = steer_cmd;

    double drive_cmd = (DESIRED_TARGET_AREA - ta) * DRIVE_K;

    if (drive_cmd > MAX_DRIVE) {
      drive_cmd = MAX_DRIVE;
    }
    m_LimelightDriveCommand = drive_cmd;
  }

  public void drive(double s, double t, double rr) {
    m1.setOpenLoopRampRate(rr);
    m2.setOpenLoopRampRate(rr);
    m3.setOpenLoopRampRate(rr);
    m4.setOpenLoopRampRate(rr);
    m5.setOpenLoopRampRate(rr);
    m6.setOpenLoopRampRate(rr);
    m1.set(s + t);
    m3.set(s + t);
    m5.set(s + t);
    m2.set(-s + t);
    m4.set(-s + t);
    m6.set(-s + t);
  }

  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("low", kDefaultAuto);
    m_chooser.addOption("high", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

  }

  @Override
  public void robotPeriodic() {
  }

  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();

    System.out.println("Auto selected: " + m_autoSelected);
  }

  @Override
  public void autonomousPeriodic() {
    Update_Limelight_Tracking();

    if (m_LimelightHasValidTarget) {
      s2.set(true);
      s1.set(false);
      drive(m_LimelightDriveCommand, m_LimelightSteerCommand, 0);
      System.out.println(ta);

    }

    else {
      drive(0, 0, 0);
    }

  }

  @Override
  public void teleopPeriodic() {
    x = j1.getX();
    y = j1.getY();
    t = j1.getRawAxis(2);
    b = j1.getRawButton(2);
    drive(y, x, .5);
    if (b && !debounce && !b1) {
      b1 = true;
      debounce = true;
    } else if (!b && debounce) {
      debounce = false;
    } else if (b && !debounce && b1) {
      debounce = true;
      b1 = false;
    }
    if (b1) {
      s2.set(true);
      s1.set(false);
    } else {
      s2.set(false);
      s1.set(true);
    }
  }

  @Override
  public void testPeriodic() {
  }

}
