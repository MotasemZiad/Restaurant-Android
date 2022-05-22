package com.motasemziad.restaurant.userScreens

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.adapters.RestaurantAdapter
import com.motasemziad.restaurant.keys.BundleKeys
import com.motasemziad.restaurant.keys.DatabaseKeys
import com.motasemziad.restaurant.models.Restaurant
import com.motasemziad.restaurant.sharedPreferences.MySharedPrefernces
import kotlinx.android.synthetic.main.fragment_nearest_restaurants.view.*

class NearestRestaurantsFragment : Fragment(), RestaurantAdapter.HandlerClicks {

    lateinit var root : View
    lateinit var adapter : RestaurantAdapter
    lateinit var restaurants : ArrayList<Restaurant>
    lateinit var myRef : DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root =  inflater.inflate(R.layout.fragment_nearest_restaurants, container, false)

        restaurants = ArrayList()
        adapter = RestaurantAdapter(requireContext(),restaurants,this)

        myRef = FirebaseDatabase.getInstance().getReference()

        return root
    }

    override fun onResume() {
        super.onResume()

        myRef.child(DatabaseKeys.RESTAURANTS)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    restaurants.clear()
                    for(res in p0.children)
                    {
                        restaurants.add(res.getValue(Restaurant::class.java)!!)
                        Log.e("near","Nearest Restaurant")
                    }

                    // check if restaurants is empty or not
                    if (restaurants.isEmpty())
                    {
                        root.nearest_no_result_image.visibility = View.VISIBLE // to show the no result image
                        root.nearest_no_result_tv.visibility = View.VISIBLE    // to show the no result title
                    } else
                    {
                        root.nearest_no_result_image.visibility = View.GONE // to hide the no result image
                        root.nearest_no_result_tv.visibility = View.GONE    // to hide the no result title
                    }

                    try{sortRestaurants()}catch (e:Exception){}
                }

            })
    }

    private fun sortRestaurants() {
        MySharedPrefernces.prepare(requireActivity().applicationContext)

        //to get distance between each restaurant and user
        for(r in restaurants)
        {
            //restaurant location
            var loc1 = Location("").apply {
                longitude = r.longitude
                latitude = r.latitude
            }

            //user location
            var loc2 = Location("").apply {
                longitude = MySharedPrefernces.getUserLongitude().toDouble()
                latitude = MySharedPrefernces.getUserLatitude().toDouble()
            }

            //distance between two locations (user and this restaurant)
            var distance = loc1.distanceTo(loc2).toDouble()
            r.distance = distance

            Log.e("near","--------------------------")
            Log.e("near","User Longtiude -> " + loc1.longitude)
            Log.e("near","User Latitude -> " + loc1.latitude)
            Log.e("near", "Restaurant Longtiude -> " + r.longitude)
            Log.e("near", "Restaurant Latitude -> " + r.latitude)
            Log.e("near","Distance Between = " + distance.toString())
            Log.e("near","--------------------------")
        }

        var sortedArray = restaurants.sortedBy { it.distance }
        root.nearest_progress_bar.visibility = View.GONE
        root.rv_nearestRestaurant.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL,false)
        root.rv_nearestRestaurant.adapter = adapter
        root.rv_nearestRestaurant.animate().alpha(1.0f).duration = 1000
    }

    override fun onCardClick(position: Int) {
        var bundle = Bundle()
        bundle.putParcelable(BundleKeys.BUNDLE_RESTAURANT,restaurants[position])
        var frag = RestaurantDetailsFragment()
        frag.arguments = bundle
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.main_container,frag).commit()
    }
}