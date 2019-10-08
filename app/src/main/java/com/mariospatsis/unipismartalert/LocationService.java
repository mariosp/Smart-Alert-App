package com.mariospatsis.unipismartalert;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService implements LocationListener {
static double latitude;
static double longitude;

    @Override
    public void onLocationChanged(Location location) {

        if(location==null){

        }else {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    static String getCity (){
        String cityName = null;
        Geocoder gcd = new Geocoder(MainActivity.mainActivity, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(latitude,
                    longitude, 1);
            if (addresses.size() > 0) {
                cityName = addresses.get(0).getLocality();
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return cityName;
    }
}
