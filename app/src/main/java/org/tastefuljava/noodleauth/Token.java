package org.tastefuljava.noodleauth;

public class Token {
    private final Account account;
    private String code;
    private long time;
    private long endValidity = Long.MIN_VALUE;

    public Token(Account account) {
        this.account = account;
        tic(System.currentTimeMillis());
    }

    public Account getAccount() {
        return account;
    }

    public String getCode() {
        return code;
    }

    public double getAngle() {
        double secs = (endValidity-time)/1000.0;
        return 360.0*secs/account.getValidity();
    }

    public final void tic(long time) {
        this.time = time;
        if (time >= endValidity) {
            code = account.generateOTP(time);
            endValidity = account.getEndValidity(time);
        }
    }
}
