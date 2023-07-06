package MVPTree;

import entity.MVPError;

public class MVPErrorMsg {
    private static final String[] ERROR_MSGS = {
            "no error",
            "bad argument",
            "no distance function found",
            "mem alloc error",
            "no leaf node created",
            "no internal node created",
            "no path array alloc'd",
            "could not select vantage points",
            "could not calculate range from an sv1",
            "could not calculate range from an sv2",
            "points too compact",
            "could not sort points",
            "could not open file",
            "could not close file",
            "mmap error",
            "unmap error",
            "no write",
            "could not extend file",
            "could not remap file",
            "datatypes in conflict",
            "no. retrieved exceeds k",
            "empty tree",
            "distance value either NaN or less than zero",
            "could not open file",
            "unrecognized node"
    };

    public static String getErrorMsg(MVPError err) {
        int errorCode = err.ordinal();
        if (errorCode >= 0 && errorCode < ERROR_MSGS.length) {
            return ERROR_MSGS[errorCode];
        } else {
            return "Unknown error";
        }
    }
}
