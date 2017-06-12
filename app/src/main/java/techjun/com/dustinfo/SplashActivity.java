package techjun.com.dustinfo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import techjun.com.dustinfo.service.DustService;

/**
 * Created by leebongjun on 2017. 6. 12..
 */

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Start home activity
        startActivity(new Intent(this, MainActivity.class));
        // close splash activity
        finish();
    }
}