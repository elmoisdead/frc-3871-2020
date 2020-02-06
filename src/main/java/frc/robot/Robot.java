/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

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


/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
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
  Compressor c=new Compressor(0);
  Solenoid s1 = new Solenoid(0);
  Solenoid s2 = new Solenoid(1);
  
  double x;
  double y;
  double t;
  private boolean m_LimelightHasValidTarget = false;
  private double m_LimelightDriveCommand = 0.0;
  private double m_LimelightSteerCommand = 0.0;
  private double ta;
  boolean b=false;
  boolean b1=false;
  boolean debounce=false;
  /**
   * This function is run when the robot is first started up and should be
   * used for any initialization code.
   */
  
  public void Update_Limelight_Tracking()
  {
        // These numbers must be tuned for your Robot!  Be careful!
        final double STEER_K = 0.005;                    // how hard to turn toward the target
        final double DRIVE_K = 0.26;                    // how hard to drive fwd toward the target
        final double DESIRED_TARGET_AREA = 0.7833;        // Area of the target when the robot reaches the wall
        final double MAX_DRIVE = 0.5;                   // Simple speed limit so we don't drive too fast

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

        // Start with proportional steering
        double steer_cmd = tx * STEER_K;
        m_LimelightSteerCommand = steer_cmd;

        // try to drive forward until the target area reaches our desired area
        double drive_cmd = (DESIRED_TARGET_AREA - ta) * DRIVE_K;

        // don't let the robot drive too fast into the goal
        if (drive_cmd > MAX_DRIVE)
        {
          drive_cmd = MAX_DRIVE;
        }
        m_LimelightDriveCommand = drive_cmd;
  }
  public void drive(double s,double t)
  {
    m1.set(s+t); m3.set(s+t); m5.set(s+t);
    m2.set(-s+t); m4.set(-s+t); m6.set(-s+t);
  }
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("low", kDefaultAuto);
    m_chooser.addOption("high", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);
    
    
  }

  /**
   * This function is called every robot packet, no matter the mode. Use
   * this for items like diagnostics that you want ran during disabled,
   * autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before
   * LiveWindow and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable
   * chooser code works with the Java SmartDashboard. If you prefer the
   * LabVIEW Dashboard, remove all of the chooser code and uncomment the
   * getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to
   * the switch structure below with additional strings. If using the
   * SendableChooser make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    
     m1.setOpenLoopRampRate(0);
    m2.setOpenLoopRampRate(0);
    m3.setOpenLoopRampRate(0);
    m4.setOpenLoopRampRate(0);
    m5.setOpenLoopRampRate(0);
    m6.setOpenLoopRampRate(0);
      Update_Limelight_Tracking();

        
      if (m_LimelightHasValidTarget)
      {
        s2.set(true);
        s1.set(false);
      drive(m_LimelightDriveCommand, m_LimelightSteerCommand);
      System.out.println(ta);

      }
      
      else
      {
        drive(0, 0);
      }       
    
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    m1.setOpenLoopRampRate(.5);
    m2.setOpenLoopRampRate(.5);
    m3.setOpenLoopRampRate(.5);
    m4.setOpenLoopRampRate(.5);
    m5.setOpenLoopRampRate(.5);
    m6.setOpenLoopRampRate(.5);
  x=j1.getX();
  y=j1.getY();
  t=j1.getRawAxis(2);
  b=j1.getRawButton(2);
  drive(y, x);

    if (b && !debounce && !b1)  {
      b1 = true;
      debounce = true;
    }
    else if (!b && debounce)
    {
      debounce=false;
    }
    else if (b && !debounce && b1)
    {
      debounce=true;
      b1=false;
    }
   if (b1)
    {
      s2.set(true);
      s1.set(false);
    } else {
      s2.set(false);
      s1.set(true);
    } 
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
  

}
