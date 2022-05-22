package com.motasemziad.restaurant.welcomeScreens

import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.viewpager.widget.ViewPager
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.motasemziad.restaurant.MainActivity
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.adapters.IntroViewPagerAdapter
import com.motasemziad.restaurant.internet.InternetBroadcast
import com.motasemziad.restaurant.models.ScreenItem
import com.motasemziad.restaurant.models.User
import com.motasemziad.restaurant.sharedPreferences.MySharedPrefernces
import kotlinx.android.synthetic.main.fragment_profile.*


class IntroActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    var callbackManager: CallbackManager? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 9001

    lateinit var broadcast: InternetBroadcast

    internal var position = 0
    lateinit private var screenPager: ViewPager
    lateinit internal var introViewPagerAdapter: IntroViewPagerAdapter
    lateinit internal var tabIndicator: TabLayout
    lateinit internal var btnNext: Button
    lateinit internal var btnLoginFacebook: Button
    lateinit internal var btnLoginGoogle: Button
    lateinit internal var btnAnim: Animation

    lateinit var facebookUser: String
    lateinit var googleUser: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        facebookUser = "FacebookUsers"
        googleUser = "GoogleUsers"

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()


        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        getWindow().setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // when this activity is about to be launch we need to check if its openened before or not
        MySharedPrefernces.prepare(applicationContext)
        if (MySharedPrefernces.getLoginViaFacebook() || MySharedPrefernces.getLoginViaGoogle()) {
            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )
            finish()
        }

        setContentView(R.layout.activity_intro)

        // ini views
        btnNext = findViewById(R.id.btn_next)
        btnLoginFacebook = findViewById(R.id.intro_facebook)
        btnLoginGoogle = findViewById(R.id.intro_google)
        tabIndicator = findViewById(R.id.tab_indicator)
        btnAnim = AnimationUtils.loadAnimation(
            getApplicationContext(),
            R.anim.button_animation
        )

        // fill list screen
        val mList = ArrayList<ScreenItem>()
        mList.add(
            ScreenItem(
                "Fresh Food",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                R.drawable.img1
            )
        )
        mList.add(
            ScreenItem(
                "Fast Delivery",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                R.drawable.img2
            )
        )
        mList.add(
            ScreenItem(
                "Easy Payment",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit",
                R.drawable.img3
            )
        )

        // setup viewpager
        screenPager = findViewById(R.id.screen_viewpager)
        introViewPagerAdapter =
            IntroViewPagerAdapter(
                this,
                mList
            )
        screenPager.setAdapter(introViewPagerAdapter)

        // setup tablayout with viewpager
        tabIndicator.setupWithViewPager(screenPager)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tabIndicator.setBackgroundColor(resources.getColor(R.color.activityBackground, null))
        }

        // next button click Listner
        btnNext.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                position = screenPager.getCurrentItem()
                if (position < mList.size) {
                    position++
                    screenPager.setCurrentItem(position)
                }
                if (position == mList.size - 1) { // when we rech to the last screen
                    // TODO : show the GETSTARTED Button and hide the indicator and the next button
                    loadedLastScreen()
                }
            }
        })
        // tabLayout add change listener
        tabIndicator.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.getPosition() == mList.size - 1) {
                    loadedLastScreen()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {
            }

            override fun onTabReselected(tab: TabLayout.Tab) {
            }
        })

        // Login With Facebook Code //

        // Initialize Facebook Login button
        callbackManager = CallbackManager.Factory.create()

        FacebookSdk.sdkInitialize(applicationContext)

        val binding = LoginManager.getInstance()

        // Login With Facebook
        btnLoginFacebook.setOnClickListener {

            binding.logInWithReadPermissions(
                this@IntroActivity,
                arrayListOf("email", "public_profile")
            )
            binding.registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {

                }

                override fun onError(error: FacebookException) {

                }
            })

        }

        // Login With Google Code //

        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("205275435435-64f4qo43jigqfhgclg11llb83b3i4vn0.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Login With Google
        btnLoginGoogle.setOnClickListener {
            signIn()
        }

    } // end of onCreate method

    // show the Facebook and Google Button and hide the indicator and the next button
    private fun loadedLastScreen() {
        btnNext.visibility = View.INVISIBLE
        btnLoginFacebook.visibility = View.VISIBLE
        tabIndicator.visibility = View.INVISIBLE
        btnLoginGoogle.visibility = View.VISIBLE
        // TODO : ADD an animation the getstarted button
        // setup animation
        btnLoginFacebook.animation = btnAnim
        btnLoginGoogle.animation = btnAnim
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Pass the activity result back to the Facebook SDK
        callbackManager!!.onActivityResult(requestCode, resultCode, data)


        // This Code For Google Login //

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                Log.e("IntroActivity", "firebaseAuthWithGoogle:" + account.id)
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.e("IntroActivity", "Google sign in failed")
            }
        }

    }

    // Login With Facebook Methods //

    private fun handleFacebookAccessToken(token: AccessToken) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->

                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = auth.currentUser
                    /*var graphrequest = GraphRequest.newMeRequest(
                        token,
                        object : GraphRequest.GraphJSONObjectCallback{
                            override fun onCompleted(`object`: JSONObject?, response: GraphResponse?) {
                                Log.e("emad","\n\nJSON OBJECT \n\n" + `object`.toString())
                            }
                        })

                    val parameters = Bundle()
                    parameters.putString("fields", "name,id")
                    graphrequest.setParameters(parameters)
                    graphrequest.executeAsync()*/

                    updateUI(user, facebookUser, task.result!!.additionalUserInfo!!.isNewUser)

                } else {
                    updateUI(null, null, true)
                }

            }
    } // end of handleFacebookAccessToken method

    private fun updateUI(currentUser: FirebaseUser?, usersType: String?, isFirstTime: Boolean) {
        if (currentUser == null) {
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Failed To SignUp")
            alertDialog.setMessage("please check your internet connection")
            alertDialog.setCancelable(true)
            alertDialog.create().show()
            return
        }

        val database = Firebase.database
        val myRef = database.getReference()

        val key = myRef.push().key

        //to avoid null
        var photoUrl = ""
        var name = ""
        var email = ""
        var phone = ""

        if (currentUser.photoUrl != null)
            photoUrl = currentUser.photoUrl.toString() + "?height=500"
        if (currentUser.displayName != null)
            name = currentUser.displayName!!
        if (currentUser.email != null)
            email = currentUser.email!!
        if (currentUser.phoneNumber != null) {
            phone = currentUser.phoneNumber!!
            if (email == null)
                email = phone
        }

        if (isFirstTime) {
            myRef.child(usersType!!).child(key!!)
                .setValue(User(key, photoUrl, name, email, "", phone, "", 0f, 0f))
                .addOnSuccessListener {
                    Toast.makeText(this, "User Added Successfully", Toast.LENGTH_SHORT).show()
                    //save user info inside shared preferences the go to home activity
                    MySharedPrefernces.prepare(applicationContext)
                    MySharedPrefernces.setUserName(name)
                    MySharedPrefernces.setUserImage(photoUrl)
                    MySharedPrefernces.setUserEmail(email)
                    MySharedPrefernces.setUserPhone(phone)
                    MySharedPrefernces.setUserId(key)
                }.addOnFailureListener {
                    Toast.makeText(this, "Error in database", Toast.LENGTH_SHORT).show()
                }

        } else {
            MySharedPrefernces.prepare(applicationContext)
            myRef.child(usersType!!).addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    for (user in p0.children) {
                        var u = user.getValue(User::class.java)
                        if (u!!.name.equals(currentUser.displayName, true) && u.email.equals(
                                currentUser.email,
                                true
                            )
                        ) {
                            //save user info inside shared preferences the go to home activity
                            MySharedPrefernces.setUserName(u.name)
                            MySharedPrefernces.setUserImage(u.url)
                            MySharedPrefernces.setUserEmail(u.email)
                            MySharedPrefernces.setUserPhone(u.phone)
                            MySharedPrefernces.setUserId(u.id)
                            break
                        }
                    }
                }

            })
        }

        if (usersType.equals(facebookUser)) {
            MySharedPrefernces.setLoginViaFacebook(true)
            MySharedPrefernces.setLoginViaGoogle(false)
        } else {
            MySharedPrefernces.setLoginViaFacebook(false)
            MySharedPrefernces.setLoginViaGoogle(true)
        }

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    } // end of updateUI method

    // Login With Google Methods //

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.e("IntroActivity", "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user, googleUser, task.result!!.additionalUserInfo!!.isNewUser)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.e("IntroActivity", "signInWithCredential:failure", task.exception)
                    // ...
                    Snackbar.make(view, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                    updateUI(null, null, true)
                }
            }
    } // end of firebaseAuthWithGoogle method

    override fun onResume() {
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        broadcast = InternetBroadcast()

        registerReceiver(broadcast, intentFilter)

        super.onResume()
    }

    override fun onPause() {
        unregisterReceiver(broadcast)
        super.onPause()
    }


}