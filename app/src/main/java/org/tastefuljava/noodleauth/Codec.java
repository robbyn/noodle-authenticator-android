package org.tastefuljava.noodleauth;

public enum Codec {
    BASE16("0123456789ABCDEF"),
    BASE32("ABCDEFGHIJKLMNOPQRSTUVWXYZ234567");

    private final char[] digits;
    private final byte[] lookup;
    private final int shift;
    private final byte mask;

    Codec(String digits) {
        this.digits = digits.toCharArray();
        this.shift = Integer.bitCount(this.digits.length - 1);
        this.mask = (byte)((1 << shift)-1);
        this.lookup = createLookup(this.digits);
    }

    public boolean isValid(String s) {
        char[] chars = s.toCharArray();
        for (char c: chars) {
            if (c <= 0 || c > 127 || lookup[c] < 0) {
                return false;
            }
        }
        return true;
    }

    public String encode(byte[] bytes) {
        int inLen = bytes.length;
        if (inLen == 0) {
            return "";
        }

        int resultLen = (8*inLen + shift - 1) / shift;
        char[] result = new char[resultLen];

        int pos = 0;
        int b = 0;
        int ix = 0;
        int bits = 0;
        while (bits > 0 || ix < inLen) {
            if (bits < shift) {
                if (ix < inLen) {
                    b <<= 8;
                    b |= (bytes[ix++] & 0xff);
                    bits += 8;
                } else {
                    int pad = shift - bits;
                    b <<= pad;
                    bits += pad;
                }
            }
            int index = mask & (b >> (bits - shift));
            bits -= shift;
            result[pos++] = digits[index];
        }
        return new String(result, 0, pos);
    }

    public byte[] decode(String s) {
        char[] chars = s.toCharArray();
        int inLen = chars.length;
        int resultLen = (inLen*shift + 7) / 8;
        byte[] result = new byte[resultLen];
        int b = 0;
        int ix = 0;
        int bits = 0;
        for (char c: chars) {
            byte val;
            if (c <= 0 || c > 127 || (val = lookup[c]) < 0) {
                throw new IllegalArgumentException(
                        "Invalid digit: " + (int) c);
            }
            b <<= shift;
            b |= val & mask;
            bits += shift;
            if (bits >= 8) {
                result[ix++] = (byte) (b >> (bits - 8));
                bits -= 8;
            }
        }
        if (bits > 0) {
            result[ix] = (byte)(b & mask);
        }
        return result;
    }

    private static byte[] createLookup(char[] digits) {
        byte[] lookup = new byte[128];
        for (int i = 0; i < 128; ++i) {
            lookup[i] = -1;
        }
        for (int i = 0; i < digits.length; ++i) {
            char c = digits[i];
            if (c <= 0 || c > 127) {
                throw new IllegalArgumentException(
                        "Non ASCII digit: " + (int) c);
            }
            lookup[c] = (byte) i;
            if (Character.isUpperCase(c)) {
                lookup[Character.toLowerCase(c)] = (byte) i;
            } else if (Character.isLowerCase(c)) {
                lookup[Character.toUpperCase(c)] = (byte) i;
            }
        }
        return lookup;
    }
}
