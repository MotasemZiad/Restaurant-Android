package com.motasemziad.restaurant.sharedPreferences

import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity

class MySharedPrefernces {

    companion object
    {

        //context
        var context: Context? = null

        //obj from shared preferences
        var sharedPrefernces: SharedPreferences? = null;

        //key -> boolean
        private val INTRO_SCREEN_OPENED_KEY = "intro_screen_has_alreay_been_opend"
        private val LOGIN_VIA_FACEBOOK = "user_login_via_facebbok"
        private val LOGIN_REGULAE_WAY = "user_login_via_email"
        private val USER_NAME = "username"
        private val USER_EMAIL = "user_email"
        private val USER_ID = "user_id"
        private val USER_PHONE = "user_phone"
        private val USER_ADDRESS = "user_address"
        private val USER_LATITUDE = "user_latitude"
        private val USER_LONGITUDE = "user_longitude"
        private val USER_IMAGE_URL = "user_image_url"


        /**this fun prepare the shared preferences so u can use it later
         * NOTE: u cant use share pref without call this method (it will cause null pointer exception)*/
        fun prepare(context:Context)
        {
            Companion.context = context
            sharedPrefernces = context.getApplicationContext().getSharedPreferences("myPrefs",
                AppCompatActivity.MODE_PRIVATE)
        }


        /**set intro screen status (seen by the user or not)*/
        fun setIntroScreenOpened(seen:Boolean)
        {
            getEditor().putBoolean(INTRO_SCREEN_OPENED_KEY,seen).commit()
        }

        /**set if user login via facebook*/
        fun setLoginViaFacebook(logged:Boolean)
        {
            getEditor().putBoolean(LOGIN_VIA_FACEBOOK,logged).commit()
        }

        /**set if user login via google*/
        fun setLoginViaGoogle(logged:Boolean)
        {
            getEditor().putBoolean(LOGIN_REGULAE_WAY,logged).commit()
        }

        /**set username*/
        fun setUserName(username:String)
        {
            getEditor().putString(USER_NAME,username).commit()
        }

        /**set password*/
        fun setUserId(userID:String)
        {
            getEditor().putString(USER_ID,userID).commit()
        }

        /**set phone*/
        fun setUserPhone(phone:String)
        {
            getEditor().putString(USER_PHONE,phone).commit()
        }

        /**set address*/
        fun setUserAddress(address:String)
        {
            getEditor().putString(USER_ADDRESS,address).commit()
        }

        /**set latitude*/
        fun setUserLatitude(latitude:Float)
        {
            getEditor().putFloat(USER_LATITUDE, latitude).commit()
        }

        /**set longitude*/
        fun setUserLongitude(longitude: Float)
        {
            getEditor().putFloat(USER_LONGITUDE, longitude).commit()
        }

        /**set image*/
        fun setUserImage(imageUrl:String)
        {
            getEditor().putString(USER_IMAGE_URL,imageUrl).commit()
        }

        /**set user email*/
        fun setUserEmail(email:String)
        {
            getEditor().putString(USER_EMAIL,email).commit()
        }


        /**return editor of my shared preferences*/
        fun getEditor() : SharedPreferences.Editor = sharedPrefernces!!.edit()


        /**return boolean whether the intro(welcome screen) have opened before or not*/
        fun getIntroOpenedBeforeResult () : Boolean = sharedPrefernces!!.getBoolean(
            INTRO_SCREEN_OPENED_KEY,false)

        /**return boolean wether user login with facebook or not*/
        fun getLoginViaFacebook() : Boolean = sharedPrefernces!!.getBoolean(LOGIN_VIA_FACEBOOK,false)

        /**return boolean wether user login with regular way or not*/
        fun getLoginViaGoogle() : Boolean = sharedPrefernces!!.getBoolean(LOGIN_REGULAE_WAY,false)

        /**return username*/
        fun getUserName() : String = sharedPrefernces!!.getString(USER_NAME,"UNKNOWN")!!

        /**return user id*/
        fun getUserId() : String = sharedPrefernces!!.getString(USER_ID,"UNKNOWN")!!

        /**return user email*/
        fun getUserEmail() : String = sharedPrefernces!!.getString(USER_EMAIL,"UNKNOWN")!!

        /**return user address*/
        fun getUserAddress() : String = sharedPrefernces!!.getString(USER_ADDRESS,"UNKNOWN")!!

        /**return user latitude*/
        fun getUserLatitude() : Float = sharedPrefernces!!.getFloat(USER_LATITUDE,0f)

        /**return user longitude*/
        fun getUserLongitude() : Float = sharedPrefernces!!.getFloat(USER_LONGITUDE,0f)

        /**return user image*/
        fun getUserImage() : String = sharedPrefernces!!.getString(USER_IMAGE_URL,"UNKNOWN")!!

        /**return user phone*/
        fun getUserPhone() : String = sharedPrefernces!!.getString(USER_PHONE,"UNKNOWN")!!

    }

}