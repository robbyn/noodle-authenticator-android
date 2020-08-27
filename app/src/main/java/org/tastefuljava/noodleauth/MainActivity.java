package org.tastefuljava.noodleauth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView accountListView;
    private AccountListAdapter accountListAdapter;

    private interface AccountHandler {
        void apply(String name, byte[] key, int otpLength, int validity);
    }

    private final Handler handler = new Handler();
    private Runnable refresh = new Runnable() {
        private long last = SystemClock.uptimeMillis();
        private long period = 1000;

        @Override
        public void run() {
            Log.i(TAG, "tic");
            accountListAdapter.notifyDataSetChanged();
            do {
                last += period;
            } while (last <= SystemClock.uptimeMillis());
            handler.postAtTime(this, last);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.add_account);
        fab.setOnClickListener(v-> accountDlg(v, null, this::addAccount));
        accountListView = findViewById(R.id.account_listview);
        accountListAdapter = new AccountListAdapter(this);
        accountListView.setAdapter(accountListAdapter);
        accountListView.setOnItemClickListener((a,view,i,id) -> {
            Account acc = accountListAdapter.getItem(i);
            assert acc != null;
            accountDlg(view, acc, (n, k, o, v) -> updateAccount(acc, n, k, o, v));
        });
        handler.post(refresh);
    }

    @SuppressLint("SetTextI18n")
    private void accountDlg(View view, Account acc, AccountHandler handler) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View dlgView = getLayoutInflater().inflate(R.layout.account_dialog,null);
        EditText txtName = (EditText)dlgView.findViewById(R.id.account_name);
        EditText txtKey = (EditText)dlgView.findViewById(R.id.account_key);
        EditText txtOtpLength = (EditText)dlgView.findViewById(R.id.account_otplength);
        EditText txtValidity = (EditText)dlgView.findViewById(R.id.account_validity);
        Button btnCancel = (Button)dlgView.findViewById(R.id.cancel);
        Button btnOkay = (Button)dlgView.findViewById(R.id.ok);
        if (acc != null) {
            txtName.setText(acc.getName());
            txtKey.setText(Codec.BASE32.encode(acc.getKey()));
            txtOtpLength.setText(Integer.toString(acc.getOtpLength()));
            txtValidity.setText(Integer.toString(acc.getValidity()));
        }
        builder.setView(dlgView);
        final AlertDialog alertDialog = builder.create();
        alertDialog.setCanceledOnTouchOutside(false);
        btnCancel.setOnClickListener((v) -> alertDialog.dismiss());
        btnOkay.setOnClickListener((v) -> {
             String name = txtName.getText().toString();
            byte[] key = Codec.BASE32.decode(txtKey.getText().toString());
            int otpLength = Integer.parseInt(txtOtpLength.getText().toString());
            int validity = Integer.parseInt(txtValidity.getText().toString());
            handler.apply(name, key, otpLength, validity);
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void addAccount(String name, byte[] key, int otpLength, int validity) {
        accountListAdapter.add(new Account(name, key, otpLength, validity));
        writeState();
    }

    private void updateAccount(Account acc, String name, byte[] key, int otpLength, int validity) {
        acc.setName(name);
        acc.setKey(key);
        acc.setOtpLength(otpLength);
        acc.setValidity(validity);
        accountListAdapter.notifyDataSetChanged();
        writeState();
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacks(refresh);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        readState();
        if (accountListAdapter.isEmpty()) {
            accountListAdapter.add(new Account("Digit",
                    Codec.BASE32.decode("MNYUWY2PIVYHAVCB"), 6, 30));
        }
    }

    private void readState() {
        SharedPreferences settings = getSharedPreferences(getString(R.string.prefs), MODE_PRIVATE);
        int count = settings.getInt(getString(R.string.account_count), 0);
        accountListAdapter.clear();
        for (int i = 0; i < count; ++i) {
            Account acc = readAccount(i, settings);
            accountListAdapter.add(acc);
        }
    }

    private Account readAccount(int i, SharedPreferences settings) {
        String pfx = getString(R.string.account_prefix);
        String name = settings.getString( pfx + i + getString(R.string.account_name), "");
        assert name != null;
        String key32 = settings.getString( pfx + i + getString(R.string.account_key), "");
        assert key32 != null;
        return new Account(name,
                Codec.BASE32.decode(key32),
                settings.getInt(pfx + i + getString(R.string.account_otplength), 6),
                settings.getInt(pfx + i + getString(R.string.account_validity), 30));
    }

    @Override
    protected void onStop() {
        super.onStop();
        writeState();
    }

    private void writeState() {
        SharedPreferences settings = getSharedPreferences(getString(R.string.prefs), MODE_PRIVATE);
        SharedPreferences.Editor edit = settings.edit();
        int count = 0;
        for (int i = 0; i < accountListAdapter.getCount(); ++i) {
            Account acc = accountListAdapter.getItem(i);
            if (acc != null) {
                writeAccount(count, acc, edit);
                edit.putInt(getString(R.string.account_count), ++count);
            }
        }
        edit.apply();
    }

    private void writeAccount(int i, Account acc, SharedPreferences.Editor edit) {
        String pfx = getString(R.string.account_prefix);
        edit.putString(pfx + i + getString(R.string.account_name), acc.getName());
        edit.putString(pfx + i + getString(R.string.account_key),
                Codec.BASE32.encode(acc.getKey()));
        edit.putInt(pfx + i + getString(R.string.account_otplength), acc.getOtpLength());
        edit.putInt(pfx + i + getString(R.string.account_validity), acc.getValidity());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}