package com.jvj28.homeworks.command;

public interface HomeworksCommand {
    /**
     * Name of the command
     * @return a String representing the name of this command
     */
    String getName();

    /**
     * Constructs the command to send to the Homeworks Processor
     * @return Command to send to Processor
     */
    String getCommand();

    /**
     * The processor will return one or more lines as a response to the
     * command.  This method is called for each line received by the processor
     * Blank lines are not sent to this method.
     * @param line String of data (a line) returned by the processor
     */
    void parseLine(String line);
}
