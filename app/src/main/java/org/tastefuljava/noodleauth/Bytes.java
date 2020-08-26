package org.tastefuljava.noodleauth;

public class Bytes {
    private Bytes() {
    }

    public static byte[] charToBytes(char v) {
        return shortToBytes((short)v);
    }

    public static char bytesToChar(byte[] b) {
        return (char)bytesToShort(b);
    }

    public static byte[] shortToBytes(int v) {
        return new byte[] {
            (byte)(0xFF & (v >> 8)),
            (byte)(0xFF & v)};
    }

    public static short bytesToShort(byte[] b) {
        return bytesToShort(b, 0, b.length);
    }

    public static short bytesToShort(byte[] b, int offs, int len) {
        if (b == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        } else if (len != 2) {
            throw new IllegalArgumentException("Wrong data length " + len);
        } else if (offs < 0 || offs + len > b.length) {
            throw new IllegalArgumentException(
                    "Wrong offset/length " + offs + "/" + len);
        }
        int end = offs + len;
        int result = 0;
        for (int i = offs; i < end; ++i) {
            result <<= 8;
            result |= 0xFF & b[i];
        }
        return (short)result;
    }

    public static byte[] intToBytes(int v) {
        return new byte[] {
                (byte)(0xFF & (v >> 24)),
                (byte)(0xFF & (v >> 16)),
                (byte)(0xFF & (v >>    8)),
                (byte)(0xFF & v)};
    }

    public static int bytesToInt(byte[] b) {
        return bytesToInt(b, 0, b.length);
    }

    public static int bytesToInt(byte[] b, int offs, int len) {
        if (b == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        } else if (len != 4) {
            throw new IllegalArgumentException("Wrong data length " + len);
        } else if (offs < 0 || offs + len > b.length) {
            throw new IllegalArgumentException(
                    "Wrong offset/length " + offs + "/" + len);
        }
        int end = offs + len;
        int result = 0;
        for (int i = offs; i < end; ++i) {
            result <<= 8;
            result |= 0xFF & b[i];
        }
        return result;
    }

    public static byte[] longToBytes(long v) {
        return new byte[] {
                (byte)(0xFF & (v >> 56)),
                (byte)(0xFF & (v >> 48)),
                (byte)(0xFF & (v >> 40)),
                (byte)(0xFF & (v >> 32)),
                (byte)(0xFF & (v >> 24)),
                (byte)(0xFF & (v >> 16)),
                (byte)(0xFF & (v >>  8)),
                (byte)(0xFF & v)};
    }

    public static long bytesToLong(byte[] b) {
        return bytesToLong(b, 0, b.length);
    }

    public static long bytesToLong(byte[] b, int offs, int len) {
        if (b == null) {
            throw new IllegalArgumentException("Argument cannot be null");
        } else if (len != 8) {
            throw new IllegalArgumentException("Wrong data length " + len);
        } else if (offs < 0 || offs + len > b.length) {
            throw new IllegalArgumentException(
                    "Wrong offset/length " + offs + "/" + len);
        }
        int end = offs + len;
        long result = 0;
        for (int i = offs; i < end; ++i) {
            result <<= 8;
            result |= 0xFF & b[i];
        }
        return result;
    }

    public static byte[] floatToBytes(float v) {
        return intToBytes(Float.floatToIntBits(v));
    }

    public static float bytesToFloat(byte[] b) {
        return bytesToFloat(b, 0, b.length);
    }

    public static float bytesToFloat(byte[] b, int offs, int len) {
        return Float.intBitsToFloat(bytesToInt(b, offs, len));
    }

    public static byte[] doubleToBytes(double v) {
        return longToBytes(Double.doubleToLongBits(v));
    }

    public static double bytesToDouble(byte[] b) {
        return bytesToDouble(b, 0, b.length);
    }

    public static double bytesToDouble(byte[] b, int offs, int len) {
        return Double.longBitsToDouble(bytesToLong(b, offs, len));
    }

    public static boolean equals(byte[] a, int offsa, byte[] b, int offsb,
            int len) {
        if (a == null) {
            return b == null;
        } else if (offsa + len > a.length || offsb + len > b.length) {
            throw new IllegalArgumentException("Invalid offset/length");
        }
        for (int i = 0; i < len; ++i) {
            if (a[offsa+i] != b[offsb+i]) {
                return false;
            }
        }
        return true;
    }

    public static boolean equals(byte[] a, byte[] b) {
        if (a == null) {
            return b == null;
        } else if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; ++i) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }
}
