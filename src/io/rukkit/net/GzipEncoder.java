package io.rukkit.net;

import java.io.*;
import java.util.zip.*;

public class GzipEncoder
{
	public String str;
    public ByteArrayOutputStream buffer;
    public DataOutputStream stream;

    public GzipEncoder() throws IOException {
        this.buffer = new ByteArrayOutputStream();
        BufferedOutputStream in = new BufferedOutputStream((OutputStream)new GZIPOutputStream((OutputStream)this.buffer));
        this.stream = new DataOutputStream((OutputStream)in);
    }
}
