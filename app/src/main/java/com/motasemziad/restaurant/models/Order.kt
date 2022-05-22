package com.motasemziad.restaurant.models

import android.os.Parcel
import android.os.Parcelable

class Order() : Parcelable
{
    var orderKey = ""
    var userId = ""
    var restaurantId = ""
    var mealId = ""

    constructor(parcel: Parcel) : this() {
        orderKey = parcel.readString()!!
        userId = parcel.readString()!!
        restaurantId = parcel.readString()!!
        mealId = parcel.readString()!!
    }

    constructor(orderKey:String,userId:String,restaurantId:String,mealId:String) : this() {
        this.orderKey = orderKey
        this.userId = userId
        this.restaurantId = restaurantId
        this.mealId = mealId
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(orderKey)
        parcel.writeString(userId)
        parcel.writeString(restaurantId)
        parcel.writeString(mealId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Order> {
        override fun createFromParcel(parcel: Parcel): Order {
            return Order(parcel)
        }

        override fun newArray(size: Int): Array<Order?> {
            return arrayOfNulls(size)
        }
    }

}