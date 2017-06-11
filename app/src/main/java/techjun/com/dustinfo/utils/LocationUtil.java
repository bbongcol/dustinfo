package techjun.com.dustinfo.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by leebongjun on 2017. 6. 4..
 */

public class LocationUtil {
    private static LocationUtil sInstance = null;
    private LocationManager locationManager;
    private LocationListener locationListener;
    final static String TAG = "LocationUtil";

    boolean canReadLocation = false;

    private Context mContext;

    private LocationUtil(Context context) {
        mContext = context;
        settingInitGPS();
    }

    public static LocationUtil getInstance() {
        return sInstance;
    }

    public static LocationUtil initInstance(Context context) {
        if (sInstance == null) {
            //Always pass in the Application Context
            sInstance = new LocationUtil(context.getApplicationContext());
        }
        return sInstance;
    }

    public void settingInitGPS() {
        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();
                // TODO 위도, 경도로 하고 싶은 것
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
            }
        };
    }

    @SuppressLint("MissingPermission")
    public Location getMyLocation() {
        Location currentLocation = null;

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
        }
        return currentLocation;
    }

    public ArrayList<String> getAddressArrayList() {
        ArrayList<String> bf = new ArrayList<String>();
        double lat = 0, lng = 0;
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;

        Location userLocation = getMyLocation();

        if( userLocation != null ) {
            // TODO 위치를 처음 얻어왔을 때 하고 싶은 것
            lat = userLocation.getLatitude();
            lng = userLocation.getLongitude();
        }

        try {
            if (geocoder != null && userLocation != null) {
                // 세번째 인수는 최대결과값인데 하나만 리턴받도록 설정했다
                address = geocoder.getFromLocation(lat, lng, 1);
                // 설정한 데이터로 주소가 리턴된 데이터가 있으면
                if (address != null && address.size() > 0) {
                    // 전송할 주소 데이터 (위도/경도 포함 편집)
                    bf.add(address.get(0).getAdminArea());
                    bf.add(address.get(0).getLocality());
                    bf.add(address.get(0).getSubLocality());
                }
            } else {
                bf.add("서울특별시");
                bf.add("서초구");
                bf.add(null);
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "주소취득 실패"
                    , Toast.LENGTH_LONG).show();

            e.printStackTrace();

        }
        return bf;
    }

    public String[] getAddressList () {
        ArrayList<String> myAddress = getAddressArrayList();
        return myAddress.toArray(new String[myAddress.size()]);
    }


}
