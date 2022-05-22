package com.motasemziad.restaurant.userScreens

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.keys.BundleKeys
import com.motasemziad.restaurant.keys.DatabaseKeys
import com.motasemziad.restaurant.models.Rate
import com.motasemziad.restaurant.models.Restaurant
import com.motasemziad.restaurant.sharedPreferences.MySharedPrefernces
import kotlinx.android.synthetic.main.custome_rating_dialog.*
import kotlinx.android.synthetic.main.fragment_restaurant_details.*
import kotlinx.android.synthetic.main.fragment_restaurant_details.view.*

class RestaurantDetailsFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var firebase: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    var restaurant: Restaurant? = null

    var oldRate = 0.0f
    var oldRateKey = ""
    var ratedBefore = false
    var finaRestaurantRate = 0f

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //initialize database
        firebase = FirebaseDatabase.getInstance()
        myRef = firebase.getReference()

        restaurant = requireArguments().getParcelable<Restaurant>(BundleKeys.BUNDLE_RESTAURANT)

        MySharedPrefernces.prepare(requireContext())
        var userId = MySharedPrefernces.getUserId()

        myRef.child(DatabaseKeys.RESTAURANTS_RATE).child(restaurant!!.id!!).addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {

                //rating algorithm
                var rate5 = 0f
                var rate4 = 0f
                var rate3 = 0f
                var rate2 = 0f
                var rate1 = 0f

                for(rate in p0.children)
                {
                    var r = rate.getValue(Rate::class.java)

                    //to see if this user rate restaurant before or not
                    if(r!!.userId == userId)
                    {
                        ratedBefore = true
                        oldRate = r.rate
                        oldRateKey = r.rateKey
                    }

                    //to clac restaurant rate
                    if(r.rate == 5f){
                        rate5+=1
                    }else if(r.rate == 4f){
                        rate4 += 1
                    }else if(r.rate == 3f){
                        rate3 +=1
                    }else if(r.rate == 2f){
                        rate2 += 1
                    }else{
                        rate1 += 1
                    }
                }
                finaRestaurantRate = (5*rate5 + 4*rate4 + 3*rate3 + 2*rate2 + 1*rate1) / (rate5+rate4+rate3+rate2+rate1)
                restaurant_details_ratingBar.rating = finaRestaurantRate
            }

        })

        setHasOptionsMenu(true)


        // Inflate the layout for this fragment
        val root = inflater.inflate(R.layout.fragment_restaurant_details, container, false)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = childFragmentManager.fragments[0] as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        //(activity!! as AppCompatActivity).supportActionBar!!.hide()

        //root.restaurant_details_toolbar.title = "تفاصيل المطعم"
        //root.restaurant_details_toolbar.setTitleTextColor(Color.WHITE)

        root.restaurant_details_toolbar.inflateMenu(R.menu.rate_toolbar)

        root.restaurant_details_toolbar.setOnMenuItemClickListener {
            when(it.itemId)
            {
                R.id.rating_star -> {
                    Dialog(requireActivity()).apply {
                        setContentView(R.layout.custome_rating_dialog)
                        if(ratedBefore)
                        {
                            custome_rating_bar_msg.setText("لقد قمت بأهطاء هذا التقييم للمطعم سابقا سيتم تعديله اذا قمت بالتقييم الان")
                            custome_rating_dialog_ratingBar.rating = oldRate
                            if(oldRate == 5f) {
                                custome_rating_dialog_textResult.setText("ممتاز")
                            }else if(oldRate == 1f){
                                custome_rating_dialog_textResult.setText("سيئ")
                            }else if(oldRate == 2f){
                                custome_rating_dialog_textResult.setText("مقبول")
                            }else if(oldRate == 3f){
                                custome_rating_dialog_textResult.setText("جيد")
                            }else if (oldRate == 4f){
                                custome_rating_dialog_textResult.setText("جيد جداً")
                            }
                        }else{
                            custome_rating_bar_msg.setText("تقييمك للمطعم")
                            custome_rating_dialog_textResult.setText("جيد")
                            custome_rating_dialog_ratingBar.rating = 3f
                        }

                        custome_rating_dialog_ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                            if(fromUser)
                            {
                                if(rating == 0f)
                                {
                                    ratingBar.rating = 1f
                                }else if(rating == 1f){
                                    custome_rating_dialog_textResult.setText("سيئ")
                                }else if(rating == 2f){
                                    custome_rating_dialog_textResult.setText("مقبول")
                                }else if(rating == 3f){
                                    custome_rating_dialog_textResult.setText("جيد")
                                }else if (rating == 4f){
                                    custome_rating_dialog_textResult.setText("جيد جداً")
                                }else{
                                    custome_rating_dialog_textResult.setText("ممتاز")
                                }
                            }
                        }//end of rating bar listener

                        custome_rating_dialog_submit_btn.setOnClickListener {
                            var rating = custome_rating_dialog_ratingBar.rating
                            if(ratedBefore)
                            {
                                updateRateOnDatabase(oldRateKey,rating)
                            }else{
                                addRateToDatabase(rating)
                            }
                            dismiss()
                        }

                        window.let { it!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT) }
                    }.show()
                }//R.id.rating_star

                R.id.backArrowFromMealDetails ->{
                    requireActivity().onBackPressed()
                }
            }
            onOptionsItemSelected(it)
        }

        /*root.restaurant_details_toolbar.setOnMenuItemClickListener {
            if(fragmentManager!!.backStackEntryCount > 0)
            {
                fragmentManager!!.popBackStackImmediate()
            } else
            {
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_container, HomeScreen().apply { arguments = this.arguments })
                (activity!! as AppCompatActivity).nav_view.selectedItemId = R.id.home
            }
            onOptionsItemSelected(it)
        }*/

        val bundle = arguments
        if (bundle != null)
        {
            restaurant = bundle.getParcelable<Restaurant>(BundleKeys.BUNDLE_RESTAURANT)
            Glide.with(requireActivity()).load(restaurant!!.image).into(root.restaurant_details_image)
            root.restaurant_details_name.text = restaurant!!.name
            root.restaurant_details_address.text = restaurant!!.address
            root.restaurant_details_ratingBar.rating = restaurant!!.rate

        } else
        {
            Toast.makeText(activity, "No Data", Toast.LENGTH_SHORT).show()
        }

        root.restaurant_details_view_meals_button.setOnClickListener {
            val frag = RestauarentMealsFragment()
            // send restaurant object to "RestaurantMeals" by Bundle
            frag.arguments = bundle
            requireActivity().supportFragmentManager.beginTransaction()
                .addToBackStack(null).replace(R.id.main_container, frag).commit()
        }

        return root
    } // end of onCreateView


    /**this fun add the rate of restaurant to database*/
    private fun addRateToDatabase(rating: Float) {
        //stor user rate on datbase
        MySharedPrefernces.prepare(requireContext())
        var userId = MySharedPrefernces.getUserId() //user id

        var key = myRef.child(DatabaseKeys.RESTAURANTS_RATE).child(restaurant!!.id!!).push().key
        myRef.child(DatabaseKeys.RESTAURANTS_RATE).child(restaurant!!.id!!).child(key!!).setValue(
            Rate(key,userId,restaurant!!.id!!,"",rating)
        )
    }

    /**this method is to update the fucking meal rate*/
    private fun updateRateOnDatabase(rateId:String,rating:Float){
        //stor user rate on datbase
        MySharedPrefernces.prepare(requireContext())
        var userId = MySharedPrefernces.getUserId() //user id

        myRef.child(DatabaseKeys.RESTAURANTS_RATE).child(restaurant!!.id!!).child(rateId).setValue(
            Rate(rateId,userId,restaurant!!.id!!,"",rating)
        )
    }


    override fun onMapReady(p0: GoogleMap) {
        mMap = p0

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireActivity(), R.raw.map_style))

        val restaurantLatLng = LatLng(restaurant!!.latitude, restaurant!!.longitude)
        //val userLatLng = LatLng(31.5, 34.46667)

        mMap.addMarker(MarkerOptions().position(LatLng(restaurantLatLng.latitude,restaurantLatLng.longitude)))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(restaurantLatLng, 18f))

    }//end of onMapReady()

    /*override fun onDestroy() {
        (activity!! as AppCompatActivity).supportActionBar!!.show()
        super.onDestroy()
    }*/

}