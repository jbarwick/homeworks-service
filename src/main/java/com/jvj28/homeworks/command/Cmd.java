package com.jvj28.homeworks.command;

import java.util.stream.Stream;

public enum Cmd {
    FADEDIM("FADEDIM","Fade a Dimmer (RPM or Vareo)", FadeDimmer.class),
    RAISEDIM("RAISEDIM","Raise a Dimmer (RPM or Vareo)", RaiseDimmer.class),
    LOWERDIM("LOWERDIM", "Lower a Dimmer (RPM or Vareo)", LowerDimmer.class),
    STOPDIM("STOPDIM", "Stop a Dimmer (RPM or Vareo)", StopDimmer.class),
    FLASHDIM("FLASHDIM", "Flash a Dimmer (RPM or Vareo)", FlashDimmer.class),
    STOPFLASH("STOPFLASH", "Stop a Dimmer Flashing (RPM or Vareo)", StopFlash.class),
    KBP("KBP", "Keypad button press", KeypadButtonPress.class),
    KBR("KBR", "Keypad button release", KeypadButtonRelease.class),
    KBDT("KBDT", "Keypad button double tap", KeypadButtonDoubleTap.class),
    KBH("KBH", "Keypad button hold", KeypadButtonHold.class),
    DBP("DBP", "Dimmer button press", DimmerButtonPress.class),
    DBR("DBR", "Dimmer button release", DimmerButtonRelease.class),
    DBDT("DBDT", "Dimmer button double tap", DimmerButtonDoubleTap.class),
    DBH("DBH", "Dimmer button hold", DimmerButtonHold.class),
    KBSS("KBSS", "Keypad button Scene Saver", KeypadButtonSceneSaver.class),
    KBSR("KBSR", "Keypad button Scene Restore", KeypadButtonSceneRestore.class),
    ST("ST", "Set system time", SetSystemTime.class),
    SD("SD", "Set system date", SetSystemdDate.class),
    SDS("SDS", "Set Daylight Savings", SetDaylightSavings.class),
    TCE("TCE", "Enable timeclock", EnableTimeclock.class),
    TCD("TCD", "Disable timeclock", DisableTimeclock.class),
    TCS("TCS", "Request timeclock state", RequestTimeclockState.class),
    CHKBATT("CHKBATT", "Check the external battery connection", CheckExternalBatteryConnection.class),
    RST("RST", "Request system time", RequestSystemTime.class),
    RST2("RST2", "Request system time with seconds", RequestSystemTimeWithSeconds.class),
    RSD("RSD", "Request system date", RequestSystemDate.class),
    RDS("RDS", "Request Daylight Savings info", RequestDaylightSavingsInformation.class),
    KE("KE", "Keypad enable", KeypadEnable.class),
    KD("KD", "Keypad disable", KeypadDisable.class),
    VMR("VMR", "Vacation mode record", VacationModeRecord.class),
    VMP("VMP", "Vacation mode playback", VacationModePlayback.class),
    VMD("VMD", "Vacation mode disable", VacationModeDisable.class),
    VMS("VMS", "Request vacation mode state", RequestVacationModeState.class),
    VRS("VRS", "Check the completeness of vacation mode records", VacationModeCheck.class),
    GSS("GSS", "Grafik Eye Scene Select", GrafikEyeSceneSelect.class),
    GRXFLASH("GRXFLASH", "Flash the zones on a grx main unit", GrxZoneFlash.class),
    STARTLNKS("STARTLNKS", "Start Links", StartLinks.class),
    STOPLNKS("STOPLNKS", "Stop Links", StopLinks.class),
    SETLED("SETLED", "Set a keypad led state", SetLED.class),
    SETLEDS("SETLEDS", "Set a whole keypad's led states", SetLEDs.class),
    KBMON("KBMON", "Enable keypad button monitoring", EnableKeypadButtonMonitoring.class),
    KBMOFF("KBMOFF", "Disable keypad button monitoring", DisableKeypadButtonMonitoring.class),
    DLMON("DLMON", "Enable dimmer level monitoring", EnableDimmerLevelMonitoring.class),
    DLMOFF("DLMOFF", "Disable dimmer level monitoring", DisableDimmerLevelMonitoring.class),
    DRMON("DRMON", "Enable driver monitoring", EnableDriverLevelMonitoring.class),
    DRMOFF("DRMOFF", "Disable driver level monitoring", DisableDriverLevelMonitoring.class),
    GSMON("GSMON", "Enable GrafikEye scene monitoring", EnableGrafikEyeSceneMonitoring.class),
    GSMOFF("GSMOFF", "Disable GrafikEye scene monitoring", DisableGrafikEyeSceneMonitoring.class),
    KLMON("KLMON", "Enable keypad led monitoring", EnableKeypadLEDMonitoring.class),
    KLMOFF("KLMOFF", "Disable keypad led monitoring", DisableKeypadLEDMonitoring.class),
    OSREV("OSREV", "Request the O/S revisions", OSRevision.class),
    BOOTREV("BOOTREV", "Request the boot revisions", RequestBootRevisions.class),
    LNKSHRT("LNKSHRT", "Request the link short status", RequestLinkShortStatus.class),
    PROCADDR("PROCADDR", "Print processor address", ProcessorAddress.class),
    SUNRISE("SUNRISE", "Request today's sunrise time", TodaysSunrise.class),
    SUNSET("SUNSET", "Request today's sunset time", TodaysSunset.class),
    PROMPTON("PROMPTON", "Turn on prompt", PromptOn.class),
    PROMPTOFF("PROMPTOFF", "Turn off prompt", PromptOff.class),
    RDL("RDL", "Request an zone level", RequestZoneLevel.class),
    REPLYON( "REPLYON", "Echo Replies", ReplyOn.class),
    RKLS("RKLS", "Request a keypad's led states", Unknown.class),
    RGS("RGS", "Request an GRX scene", Unknown.class),
    RKES("RKES", "Request a keypads state", Unknown.class),
    RKLBP("RKLBP", "Request the last button pressed on a keypad", Unknown.class),
    SETBAUD("SETBAUD", "Set the RS232 port baud rate", Unknown.class),
    GETBAUD("GETBAUD", "Get the RS232 port baud rate", Unknown.class),
    SMB("SMB", "Security mode begin", Unknown.class),
    SMT("SMT", "Security mode terminate", Unknown.class),
    SMS("SMS", "Security mode state", Unknown.class),
    CCOPULSE("CCOPULSE", "Pulse a CCO relay", Unknown.class),
    CCOCLOSE("CCOCLOSE", "Close a CCO relay", Unknown.class),
    CCOOPEN("CCOOPEN", "Open a CCO relay", Unknown.class),
    LOGIN("LOGIN", "Login to the processor", Login.class),
    LOGOUT("LOGOUT", "Logout the connected processor or all processors", Logout.class),
    SETHAND("SETHAND", "Set port handshaking type", Unknown.class),
    GETHAND("GETHAND", "Get port handshaking type", Unknown.class),
    RESET232("RESET232", "Reset the 232 port to defined parameters", Unknown.class),
    STOPSEQ("STOPSEQ", "Stop All Sequences Running and run terminate", Unknown.class),
    KILLSEQ("KILLSEQ", "Kill All Sequences Running", Unknown.class),
    TEMON("TEMON", "Enable Timed Event monitoring", EnableTimedEventMonitoring.class),
    TEMOFF("TEMOFF", "Disable Timed Event monitoring", DisableTimedEventMonitoring.class),
    SSB("SSB", "Begin Scene Saving Mode", Unknown.class),
    SST("SST", "Terminate Scene Saving Mode", Unknown.class),
    SSS("SSS", "Check the Scene Saving State", Unknown.class),
    SVSS("SVSS", "Sivoia Select Scene", Unknown.class),
    RSVS("RSVS", "Request the Sivoia Scene", Unknown.class),
    HELP("HELP", "Displays this help screen", Help.class),
    GDQA("GDQA", "Get Device QED Address", Unknown.class),
    DMS("DMS", "Database download state", Unknown.class),
    SENDDB("SENDDB", "Sends a DB to device", Unknown.class),
    SYNCDB("SYNCDB", "Sends DBs to all devices on a processor", Unknown.class),
    RETRYDB("RETRYDB", "Sends DBs to devices with errors on a processor", Unknown.class),
    RFDBSTAT("RFDBSTAT", "Prints which devices has an invalid DB", Unknown.class),
    DEVINFO("DEVINFO", "Get device information", Unknown.class),
    GTI("GTI", "Get translator subnet address and frequency", Unknown.class),
    REBOOT("REBOOT", "Forces a processor reset", Unknown.class),
    WIGGLEON("WIGGLEON", "Wiggles a QED shade", Unknown.class),
    WIGGLEOFF("WIGGLEOFF", "Stops a QED shade from wiggling", Unknown.class),
    QEDADDR("QEDADDR", "Enter QED Addressing mode", Unknown.class),
    GQR("GQR", "Get QED Revisions", Unknown.class),
    QEDMOOB("QEDMOOB", "QED Massive Out-Of-Box", Unknown.class),
    QEDGTT("QEDGTT", "QED Get Translation Table", Unknown.class),
    QEDPTT("QEDPTT", "QED Print Translation Table", Unknown.class),
    QEDCOMTEST("QEDCOMTEST", "Test communication for a QED", Unknown.class),
    CLRCOMTEST("CLRCOMTEST", "Clear comm test for a QED", Unknown.class),
    RFTEST("RFTEST", "Test rf integrity", Unknown.class),
    PING("PING", "Ping another ethernet device", Ping.class),
    SETPING("SETPING", "Enables or disables ping responses", SetPing.class),
    SETDHCP("SETDHCP", "Enables or disables a dynamic IP address", SetDHCP.class),
    NETSTAT("NETSTAT", "Prints the ethernet network statistics", Netstat.class),
    PINFO("PINFO", "Request all processor status information", RequestAllProcessorStatusInformation.class),
    QUIT("QUIT", "Quit a telnet session", Quit.class),
    CLEARIP("CLEARIP", "Clears the processors ip address", Unknown.class),
    SETIP("SETIP", "Sets the processors ip address", Unknown.class),
    RELEASEIP("RELEASEIP", "Releases an IP address obtained by DHCP", Unknown.class),
    RENEWIP("RENEWIP", " Renews an IP address obtained by DHCP", Unknown.class),
    SETSUBNET("SETSUBNET", "Sets the processors subnet mask", Unknown.class),
    SETGATEWAY("SETGATEWAY", "Sets the processors gateway address", Unknown.class),
    SETTELNET("SETTELNET", "Sets the processors telnet port", Unknown.class),
    SETFTP("SETFTP", "Sets the processors ftp port", Unknown.class),
    SETHTTP("SETHTTP", "Sets the processors http port", Unknown.class),
    SETNET("SETNET", "Sets the processors network info", Unknown.class),
    SETAMX("SETAMX", "Enables or disables the AMX beacon", Unknown.class),
    SETNAME("SETNAME", "Sets the processor's NetBIOS name", Unknown.class),
    ENABLENAME("ENABLENAME", "Enables one of the processor's NetBIOS names", Unknown.class),
    RELNAME("RELNAME", "Releases one of the processor's NetBIOS names", Unknown.class);

    private final String name;
    private final String description;
    private final Class<? extends HomeworksCommand> clazz;

    Cmd(String name, String s, Class<? extends HomeworksCommand> c) {
        this.name = name;
        this.description = s;
        this.clazz = c;
    }

    public Class<? extends HomeworksCommand> commandClass() {
        return this.clazz;
    }

    public String description() {
        return this.description;
    }

    @Override
    public String toString() {
        return this.name;
    }

    public String toJson() {
        return String.format("{\"Name\": \"%s\", \"Description\": \"%s\"}", this.name, this.description);
    }

    public static Stream<Cmd> stream() {
        return Stream.of(Cmd.values());
    }
}
