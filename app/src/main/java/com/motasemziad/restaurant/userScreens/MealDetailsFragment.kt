package com.motasemziad.restaurant.userScreens

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.adapters.MealAdapter2
import com.motasemziad.restaurant.keys.BundleKeys
import com.motasemziad.restaurant.keys.DatabaseKeys
import com.motasemziad.restaurant.models.Meal
import com.motasemziad.restaurant.models.Rate
import com.motasemziad.restaurant.models.Restaurant
import com.motasemziad.restaurant.sharedPreferences.MySharedPrefernces
import kotlinx.android.synthetic.main.custome_rating_dialog.*
import kotlinx.android.synthetic.main.fragment_meal_details.*
import kotlinx.android.synthetic.main.fragment_meal_details.view.*

class MealDetailsFragment : Fragment(), MealAdapter2.MealsHandlerClicks {

    lateinit var root: View
    lateinit var meals: ArrayList<Meal>
    lateinit var adapter: MealAdapter2
    lateinit var thread: Thread

    //for database
    private lateinit var firebase: FirebaseDatabase
    private lateinit var myRef: DatabaseReference

    //from bundle
    lateinit var meal: Meal

    //current user id
    var userId = ""

    //to check if user rate this meal before
    var ratedBefore = false
    var oldRate = 0.0f
    var oldRateKey = ""

    //to get meal rate
    var finalMealRate = 0.0f


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //initialize database
        firebase = FirebaseDatabase.getInstance()
        myRef = firebase.getReference()

        meal = requireArguments().getParcelable<Meal>(BundleKeys.BUNDLE_MEAL)!!

        MySharedPrefernces.prepare(requireContext())
        userId = MySharedPrefernces.getUserId()


        myRef.child(DatabaseKeys.MEALS_RATE).child(meal.mealId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {

                    //rating algorithm
                    var rate5 = 0f
                    var rate4 = 0f
                    var rate3 = 0f
                    var rate2 = 0f
                    var rate1 = 0f

                    for (rate in p0.children) {
                        var r = rate.getValue(Rate::class.java)

                        //to see if this user rate meal before or not
                        if (r!!.userId == userId) {
                            ratedBefore = true
                            oldRate = r!!.rate
                            oldRateKey = r!!.rateKey
                        }

                        //to calac meal rate
                        if (r.rate == 5f) {
                            rate5 += 1
                        } else if (r.rate == 4f) {
                            rate4 += 1
                        } else if (r.rate == 3f) {
                            rate3 += 1
                        } else if (r.rate == 2f) {
                            rate2 += 1
                        } else {
                            rate1 += 1
                        }
                    }
                    finalMealRate =
                        (5 * rate5 + 4 * rate4 + 3 * rate3 + 2 * rate2 + 1 * rate1) / (rate5 + rate4 + rate3 + rate2 + rate1)
                    meal_details_rate.rating = finalMealRate
                }

            })

        setHasOptionsMenu(true)

        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_meal_details, container, false)

        root.meal_details_toolbar.inflateMenu(R.menu.rate_toolbar)

        root.meal_details_toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.rating_star -> {
                    Dialog(requireActivity()).apply {
                        setContentView(R.layout.custome_rating_dialog)
                        if (ratedBefore) {
                            custome_rating_bar_msg.setText("لقد قمت بأعطاء هذا التقييم للوجبة سابقا سيتم تعديله اذا قمت بالتقييم الان")
                            custome_rating_dialog_ratingBar.rating = oldRate
                            if (oldRate == 5f) {
                                custome_rating_dialog_textResult.setText("ممتاز")
                            } else if (oldRate == 1f) {
                                custome_rating_dialog_textResult.setText("سيئ")
                            } else if (oldRate == 2f) {
                                custome_rating_dialog_textResult.setText("مقبول")
                            } else if (oldRate == 3f) {
                                custome_rating_dialog_textResult.setText("جيد")
                            } else if (oldRate == 4f) {
                                custome_rating_dialog_textResult.setText("جيد جداً")
                            }
                        } else {
                            custome_rating_bar_msg.setText("تقييمك الوجبة")
                            custome_rating_dialog_textResult.setText("جيد")
                            custome_rating_dialog_ratingBar.rating = 3f
                        }

                        custome_rating_dialog_ratingBar.setOnRatingBarChangeListener { ratingBar, rating, fromUser ->
                            if (fromUser) {
                                if (rating == 0f) {
                                    ratingBar.rating = 1f
                                } else if (rating == 1f) {
                                    custome_rating_dialog_textResult.setText("سيئ")
                                } else if (rating == 2f) {
                                    custome_rating_dialog_textResult.setText("مقبول")
                                } else if (rating == 3f) {
                                    custome_rating_dialog_textResult.setText("جيد")
                                } else if (rating == 4f) {
                                    custome_rating_dialog_textResult.setText("جيد جداً")
                                } else {
                                    custome_rating_dialog_textResult.setText("ممتاز")
                                }
                            }
                        }//end of rating bar listener

                        custome_rating_dialog_submit_btn.setOnClickListener {
                            var rating = custome_rating_dialog_ratingBar.rating
                            if (ratedBefore) {
                                updateRateOnDatabase(oldRateKey, rating)
                            } else {
                                addRateToDatabase(rating)
                            }
                            dismiss()
                        }

                        window.let {
                            it!!.setLayout(
                                WindowManager.LayoutParams.MATCH_PARENT,
                                WindowManager.LayoutParams.WRAP_CONTENT
                            )
                        }
                    }.show()
                }

                R.id.backArrowFromMealDetails -> {
                    requireActivity().onBackPressed()
                }
            }
            onOptionsItemSelected(it)
        }

        meals = ArrayList<Meal>()
        adapter = MealAdapter2(requireContext(), meals, this)
        thread = Thread()

        val bundle = arguments
        if (bundle != null) {
            // get meal object from Bundle
            val meal = bundle.getParcelable<Meal>(BundleKeys.BUNDLE_MEAL)

            Glide.with(requireActivity()).load(meal!!.image).into(root.meal_details_image)
            root.meal_details_name.text = meal.name
            root.meal_details_description.text = meal.description
            root.meal_details_rate.rating = meal.rate
            root.meal_details_prise.text = meal.prise.toString()
        }

        // when user click on meal purchase button
        root.btnMealPurchase.setOnClickListener {
            AlertDialog.Builder(activity)
                .setTitle("شراء")
                .setMessage("هل انت متأكد انك تريد شراء هذه الوجبة ؟")
                .setPositiveButton("نعم") { _, _ ->
                    addPurchasedMealToDatabase()
                }
                .setNegativeButton("لا") { dialog, which ->
                    dialog.dismiss()
                }
                .create().show()
        }

        return root
    } // end of onCreateView

    // when user click on any meal in meal details
    override fun onCardClick(position: Int) {
        val bundle = Bundle()
        val meal = meals[position]
        // send meal object to "MealDetails" by Bundle
        bundle.putParcelable(BundleKeys.BUNDLE_MEAL, meal)

        val fragment = MealDetailsFragment()
        fragment.arguments = bundle

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment).commit()
        /*activity!!.supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .addToBackStack(null).replace(R.id.main_container, fragment).commit()*/
    }

    override fun onResume() {
        super.onResume()

        root.meal_detail_recycler.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.HORIZONTAL, false
        )
        root.meal_detail_recycler.adapter = adapter

        //initialize database
        val firebase = FirebaseDatabase.getInstance()
        val myRef = firebase.reference

        myRef.child(DatabaseKeys.RESTAURANT_MEALS)
            .addValueEventListener(object : ValueEventListener {

                override fun onCancelled(data: DatabaseError) {

                }

                override fun onDataChange(data: DataSnapshot) {
                    meals.clear()
                    for (meal in data.children) {
                        val m = meal.getValue(Meal::class.java)
                        if (m!!.restaurantId!! == arguments!!.getParcelable<Meal>(BundleKeys.BUNDLE_MEAL)!!.restaurantId
                            && m.mealId!! != arguments!!.getParcelable<Meal>(BundleKeys.BUNDLE_MEAL)!!.mealId
                        )
                            meals.add(0, m)
                    }
                    if(meals.size == 0)
                        root.meal_detail_recycler.visibility=View.GONE
                    else
                        root.meal_detail_recycler.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()


                    //hide progress bar and show the recycler (with animation)
                    thread = Thread(Runnable {
                        try {
                            activity!!.runOnUiThread {
                                root.meal_details__progress.animate().alpha(0.0f).duration = 1500
                            }
                            Thread.sleep(1000)
                            activity!!.runOnUiThread {
                                root.meal_details__progress.visibility = View.GONE
                            }
                            activity!!.runOnUiThread {
                                root.meal_detail_recycler.animate().alpha(1.0f).duration = 1000
                            }
                        } catch (e: Exception) {
                        }
                    })
                    thread.start()

                    //there is no data
                    if (meals.size == 0)
                        root.meal_details_noResult.visibility = View.VISIBLE
                    else
                        root.meal_details_noResult.visibility = View.GONE
                }
            })

    } // end of onResume

    /**this fun add the rate of meal to database*/
    private fun addRateToDatabase(rating: Float) {
        //stor user rate on datbase
        MySharedPrefernces.prepare(requireContext())
        var userId = MySharedPrefernces.getUserId() //user id

        myRef.child(DatabaseKeys.RESTAURANTS).child(meal.restaurantId!!).addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var restaurant = p0.getValue(Restaurant::class.java)

                    var key = myRef.child(DatabaseKeys.MEALS_RATE).child(meal.mealId!!).push().key
                    myRef.child(DatabaseKeys.MEALS_RATE).child(meal.mealId!!).child(key!!).setValue(
                        Rate(key,userId,restaurant!!.id!!,meal.mealId!!,rating)
                    )
                }
            })
    }

    /**this method is to update the fucking meal rate*/
    private fun updateRateOnDatabase(rateId:String, rating:Float) {
        //stor user rate on datbase
        MySharedPrefernces.prepare(requireContext())
        var userId = MySharedPrefernces.getUserId() //user id

        myRef.child(DatabaseKeys.RESTAURANTS).child(meal.restaurantId!!).addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    var restaurant = p0.getValue(Restaurant::class.java)

                    myRef.child(DatabaseKeys.MEALS_RATE).child(meal.mealId!!).child(rateId).setValue(
                        Rate(rateId,userId,restaurant!!.id!!,meal.mealId!!,rating)
                    )
                }
            })
    }

    /** this method to add the meal that user purchased to database*/
    private fun addPurchasedMealToDatabase() {
        root.btnMealPurchase.isActivated = false

        var hashmap = hashMapOf<String,Any>(
            "saleTime" to meal.saleTime+1
        )
        myRef.child(DatabaseKeys.RESTAURANT_MEALS)
            .child(meal.mealId!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                meal.saleTime+=1
                var key = myRef.child(DatabaseKeys.USER_ORDERS).push().key
                myRef.child(DatabaseKeys.USER_ORDERS)
                    .child(userId)
                    .child(key!!)
                    .setValue(meal).addOnSuccessListener {
                        AlertDialog.Builder(requireContext()).apply{
                            setTitle("تأكيد العملية")
                            setMessage("تم تأكيد عملية الشراء سيتم ارسال طلبك للمطعم")
                            setCancelable(true)
                            setPositiveButton("حسناً"){dialog,which->
                                root.btnMealPurchase.isActivated = true
                                dialog.dismiss()
                            }
                            create().show()
                        }
                    }
            }
    }

}