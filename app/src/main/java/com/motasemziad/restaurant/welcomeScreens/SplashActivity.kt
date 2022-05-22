package com.motasemziad.restaurant.welcomeScreens

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.DexterError
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.motasemziad.restaurant.MainActivity
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.sharedPreferences.MySharedPrefernces
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity(), GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener {

    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mLocationRequest: LocationRequest? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var result: PendingResult<LocationSettingsResult>? = null
    private var locationManager: LocationManager? = null
    private var handler: Handler? = null
    private var mLocation: Location? = null

    val REQUEST_LOCATION = 199

    var lat = 0.0
    var lng = 0.0

    var mLocationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations)
            {
                mLocation = location
            }

            lat = mLocation!!.latitude  // to get latitude
            lng = mLocation!!.longitude // to get longitude

            // to stored the latitude and the longitude in shared preferences file
            MySharedPrefernces.prepare(this@SplashActivity)
            MySharedPrefernces.setUserLatitude(lat.toFloat())
            MySharedPrefernces.setUserLongitude(lng.toFloat())

            Log.e("abd", "User Location : $lat , $lng")
            if (mFusedLocationClient != null)
            {
                mFusedLocationClient!!.removeLocationUpdates(this)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //startActivity(Intent(this,MapsActivity::class.java))


        setContentView(R.layout.activity_splash)

        handler = Handler()
        requestStoragePermission() // call requestStoragePermission method

        /*Thread{
            Thread.sleep(1000)
            runOnUiThread {splach_appName.animate().alpha(1f).duration = 1500}

            //then show bio after 1 sec
            Thread.sleep(1000)
            runOnUiThread {splach_bio.animate().alpha(1f).duration = 1500}
            Thread.sleep(2000)

            //Thread.sleep(3000)
            MySharedPrefernces.prepare(this)
            if(MySharedPrefernces.getLoginViaGoogle() || MySharedPrefernces.getLoginViaFacebook())
            {
                startActivity(Intent(this, MainActivity::class.java))
            }else{
                startActivity(Intent(this,IntroActivity::class.java))
            }

            //remove screen from back stack
            runOnUiThread {finish()}
        }.start()*/

        /*//splach screen
        var handler = Handler(Looper.getMainLooper())
        handler.post(Runnable{
            //excute code while spalch is running
            for(i in 0..20)
            {
                Log.e("emad",i.toString())
            }
        })*/

    } // end of onCreate

    private fun requestStoragePermission() {
        Dexter.withActivity(this)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(object : PermissionListener {

                override fun onPermissionGranted(response: PermissionGrantedResponse?) {

                    if (ActivityCompat.checkSelfPermission(
                            this@SplashActivity,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            this@SplashActivity,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            1
                        )
                    } else {
                        mFusedLocationClient =
                            LocationServices.getFusedLocationProviderClient(this@SplashActivity)
                        mLocationRequest = LocationRequest.create()
                        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        if (isLocationServiceEnabled(this@SplashActivity))
                        {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                            {
                                if (ContextCompat.checkSelfPermission(
                                        this@SplashActivity,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    )
                                    == PackageManager.PERMISSION_GRANTED
                                ) {
                                    mFusedLocationClient!!.requestLocationUpdates(
                                        mLocationRequest!!,
                                        mLocationCallback,
                                        Looper.myLooper()!!
                                    )
                                } //Request Location Permission
                            } else {
                                mFusedLocationClient!!.requestLocationUpdates(
                                    mLocationRequest!!,
                                    mLocationCallback,
                                    Looper.myLooper()!!
                                )
                            }
                            if (isLocationServiceEnabled(this@SplashActivity))
                            {
                                Thread {
                                    Thread.sleep(1000)
                                    runOnUiThread {splach_appName.animate().alpha(1f).duration = 1500}

                                    //then show bio after 1 sec
                                    Thread.sleep(1000)
                                    runOnUiThread {splach_bio.animate().alpha(1f).duration = 1500}
                                    Thread.sleep(2000)

                                    //Thread.sleep(3000)
                                    MySharedPrefernces.prepare(this@SplashActivity)
                                    if (MySharedPrefernces.getLoginViaGoogle() || MySharedPrefernces.getLoginViaFacebook())
                                    {
                                        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                                    } else {
                                        startActivity(Intent(this@SplashActivity,IntroActivity::class.java))
                                    }

                                    //remove screen from back stack
                                    runOnUiThread {finish()}
                                }.start()
                            }
                        } else {
                            mGoogleApiClient = GoogleApiClient.Builder(this@SplashActivity)
                                .addConnectionCallbacks(this@SplashActivity)
                                .addOnConnectionFailedListener(this@SplashActivity)
                                .addApi(LocationServices.API)
                                .build()
                            mGoogleApiClient!!.connect()
                        }
                    }
                }

                override fun onPermissionDenied(response: PermissionDeniedResponse?) {
                    showSettingsDialog()
                }

                override fun onPermissionRationaleShouldBeShown(
                    permission: PermissionRequest?,
                    token: PermissionToken?
                ) {
                    token!!.continuePermissionRequest()
                }
            })
            .withErrorListener { error: DexterError? ->
                Toast.makeText(applicationContext, "try again", Toast.LENGTH_SHORT).show()
            }
            .onSameThread()
            .check()
    } // end of requestStoragePermission() method

    private fun showSettingsDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@SplashActivity)
        builder.setTitle("تنبيه")
        builder.setMessage("لن تتمكن من استخدام التطبيق الا بعد السماح بالاذن")
        builder.setPositiveButton("حسنا") { dialog, _ ->
            dialog.cancel()
            requestStoragePermission()
        }
        builder.setNegativeButton("الغاء") { _, _ ->
            onBackPressed()
        }
        builder.create().show()
    }

    private fun isLocationServiceEnabled(context: Context): Boolean {
        var gps_enabled = false
        var network_enabled = false
        try
        {
            locationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager?
            val criteria = Criteria()
            criteria.powerRequirement = Criteria.POWER_MEDIUM // Chose your desired power consumption level.
            criteria.accuracy = Criteria.ACCURACY_COARSE // Choose your accuracy requirement.
            criteria.isSpeedRequired = true // Chose if speed for first location fix is required.
            criteria.isAltitudeRequired = true // Choose if you use altitude.
            criteria.isBearingRequired = true // Choose if you use bearing.
            criteria.isCostAllowed = true // Choose if this provider can waste money :-)
            locationManager!!.getBestProvider(criteria, true)
            gps_enabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            network_enabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        } catch (ex: IndexOutOfBoundsException) {
            ex.message
        }
        return gps_enabled || network_enabled
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode)
        {
            REQUEST_LOCATION -> when (resultCode) {
                Activity.RESULT_OK -> requestStoragePermission()
                Activity.RESULT_CANCELED -> finish()
                else -> { }
            }
            else -> { }
        }
    }

    override fun onConnected(@Nullable bundle: Bundle?) {

        mLocationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(3 * 1000)
            .setFastestInterval(1000)

        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(mLocationRequest!!)
        builder.setAlwaysShow(true)

        result =
            LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient!!, builder.build())
        result!!.setResultCallback(
            ResultCallback { result: LocationSettingsResult ->

                val status: Status = result.status
                when (status.statusCode)
                {
                    LocationSettingsStatusCodes.SUCCESS -> { }

                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->
                        try {
                            status.startResolutionForResult(this@SplashActivity, REQUEST_LOCATION)
                        } catch (e: IntentSender.SendIntentException) { }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> { }
                }
            }
        )
    } // end of onConnected() method

    override fun onConnectionSuspended(i: Int) {}
    override fun onConnectionFailed(p0: ConnectionResult) {}

    override fun onStart() {
        super.onStart()
        if (!isLocationServiceEnabled(this@SplashActivity)) if (mGoogleApiClient != null) {
            mGoogleApiClient!!.connect()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mGoogleApiClient != null) mGoogleApiClient!!.disconnect()
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
    }

    override fun onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient!!.disconnect()
        }
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
        super.onStop()
    }

    override fun onDestroy() {
        if (mGoogleApiClient != null) mGoogleApiClient!!.disconnect()
        handler!!.removeCallbacksAndMessages(null)
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
        }
        super.onDestroy()
    }

}