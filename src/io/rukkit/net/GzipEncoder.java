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
        this.stream = new DataOutputStream((OutputStream)this.buffer);
    }
}
