package com.motasemziad.restaurant.models

import android.os.Parcel
import android.os.Parcelable

class Meal() : Parcelable
{
    var mealId:String? = ""
    var restaurantId: String? = ""
    var image: String? = ""
    var name: String? = ""
    var description: String? = ""
    var rate: Float = 0f
    var prise: Float = 0f
    var saleTime = 0

    constructor(parcel: Parcel) : this() {
        mealId = parcel.readString()
        restaurantId = parcel.readString()
        image = parcel.readString()
        name = parcel.readString()
        description = parcel.readString()
        rate = parcel.readFloat()
        prise = parcel.readFloat()
        saleTime = parcel.readInt()
    }

    constructor(mealId:String?,restaurantId: String?, image: String?, name: String?, description: String?, rate: Float, prise: Float,saleTime:Int) : this() {
        this.mealId = mealId
        this.restaurantId = restaurantId
        this.image = image
        this.name = name
        this.description = description
        this.rate = rate
        this.prise = prise
        this.saleTime = saleTime
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(mealId)
        parcel.writeString(restaurantId)
        parcel.writeString(image)
        parcel.writeString(name)
        parcel.writeString(description)
        parcel.writeFloat(rate)
        parcel.writeFloat(prise)
        parcel.writeInt(saleTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Meal> {
        override fun createFromParcel(parcel: Parcel): Meal {
            return Meal(parcel)
        }

        override fun newArray(size: Int): Array<Meal?> {
            return arrayOfNulls(size)
        }
    }
}