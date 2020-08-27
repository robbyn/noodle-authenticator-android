package org.tastefuljava.noodleauth;

import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private ListView accountListView;
    private AccountListAdapter accountListAdapter;

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
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        accountListView = findViewById(R.id.account_listview);
        accountListAdapter = new AccountListAdapter(this);
        accountListView.setAdapter(accountListAdapter);
        accountListAdapter.add(new Account("Digit",
                Codec.BASE32.decode("MNYUWY2PIVYHAVCB"), 6, 30));
        handler.post(refresh);
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