package techjun.com.dustinfo;

import android.Manifest;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import techjun.com.dustinfo.fragment.MainDustInfoFragment;
import techjun.com.dustinfo.fragment.SwipeRefreshListFragmentFragment;
import techjun.com.dustinfo.service.DustDBService;
import techjun.com.dustinfo.service.DustService;
import techjun.com.dustinfo.utils.LocationUtil;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, MainDustInfoFragment.OnFragmentInteractionListener{

    public final static int REQUEST_CODE_LOCATION = 1000;

    public final static int FRAGMENT_DUST_INFO_MAIN = 2001;
    public final static int FRAGMENT_DUST_INFO_LIST = 2002;
    public final static int FRAGMENT_DUST_INFO_GRAPH = 2004;
    public final static int FRAGMENT_SETTING = 2004;

    final static String TAG = "MainActivity";

    DustDBService mDustDBService;
    boolean mBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //권한 요청
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 사용자 권한 요청
            Log.d(TAG, "requestPermissions");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_LOCATION);
        } else {
            setTitle(displayAddress(LocationUtil.getInstance(this).getAddressList()));
            DustService.getInstance(this).updateDustInfo();

            if (savedInstanceState == null) {
                updateFragment(FRAGMENT_DUST_INFO_MAIN);
            }
        }

        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //서비스 실행 및 바인딩
        Intent intent = new Intent(this, DustDBService.class);
        startService(intent);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);

        //mDustDBService.getJsonText();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        doUnbindService();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
    }

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            DustDBService.LocalBinder binder = (DustDBService.LocalBinder) service;
            mDustDBService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    void doUnbindService() {
        if (mBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mBound = false;
        }
    }

    public String displayAddress (String[] address) {
        String displayAddress = null;
        if(address[2] != null) {
            displayAddress = address[1] + " " + address[2];
        } else {
            displayAddress = address[0] + " " + address[1];
        }
        return displayAddress;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult");

        if (requestCode != REQUEST_CODE_LOCATION) {
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {

        }
        //권한 유무와 상관없이 업데이트
        setTitle(displayAddress(LocationUtil.getInstance(this).getAddressList()));
        DustService.getInstance(this).updateDustInfo();
        updateFragment(FRAGMENT_DUST_INFO_MAIN);
        return;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        int id = item.getItemId();
        int fragmentId = 0;

        if (id == R.id.nav_main) {
            fragmentId = FRAGMENT_DUST_INFO_MAIN;
        } else if (id == R.id.nav_list) {
            fragmentId = FRAGMENT_DUST_INFO_LIST;
        } else if (id == R.id.nav_graph) {
            fragmentId = FRAGMENT_DUST_INFO_GRAPH;
        } else if (id == R.id.nav_setting) {
            fragmentId = FRAGMENT_SETTING;
        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        updateFragment(fragmentId);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void updateFragment (int feagment) {
        updateFragment(feagment, false);
    }

    private void updateFragment (int feagment, boolean addToBackStack) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        if (feagment == FRAGMENT_DUST_INFO_MAIN) {
            MainDustInfoFragment mMainDustInfoFragment = MainDustInfoFragment.newInstance("", "");
            transaction.replace(R.id.dust_content_fragment, mMainDustInfoFragment);
        } else if (feagment == FRAGMENT_DUST_INFO_LIST) {
            SwipeRefreshListFragmentFragment fragment = new SwipeRefreshListFragmentFragment();
            transaction.replace(R.id.dust_content_fragment, fragment);
        } else if (feagment == FRAGMENT_DUST_INFO_GRAPH) {

        } else if (feagment == FRAGMENT_SETTING) {

        } else if (feagment == R.id.nav_share) {

        } else if (feagment == R.id.nav_send) {

        }

        if(addToBackStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
