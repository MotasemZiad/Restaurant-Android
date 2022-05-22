package com.motasemziad.restaurant.models

import android.os.Parcel
import android.os.Parcelable

class Rate() : Parcelable {
    var rateKey = ""
    var userId = ""
    var restaurantId = ""
    var mealId = ""
    var rate = 0.0f

    constructor(parcel: Parcel) : this() {
        rateKey = parcel.readString()!!
        userId = parcel.readString()!!
        restaurantId = parcel.readString()!!
        mealId = parcel.readString()!!
        rate = parcel.readFloat()
    }

    constructor(rateKey:String,userId:String,restaurantId:String,mealId:String,rate:Float) : this() {
        this.rateKey = rateKey
        this.userId = userId
        this.restaurantId = restaurantId
        this.mealId = mealId
        this.rate = rate
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(rateKey)
        parcel.writeString(userId)
        parcel.writeString(restaurantId)
        parcel.writeString(mealId)
        parcel.writeFloat(rate)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Rate> {
        override fun createFromParcel(parcel: Parcel): Rate {
            return Rate(parcel)
        }

        override fun newArray(size: Int): Array<Rate?> {
            return arrayOfNulls(size)
        }
    }
}