package utils;

public final class Debug {

    private static boolean ENABLED = true;

    public static boolean getEnabled() {
        return ENABLED;
    }

    public static void setEnabled(boolean value) {
        ENABLED = value;
    }

    // log methods
    // only print when ENABLED = true (using System.out)
    public static void log(String msg) {
        if (ENABLED) {
            System.out.print("[DEBUG] " + msg);
        }
    }

    public static void log(String msg, Object... args) {
        if (ENABLED) {
            System.out.printf("[DEBUG] " + msg, args);
        }
    }

    public static void log(Object obj) {
        if (ENABLED) {
            System.out.print("[DEBUG] " + obj);
        }
    }

    public static void logln(String msg) {
        if (ENABLED) {
            System.out.println("[DEBUG] " + msg);
        }
    }

    public static void logln(String msg, Object... args) {
        if (ENABLED) {
            System.out.printf("[DEBUG] " + msg + "%n", args);
        }
    }

    public static void logln(Object obj) {
        if (ENABLED) {
            System.out.println("[DEBUG] " + obj);
        }
    }

    // print methods
    // always print (using System.out)
    public static void print(String msg) {
        System.out.print(msg);
    }

    public static void print(String msg, Object... args) {
        System.out.printf(msg, args);
    }

    public static void print(Object obj) {
        System.out.print(obj);
    }

    public static void println(String msg) {
        System.out.println(msg);
    }

    public static void println(String msg, Object... args) {
        System.out.printf(msg + "%n", args);
    }

    public static void println(Object obj) {
        System.out.println(obj);
    }

    // error methods
    // always print (using System.err)
    public static void err(String msg) {
        System.err.print("[ERROR] " + msg);
    }

    public static void err(String msg, Object... args) {
        System.err.printf("[ERROR] " + msg, args);
    }

    public static void err(Object obj) {
        System.err.print("[ERROR] " + obj);
    }

    public static void errln(String msg) {
        System.err.println("[ERROR] " + msg);
    }

    public static void errln(String msg, Object... args) {
        System.err.printf("[ERROR] " + msg + "%n", args);
    }

    public static void errln(Object obj) {
        System.err.println("[ERROR] " + obj);
    }

}
