package com.motasemziad.restaurant.userScreens

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.adapters.RestaurantAdapter
import com.motasemziad.restaurant.keys.BundleKeys
import com.motasemziad.restaurant.keys.DatabaseKeys
import com.motasemziad.restaurant.models.Restaurant
import com.motasemziad.restaurant.sharedPreferences.MySharedPrefernces
import com.motasemziad.restaurant.welcomeScreens.SplashActivity
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.view.*

class HomeFragment : Fragment(), SearchView.OnQueryTextListener, RestaurantAdapter.HandlerClicks {

    var adapter: RestaurantAdapter? = null
    var restaurants: ArrayList<Restaurant>? = null
    var restaurantSearch: ArrayList<Restaurant>? = null

    lateinit var database: FirebaseDatabase
    lateinit var mRef: DatabaseReference

    lateinit var thread: Thread

    lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_home, container, false)

        root.home_toolbar.inflateMenu(R.menu.main_menu)

        return root
    } // end of onCreateView


    override fun onResume() {
        super.onResume()

        //initialize database
        database = Firebase.database
        mRef = database.reference

        //show progress bar until data
        root.homeProgressBar.visibility = View.VISIBLE
        root.homeScreen_recycler.alpha = 0.0f

        // initialize arrays and adapter
        restaurants  = ArrayList()
        restaurantSearch = ArrayList()
        adapter = RestaurantAdapter(requireContext(), restaurants!!, this)

        // this for search
        val searchView =
            MenuItemCompat.getActionView(root.home_toolbar.menu.findItem(R.id.search)) as SearchView
        searchView.setOnQueryTextListener(this)

        root.home_toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.sign_out)
            {
                MySharedPrefernces.prepare(requireContext().applicationContext)
                MySharedPrefernces.setLoginViaFacebook(false)
                MySharedPrefernces.setLoginViaGoogle(false)
                var auth = FirebaseAuth.getInstance()
                auth.signOut()
                startActivity(Intent(requireContext(), SplashActivity::class.java))
                requireActivity().finish()
            }
            onOptionsItemSelected(item)
        }

        //chose layout for recycler
        root.homeScreen_recycler.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL,false)
        root.homeScreen_recycler.adapter = adapter //connect recycler with adapter

        mRef.child(DatabaseKeys.RESTAURANTS).addValueEventListener(object : ValueEventListener {

            override fun onDataChange(data: DataSnapshot) {
                restaurants!!.clear()
                for (res in data.children) {
                    restaurants!!.add(0, res.getValue(Restaurant::class.java)!!)
                }
                adapter!!.notifyDataSetChanged()

                // check if restaurants is empty or not
                if (restaurants!!.isEmpty())
                {
                    root.home_no_result_image.visibility = View.VISIBLE // to show the no result image
                    root.home_no_result_tv.visibility = View.VISIBLE    // to show the no result title
                    root.home_no_result_tv.text = "لا يوجد مطاعم"
                } else
                {
                    root.home_no_result_image.visibility = View.GONE // to hide the no result image
                    root.home_no_result_tv.visibility = View.GONE    // to hide the no result title
                }

                //hide progress bar and show the recycler (with animation)
                thread = Thread(Runnable {
                    try {
                        activity!!.runOnUiThread { root.homeProgressBar.animate().alpha(0.0f).duration = 1500}
                        Thread.sleep(1000)
                        activity!!.runOnUiThread { root.homeProgressBar.visibility = View.GONE }
                        activity!!.runOnUiThread { root.homeScreen_recycler.animate().alpha(1.0f).duration = 1000 }
                    } catch (e:Exception){}
                })
                thread.start()
            }

            override fun onCancelled(data: DatabaseError) {

            }

        })

    } // end of onResume

    // when user click on any restaurant in home screen
    override fun onCardClick(position: Int) {

        val bundle = Bundle()
        val restaurant = restaurants!![position]
        bundle.putParcelable(BundleKeys.BUNDLE_RESTAURANT, restaurant)

        val fragment = RestaurantDetailsFragment()
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(null).replace(R.id.main_container, fragment).commit()

    } // end of onCardClick

    // For Search

    override fun onQueryTextSubmit(query: String?): Boolean {
        search(query)
        if (restaurantSearch!!.isEmpty())
        {
            Snackbar.make(homeScreen_recycler, "لا يوجد مطعم يحتوي على هذا الاسم", Snackbar.LENGTH_LONG).show()
            root.home_no_result_image.visibility = View.VISIBLE
            root.home_no_result_tv.visibility = View.VISIBLE
        } else
        {
            root.home_no_result_image.visibility = View.GONE
            root.home_no_result_tv.visibility = View.GONE
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        search(newText)
        if (restaurantSearch!!.isEmpty() && newText!!.isNotEmpty())
        {
            Snackbar.make(homeScreen_recycler, "لا يوجد مطعم يحتوي على هذا الاسم", Snackbar.LENGTH_LONG).show()
            root.home_no_result_image.visibility = View.VISIBLE
            root.home_no_result_tv.visibility = View.VISIBLE
        } else
        {
            root.home_no_result_image.visibility = View.GONE
            root.home_no_result_tv.visibility = View.GONE
        }
        return false
    }

    var name = ""
    private fun search(searchName: String?) {
        restaurantSearch!!.clear()
        for (i in 0 until restaurants!!.size)
        {
            name = restaurants!![i].name
            if (name.toLowerCase().contains(searchName!!.toLowerCase()))
            {
                restaurantSearch!!.add(restaurants!![i])
            }
        }

        adapter = RestaurantAdapter(requireContext(), restaurantSearch!!, this)
        homeScreen_recycler.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL,false)
        homeScreen_recycler.adapter = adapter
    }

}