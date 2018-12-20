package org.mvnsearch.boot.oss;


import java.io.*;
import javax.activation.*;

/**
 * A DataSource backed by a byte array.  The byte array may be
 * passed in directly, or may be initialized from an InputStream
 * or a String.
 *
 * @author John Mani
 * @author Bill Shannon
 * @author Max Spivak
 * @since JavaMail 1.4
 */
public class ByteArrayDataSource implements DataSource {
    private byte[] data;    // data
    private int len = -1;
    private String type;    // content-type
    private String name = "";

    static class DSByteArrayOutputStream extends ByteArrayOutputStream {
        public byte[] getBuf() {
            return buf;
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * Create a ByteArrayDataSource with data from the
     * specified InputStream and with the specified MIME type.
     * The InputStream is read completely and the data is
     * stored in a byte array.
     *
     * @param is   the InputStream
     * @param type the MIME type
     * @throws IOException errors reading the stream
     */
    public ByteArrayDataSource(InputStream is, String type) throws IOException {
        DSByteArrayOutputStream os = new DSByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int len;
        while ((len = is.read(buf)) > 0) {
            os.write(buf, 0, len);
        }
        this.data = os.getBuf();
        this.len = os.getCount();

	/*
     * ByteArrayOutputStream doubles the size of the buffer every time
	 * it needs to expand, which can waste a lot of memory in the worst
	 * case with large buffers.  Check how much is wasted here and if
	 * it's too much, copy the data into a new buffer and allow the
	 * old buffer to be garbage collected.
	 */
        if (this.data.length - this.len > 256 * 1024) {
            this.data = os.toByteArray();
            this.len = this.data.length;    // should be the same
        }
        this.type = type;
    }

    /**
     * Create a ByteArrayDataSource with data from the
     * specified byte array and with the specified MIME type.
     *
     * @param data the data
     * @param type the MIME type
     */
    public ByteArrayDataSource(byte[] data, String type) {
        this.data = data;
        this.type = type;
    }

    /**
     * Create a ByteArrayDataSource with data from the
     * specified String and with the specified MIME type.
     * The MIME type should include a <code>charset</code>
     * parameter specifying the charset to be used for the
     * string.  If the parameter is not included, the
     * default charset is used.
     *
     * @param data the String
     * @param type the MIME type
     * @throws IOException errors reading the String
     */
    public ByteArrayDataSource(String data, String type) throws IOException {
        this.data = data.getBytes();
        this.type = type;
    }

    /**
     * Return an InputStream for the data.
     * Note that a new stream is returned each time
     * this method is called.
     *
     * @return the InputStream
     * @throws IOException if no data has been set
     */
    @Override
    public InputStream getInputStream() throws IOException {
        if (data == null) {
            throw new IOException("no data");
        }
        if (len < 0) {
            len = data.length;
        }
        return new ByteArrayInputStream(data, 0, len);
    }

    /**
     * Return an OutputStream for the data.
     * Writing the data is not supported; an <code>IOException</code>
     * is always thrown.
     *
     * @throws IOException always
     */
    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("cannot do this");
    }

    /**
     * Get the MIME content type of the data.
     *
     * @return the MIME type
     */
    @Override
    public String getContentType() {
        return type;
    }

    /**
     * Get the name of the data.
     * By default, an empty string ("") is returned.
     *
     * @return the name of this data
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the name of the data.
     *
     * @param name the name of this data
     */
    public void setName(String name) {
        this.name = name;
    }
}