package org.tastefuljava.noodleauth;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.SystemClock;
import android.text.InputFilter;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

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
        registerForContextMenu(accountListView);
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri data = intent.getData();
        if ("android.intent.action.VIEW".equals(action) && data != null) {
            handleUri(data);
        }
        handler.post(refresh);
    }

    private void handleUri(Uri uri) {
        if ("otpauth".equals(uri.getScheme())
                && "totp".equals(uri.getHost())) {
            String name = uri.getPath();
            if (name == null) {
                name = "";
            } else if (name.startsWith("/")) {
                name = name.substring(1);
            }
            byte[] key = {};
            String key32 = uri.getQueryParameter("secret");
            if (key32 != null) {
                key = Codec.BASE32.decode(key32);
            }
            String sotplen = uri.getQueryParameter("digits");
            int otplen = sotplen == null ? 6 : Integer.parseInt(sotplen);
            String svalidity = uri.getQueryParameter("period");
            int validity = svalidity == null ? 30 : Integer.parseInt(svalidity);
            Account acc = new Account(name, key, otplen, validity);
            accountDlg(findViewById(android.R.id.content), acc, this::addAccount);
        }
    }

    @SuppressLint("SetTextI18n")
    private void accountDlg(View view, Account acc, AccountHandler handler) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final View dlgView = getLayoutInflater().inflate(R.layout.account_dialog,null);
        EditText txtName = dlgView.findViewById(R.id.account_name);
        EditText txtKey = dlgView.findViewById(R.id.account_key);
        InputFilter[] editFilters = txtKey.getFilters();
        InputFilter[] newFilters = new InputFilter[editFilters.length + 1];
        System.arraycopy(editFilters, 0, newFilters, 0, editFilters.length);
        newFilters[editFilters.length] = new InputFilter.AllCaps();
        txtKey.setFilters(newFilters);
        EditText txtOtpLength = dlgView.findViewById(R.id.account_otplength);
        EditText txtValidity = dlgView.findViewById(R.id.account_validity);
        Button btnCancel = dlgView.findViewById(R.id.cancel);
        Button btnOkay = dlgView.findViewById(R.id.ok);
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
            try {
                String name = txtName.getText().toString();
                if (isBlank(name)) {
                    showError("Error", "Please enter a name for the account");
                    txtName.requestFocus();
                    return;
                }
                String keyStr = txtKey.getText().toString();
                if (isBlank(keyStr)) {
                    showError("Error", "Please enter a base32-encoded key");
                    txtKey.requestFocus();
                    return;
                }
                byte[] key = Codec.BASE32.decode(keyStr);
                int otpLength = Integer.parseInt(txtOtpLength.getText().toString());
                int validity = Integer.parseInt(txtValidity.getText().toString());
                handler.apply(name, key, otpLength, validity);
            } catch (Throwable ex) {
                showError(getString(R.string.error), ex.getMessage());
            }
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    private void showError(String title, String message) {
        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                (dialog, which) -> dialog.dismiss());
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
            accountListAdapter.add(new Account("Example",
                    Codec.BASE32.decode("ABCDEF3425"), 6, 30));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        writeState();
    }

    private void readState() {
        State state = new State();
        state.load(this);
        accountListAdapter.clear();
        accountListAdapter.addAll(state.getAccounts());
    }

    private void writeState() {
        State state = new State();
        state.setAccounts(accountListAdapter.getAllItems());
        state.store(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == R.id.action_about) {
            AboutBox.show(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.context_menu, menu);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
        Account acc = (Account)accountListView.getItemAtPosition(info.position);
        if (acc != null) {
            menu.setHeaderTitle(acc.getName());
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        Account acc = (Account)accountListView.getItemAtPosition(info.position);
        if (acc == null) {
            return false;
        }
        switch (item.getItemId()) {
            case R.id.edit:
                accountDlg(accountListView, acc,
                        (n, k, o, v) -> updateAccount(acc, n, k, o, v));
                break;
            case R.id.duplicate:
                accountDlg(accountListView, acc, this::addAccount);
                break;
            case R.id.delete:
                accountListAdapter.remove(acc);
                writeState();
                break;
            case R.id.copy: {
                ClipboardManager clipboard = (ClipboardManager)
                        getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText(getString(R.string.token),
                        acc.generateOTP(System.currentTimeMillis()));
                clipboard.setPrimaryClip(clip);
                break;
            }
            default:
                return false;
        }
        return true;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }
}
