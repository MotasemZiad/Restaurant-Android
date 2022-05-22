package com.motasemziad.restaurant

import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.motasemziad.restaurant.internet.InternetBroadcast
import com.motasemziad.restaurant.userScreens.HomeFragment
import com.motasemziad.restaurant.userScreens.NearestRestaurantsFragment
import com.motasemziad.restaurant.userScreens.ProfileFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

class MainActivity : AppCompatActivity() {
    var broadcast = InternetBroadcast()

    // when select item from bottom navigation || note: this code outside onCreate method
    private val itemSelected = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when(item.itemId) {
            R.id.home -> {
                replaceFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.profile -> {
                replaceFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nearestRestaurant -> {
                replaceFragment(NearestRestaurantsFragment())
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        nav_view.setOnNavigationItemSelectedListener(itemSelected)
        nav_view.selectedItemId = R.id.home

        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }

    } // end of onCreate

    /*override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }*/

    // this code to exit from the app when user double click on back pressed button
    private var doubleBackToExitPressedOnce = false
    override fun onBackPressed() {

        val count = supportFragmentManager.backStackEntryCount
        if (count == 0)
        {
            if (doubleBackToExitPressedOnce)
            {
                super.onBackPressed()
                return
            }

            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "أضغط مرة أخرى للخروج", Toast.LENGTH_SHORT).show()

            Handler().postDelayed( { doubleBackToExitPressedOnce = false }, 2000)
        } else
        {
            supportFragmentManager.popBackStack()
        }
    }

    // this function to switch between fragments
    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().replace(R.id.main_container, fragment).commit()
    }

    override fun onResume() {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        broadcast = InternetBroadcast()

        registerReceiver(broadcast,intentFilter)

        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(broadcast)
        super.onPause()
    }

}