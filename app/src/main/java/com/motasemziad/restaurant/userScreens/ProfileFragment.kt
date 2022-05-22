package com.motasemziad.restaurant.userScreens

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.adapters.MealAdapter2
import com.motasemziad.restaurant.keys.BundleKeys
import com.motasemziad.restaurant.keys.DatabaseKeys
import com.motasemziad.restaurant.models.Meal
import com.motasemziad.restaurant.sharedPreferences.MySharedPrefernces
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment(), MealAdapter2.MealsHandlerClicks {

    lateinit var firebase: FirebaseDatabase
    lateinit var myRef: DatabaseReference

    lateinit var root: View
    lateinit var thread: Thread

    lateinit var meals:ArrayList<Meal>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_profile, container, false)

        meals = ArrayList<Meal>()


        return root
    } // end of onCreateView

    override fun onResume() {
        super.onResume()

        //initialize database
        firebase = FirebaseDatabase.getInstance()
        myRef = firebase.reference

        //get meals that user order before
        myRef.child(DatabaseKeys.USER_ORDERS)
            .child(MySharedPrefernces.getUserId())
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(p0: DataSnapshot) {
                    meals = ArrayList<Meal>()
                    for(meal in p0.children)
                    {
                        meals.add(meal.getValue(Meal::class.java)!!)
                    }

                    if (meals.isEmpty())
                        root.profile_no_data.visibility = View.VISIBLE
                    else
                        root.profile_no_data.visibility = View.GONE

                    try{
                        root.profile_recycler.adapter = MealAdapter2(context!!,meals,this@ProfileFragment)
                    }catch(e:Exception){}
                    root.profile_recycler.layoutManager = LinearLayoutManager(context,
                        RecyclerView.HORIZONTAL,false)
                    root.profile_progress.visibility = View.GONE
                    root.profile_recycler.animate().alpha(1.0f).duration = 1500
                }

            })

        //show progress bar until data
        root.profile_progress.visibility = View.VISIBLE
        root.profile_progress.alpha = 0.0f

        root.profile_recycler.layoutManager = LinearLayoutManager(
            requireActivity(),
            RecyclerView.HORIZONTAL, false
        )

        /*myRef.child(DatabaseKeys.USER_MEALS)
            .addValueEventListener(object : ValueEventListener {

                override fun onCancelled(p0: DatabaseError) {

                }

                override fun onDataChange(data: DataSnapshot) {
                    for (meal in data.children)
                    {

                    }
                    adapter!!.notifyDataSetChanged()

                    //hide progress bar and show the recycler (with animation)
                    thread = Thread(Runnable {
                        try {
                            activity!!.runOnUiThread { root.profile_progress.animate().alpha(0.0f).duration = 1500}
                            Thread.sleep(1000)
                            activity!!.runOnUiThread { root.profile_progress.visibility = View.GONE }
                            activity!!.runOnUiThread { root.profile_recycler.animate().alpha(1.0f).duration = 1000 }
                        } catch (e:Exception){}
                    })
                    thread.start()
                }

            })*/

        MySharedPrefernces.prepare(requireActivity().applicationContext)
        Glide.with(requireContext()).load(MySharedPrefernces.getUserImage())
            .placeholder(R.drawable.place_holder_image).into(root.profile_image)

        Log.e("profile",MySharedPrefernces.getUserName())
        //Glide.with(context!!).load(MySharedPrefernces.getUserImage()).into(root.view)
        root.profile_name.setText(MySharedPrefernces.getUserName())

        // when user click on profile image
        /*root.profile_image.setOnClickListener {

            // to customize the image dialog
            val pickSetup = PickSetup()
            pickSetup.title = "تعيين صورة الملف الشخصي"
            pickSetup.cancelText = "الغاء"
            pickSetup.cameraButtonText = "الكاميرا"
            pickSetup.galleryButtonText = "المعرض"
            pickSetup.isSystemDialog = true // to show the system dialog not the default

            // to show the image dialog // here we used the PickImage library for image
            PickImageDialog.build(pickSetup)
                .setOnPickResult { r ->
                    MySharedPrefernces.setUserImage(r.uri.toString()) // save image in shared pref
                    Glide.with(this).load(MySharedPrefernces.getUserImage())
                        .placeholder(R.drawable.place_holder_image).into(root.profile_image)
                    Glide.with(this).load(MySharedPrefernces.getUserImage()).into(root.view)
                }
                .setOnPickCancel {

                }
                .show(activity!!.supportFragmentManager)
        }*/

        // when user click on edit name button
        root.profile_edit_name_button.setOnClickListener {
            if (profile_edit_name_button.text == "تعديل")
            {
                root.profile_edit_name_button.setText("حفظ")
                root.profile_name.isEnabled = true
                root.profile_edit_name_button.text = "حفظ"
                root.profile_edit_name_button.setBackgroundColor(Color.parseColor("#4DBF52"))
                Toast.makeText(activity, "قم بادخال الاسم الجديد", Toast.LENGTH_SHORT).show()
            } else
            {
                if (profile_name.text.isNotEmpty())
                {
                    MySharedPrefernces.setUserName(profile_name.text.toString())

                    //check if user is facebook user or google user
                    if(MySharedPrefernces.getLoginViaFacebook())
                    {
                        myRef.child(DatabaseKeys.FACEBOOK_USERS)
                            .child(MySharedPrefernces.getUserId())
                            .updateChildren(
                                hashMapOf<String,Any>(
                                    "name" to profile_name.text.toString()
                                )
                            )
                    }else{
                        myRef.child(DatabaseKeys.GOOGLE_USERS)
                            .child(MySharedPrefernces.getUserId())
                            .updateChildren(
                                hashMapOf<String,Any>(
                                    "name" to profile_name.text.toString()
                                )
                            )
                    }

                    root.profile_name.isEnabled = false
                    root.profile_edit_name_button.text = "تعديل"
                    root.profile_edit_name_button.setBackgroundColor(
                        ContextCompat.getColor(requireContext(),
                            R.color.colorAccent))
                    Toast.makeText(activity, "تم التعديل بنجاح", Toast.LENGTH_SHORT).show()
                } else
                {
                    Toast.makeText(activity, "يرجى ادخال الاسم أولاً", Toast.LENGTH_SHORT).show()
                }
            }
        }

    } // end of onResume

    override fun onCardClick(position: Int) {
        var mealFragment = MealDetailsFragment()
        mealFragment.arguments = Bundle().apply {
            putParcelable(BundleKeys.BUNDLE_MEAL,meals[position])
        }
        requireActivity().supportFragmentManager.beginTransaction().replace(R.id.main_container,mealFragment).commit()
    }

}