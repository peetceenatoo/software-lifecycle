package com.polimi.PPP.CodeKataBattle.Utilities;

import org.apache.maven.shared.invoker.InvokerLogger;
import org.apache.maven.shared.invoker.PrintStreamLogger;
import org.apache.maven.shared.invoker.InvocationOutputHandler;

public class CustomLogger extends PrintStreamLogger {
    private StringBuilder output = new StringBuilder();

    public CustomLogger() {
        super(System.out, InvokerLogger.INFO);
    }

    @Override
    public void debug(String message) {
        super.debug(message);
        output.append("DEBUG: ").append(message).append("\n");
    }

    @Override
    public void info(String message) {
        super.info(message);
        output.append("INFO: ").append(message).append("\n");
    }

    @Override
    public void warn(String message) {
        super.warn(message);
        output.append("WARN: ").append(message).append("\n");
    }

    @Override
    public void error(String message) {
        super.error(message);
        output.append("ERROR: ").append(message).append("\n");
    }

    public String getOutput() {
        return output.toString();
    }
}

