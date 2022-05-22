package com.motasemziad.restaurant.userScreens

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.adapters.MealAdapter
import com.motasemziad.restaurant.keys.BundleKeys
import com.motasemziad.restaurant.keys.DatabaseKeys
import com.motasemziad.restaurant.models.Meal
import com.motasemziad.restaurant.models.Restaurant
import kotlinx.android.synthetic.main.fragment_restauarent_meals.*
import kotlinx.android.synthetic.main.fragment_restauarent_meals.view.*


class RestauarentMealsFragment : Fragment(), MealAdapter.MealsHandlerClicks,
    SearchView.OnQueryTextListener {

    lateinit var firebase: FirebaseDatabase
    lateinit var myRef: DatabaseReference

    lateinit var root: View
    lateinit var thread: Thread

    var adapter: MealAdapter? = null
    var meals: ArrayList<Meal>? = null
    var mealSearch: ArrayList<Meal>? = null

    lateinit var restaurant: Restaurant

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_restauarent_meals, container, false)

        root.restaurant_meals_toolbar.inflateMenu(R.menu.meal_menu)

        val searchView =
            MenuItemCompat.getActionView(root.restaurant_meals_toolbar.menu.findItem(R.id.search)) as SearchView
        searchView.setOnQueryTextListener(this)

        root.restaurant_meals_toolbar.setOnMenuItemClickListener { item ->
            if (item.itemId == R.id.backArrow)
            {
                requireActivity().onBackPressed()
            }
            onOptionsItemSelected(item)
        }

        //(activity!! as AppCompatActivity).supportActionBar!!.hide()

        //root.restaurant_meals_toolbar.title = "وجبات المطعم"
        //root.restaurant_meals_toolbar.setTitleTextColor(Color.WHITE)

        /*root.restaurant_meals_toolbar.setOnMenuItemClickListener {
            if(fragmentManager!!.backStackEntryCount > 0){
                fragmentManager!!.popBackStackImmediate()
            } else {
                activity!!.supportFragmentManager.beginTransaction().replace(R.id.main_container, RestaurantDetails().apply { arguments = this.arguments })
                activity!!.nav_view.selectedItemId = R.id.home
            }
            onOptionsItemSelected(it)
        }*/

        return root
    } // end of onCreateView

    override fun onResume() {
        super.onResume()

        //initialize database
        firebase = FirebaseDatabase.getInstance()
        myRef = firebase.reference

        val bundle = arguments
        if (bundle != null)
        {
            // get restaurant object from "RestaurantDetails" by Bundle
            restaurant = bundle.getParcelable<Restaurant>(BundleKeys.BUNDLE_RESTAURANT)!!
        } else
        {
            Toast.makeText(activity, "No Data", Toast.LENGTH_SHORT).show()
        }

        //show progress bar until data
        root.restaurant_meals_ProgressBar.visibility = View.VISIBLE
        root.restaurant_meals_recycler.alpha = 0.0f

        meals = ArrayList()
        mealSearch = ArrayList()
        meals!!.clear()
        adapter = MealAdapter(requireContext(), meals!!, this)

        root.restaurant_meals_recycler.layoutManager = GridLayoutManager(requireActivity(), 2)
        root.restaurant_meals_recycler.adapter = adapter

        myRef.child(DatabaseKeys.RESTAURANT_MEALS)
            .addValueEventListener(object : ValueEventListener {

                override fun onCancelled(data: DatabaseError) {

                }

                override fun onDataChange(data: DataSnapshot) {
                    meals!!.clear()
                    for (meal in data.children)
                    {
                        val m = meal.getValue(Meal::class.java)
                        if(m!!.restaurantId.equals(arguments!!.getParcelable<Restaurant>(BundleKeys.BUNDLE_RESTAURANT)!!.id))
                        {
                            meals!!.add(0, m!!)
                        }
                    }
                    adapter!!.notifyDataSetChanged()

                    //hide progress bar and show the recycler (with animation)
                    thread = Thread(Runnable {
                        try {
                            activity!!.runOnUiThread { root.restaurant_meals_ProgressBar.animate().alpha(0.0f).duration = 1500}
                            Thread.sleep(1000)
                            activity!!.runOnUiThread { root.restaurant_meals_ProgressBar.visibility = View.GONE }
                            activity!!.runOnUiThread { root.restaurant_meals_recycler.animate().alpha(1.0f).duration = 1000 }
                        } catch (e:Exception){}
                    })
                    thread.start()
                }
            })

    } // end of onResume

    // when user click on any meal on the restaurant meals
    override fun onCardClick(position: Int) {
        val bundle = Bundle()
        val meal = meals!![position]
        // send meal object to "MealDetails" by Bundle
        bundle.putParcelable(BundleKeys.BUNDLE_MEAL, meal)
        bundle.putParcelable(BundleKeys.BUNDLE_RESTAURANT,requireArguments().getParcelable<Restaurant>(
            BundleKeys.BUNDLE_RESTAURANT)!!)

        val fragment = MealDetailsFragment()
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .addToBackStack(null).replace(R.id.main_container, fragment).commit()
        /*activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .addToBackStack(null).replace(R.id.main_container, fragment).commit()*/
    }

    // For Search

    override fun onQueryTextSubmit(query: String?): Boolean {
        search(query)
        if (mealSearch!!.isEmpty())
        {
            Snackbar.make(restaurant_meals_recycler, "لا يوجد وجبة تحتوي على هذا الاسم", Snackbar.LENGTH_LONG).show()
            root.restaurant_meals_no_result_image.visibility = View.VISIBLE
            root.restaurant_meals_no_result_tv.visibility = View.VISIBLE
        } else
        {
            root.restaurant_meals_no_result_image.visibility = View.GONE
            root.restaurant_meals_no_result_tv.visibility = View.GONE
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        search(newText)
        if (mealSearch!!.isEmpty() && newText!!.isNotEmpty())
        {
            Snackbar.make(restaurant_meals_recycler, "لا يوجد وجبة تحتوي على هذا الاسم", Snackbar.LENGTH_LONG).show()
            root.restaurant_meals_no_result_image.visibility = View.VISIBLE
            root.restaurant_meals_no_result_tv.visibility = View.VISIBLE
        } else
        {
            root.restaurant_meals_no_result_image.visibility = View.GONE
            root.restaurant_meals_no_result_tv.visibility = View.GONE
        }
        return false
    }

    var name = ""
    private fun search(searchName: String?) {
        mealSearch!!.clear()
        for (i in 0 until meals!!.size)
        {
            name = meals!![i].name!!
            if (name.toLowerCase().contains(searchName!!.toLowerCase()))
            {
                mealSearch!!.add(meals!![i])
            }
        }

        adapter = MealAdapter(requireContext(), mealSearch!!, this)
        restaurant_meals_recycler.layoutManager = GridLayoutManager(activity, 2)
        restaurant_meals_recycler.adapter = adapter
    }

    /*override fun onDestroy() {
        (activity!! as AppCompatActivity).supportActionBar!!.show()
        super.onDestroy()
    }*/

}