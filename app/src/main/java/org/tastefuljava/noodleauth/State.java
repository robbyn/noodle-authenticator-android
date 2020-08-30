package org.tastefuljava.noodleauth;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import androidx.annotation.NonNull;

public class State {
    public static final int CURRENT_VERSION = 0;
    private int version = CURRENT_VERSION;
    private Account[] accounts = {};

    public @NonNull Account[] getAccounts() {
        return accounts.clone();
    }

    public void setAccounts(@NonNull Account[] accounts) {
        this.accounts = accounts.clone();
    }

    public void load(Context context) {
        SharedPreferences settings = getSettings(context);
        version = settings.getInt(context.getString(R.string.state_version), 0);
        int count = settings.getInt(context.getString(R.string.account_count), 0);
        Account[] newAccounts = new Account[count];
        for (int i = 0; i < count; ++i) {
            newAccounts[i] = readAccount(i, settings, context);
        }
        accounts = newAccounts;
    }

    public void store(Context context) {
        SharedPreferences settings = getSettings(context);
        Editor edit = settings.edit();
        edit.putInt(context.getString(R.string.state_version), CURRENT_VERSION);
        int count = 0;
        for (Account acc: accounts) {
            if (acc != null) {
                writeAccount(count, acc, edit, context);
                edit.putInt(context.getString(R.string.account_count), ++count);
            }
        }
        edit.apply();
    }

    private SharedPreferences getSettings(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.prefs), Context.MODE_PRIVATE);
    }

    private Account readAccount(int i, SharedPreferences settings, Context context) {
        String pfx = context.getString(R.string.account_prefix);
        String name = settings.getString( pfx + i + context.getString(R.string.account_name),
                "");
        assert name != null;
        String key32 = settings.getString( pfx + i + context.getString(R.string.account_key),
                "");
        assert key32 != null;
        return new Account(name,
                Codec.BASE32.decode(key32),
                settings.getInt(pfx + i + context.getString(R.string.account_otplength), 6),
                settings.getInt(pfx + i + context.getString(R.string.account_validity), 30));
    }

    private void writeAccount(int i, Account acc, Editor edit, Context context) {
        String pfx = context.getString(R.string.account_prefix);
        edit.putString(pfx + i + context.getString(R.string.account_name), acc.getName());
        edit.putString(pfx + i + context.getString(R.string.account_key),
                Codec.BASE32.encode(acc.getKey()));
        edit.putInt(pfx + i + context.getString(R.string.account_otplength), acc.getOtpLength());
        edit.putInt(pfx + i + context.getString(R.string.account_validity), acc.getValidity());
    }
}
