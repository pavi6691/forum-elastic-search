package com.acme.poc.notes.restservice.util;

import java.util.Optional;


/**
 * Logging utility functions
 */
public class LogUtil {


    /**
     * Return the current method name from the calling method.
     *
     * @return Method name
     */
    public static String method() {
        StackWalker stackWalker = StackWalker.getInstance();
        Optional<String> methodName = stackWalker.walk(stackFrameStream -> stackFrameStream.skip(1).findFirst().map(StackWalker.StackFrame::getMethodName));
        return methodName.map(s -> s + "()").orElse("UNKNOWN");
    }

}
