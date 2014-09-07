package org.usvo.openid.util;

import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;

public class IOKit {
    public static void close(OutputStream out) {
        if (out != null)
            //noinspection EmptyCatchBlock
            try { out.close(); } catch (IOException e) { }
    }

    public static void close(InputStream in) {
        if (in != null)
            //noinspection EmptyCatchBlock
            try { in.close(); } catch (IOException e) { }
    }
}
