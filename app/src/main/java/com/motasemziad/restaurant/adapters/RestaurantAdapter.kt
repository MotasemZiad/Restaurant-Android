package com.motasemziad.restaurant.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.keys.DatabaseKeys
import com.motasemziad.restaurant.models.Rate
import com.motasemziad.restaurant.models.Restaurant
import kotlinx.android.synthetic.main.fragment_restaurant_details.*
import kotlinx.android.synthetic.main.restaurant_item.view.*

class RestaurantAdapter(var context: Context, var restaurantes: ArrayList<Restaurant>, var handlerClicks: HandlerClicks) : RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

    class ViewHolder(item : View) : RecyclerView.ViewHolder(item){
        var image = item.restaurant_item_image
        var name = item.restaurant_item_name
        var address = item.restaurant_item_address
        var rate = item.restaurant_item_rate
        var card = item.restaurant_item_card
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.restaurant_item,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return restaurantes.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Glide.with(context).load(restaurantes[position].image).into(holder.image)
        holder.name.text = restaurantes[position].name
        holder.address.text = restaurantes[position].address
        holder.rate.rating = restaurantes[position].rate

        var myRef = FirebaseDatabase.getInstance().reference
        myRef.child(DatabaseKeys.RESTAURANTS_RATE).child(restaurantes[position].id!!).addValueEventListener(object :
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

                    //to clac restaurant rate
                    if(r!!.rate == 5f){
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
                holder.rate.rating = (5*rate5 + 4*rate4 + 3*rate3 + 2*rate2 + 1*rate1) / (rate5+rate4+rate3+rate2+rate1)
            }

        })


        holder.card.setOnClickListener {
            handlerClicks.onCardClick(position)
        }

    }

    interface HandlerClicks{
        fun onCardClick(position: Int)
    }
}