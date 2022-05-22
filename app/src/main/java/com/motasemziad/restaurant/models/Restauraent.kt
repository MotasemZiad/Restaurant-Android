package com.motasemziad.restaurant.models

import android.os.Parcel
import android.os.Parcelable

class Restaurant() : Parcelable
{
    var id: String? = ""
    var image: String = ""
    var name: String = ""
    var rate: Float = 0f
    var address: String? = ""
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var distance:Double  = 0.0

    constructor(
        id: String,
        image: String,
        name: String,
        rate: Float,
        address: String,
        latitude: Double,
        longitude: Double
    ) : this() {
        this.id = id
        this.image = image
        this.name = name
        this.rate = rate
        this.address = address
        this.latitude = latitude
        this.longitude = longitude
    }

    constructor(
        id: String,
        image: String,
        name: String,
        rate: Float,
        address: String,
        latitude: Double,
        longitude: Double,
        distance:Double
    ) : this() {
        this.id = id
        this.image = image
        this.name = name
        this.rate = rate
        this.address = address
        this.latitude = latitude
        this.longitude = longitude
        this.distance = distance
    }

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()
        image = parcel.readString()!!
        name = parcel.readString()!!
        rate = parcel.readFloat()
        address = parcel.readString()
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        distance = parcel.readDouble()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(image)
        parcel.writeString(name)
        parcel.writeFloat(rate)
        parcel.writeString(address)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeDouble(distance)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Restaurant> {
        override fun createFromParcel(parcel: Parcel): Restaurant {
            return Restaurant(parcel)
        }

        override fun newArray(size: Int): Array<Restaurant?> {
            return arrayOfNulls(size)
        }
    }

}