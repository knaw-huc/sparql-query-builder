package org.uu.nl.goldenagents.util;

import java.util.Arrays;

/**
 * List of classes that should be filtered from the stack trace produced by SPRING errors
 */
public class StacktraceFilterElements {

    /**
     * Stack trace elements which start with this should be filtered if filter is enabled
     */
    public static final String[] FILTER_START = {
            "org.springframework.web",
            "javax.servlet.http.HttpServlet",
            "org.apache.catalina",
            "org.apache.tomcat",
            "org.springframework.boot",
            "org.apache.coyote",
            "jdk.internal.reflect",
            "java.lang.reflect",
            "java.util.concurrent",
            "java.lang.Thread"
    };

    /**
     * Stack trace elements that contian these strings should be filtered if filter is enabled
     */
    public static final String[] FILTER_CONTAINS = {

    };

    public static StackTraceElement[] filter(StackTraceElement[] toFilter) {
        return Arrays.stream(toFilter).filter(
                e -> Arrays.stream(FILTER_START).noneMatch(f -> e.getClassName().startsWith(f))
                    && Arrays.stream(FILTER_CONTAINS).noneMatch(f -> e.getClassName().contains(f))
            ).toArray(StackTraceElement[]::new);
    }

}
