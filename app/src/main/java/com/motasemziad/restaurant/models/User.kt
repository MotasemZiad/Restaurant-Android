package com.motasemziad.restaurant.models

import android.os.Parcel
import android.os.Parcelable
import com.google.android.gms.maps.model.LatLng

class User(): Parcelable {

    var id:String = ""
    var url:String = ""
    var name:String = ""
    var email:String = ""
    var password:String = ""
    var phone:String = ""
    var address:String = ""
    var latitude:Float = 0f
    var longitude:Float = 0f

    constructor(parcel: Parcel) : this() {
        id = parcel.readString()!!
        url = parcel.readString()!!
        name = parcel.readString()!!
        email = parcel.readString()!!
        password = parcel.readString()!!
        phone = parcel.readString()!!
        address = parcel.readString()!!
        latitude = parcel.readFloat()
        longitude = parcel.readFloat()
    }

    constructor(id:String,url:String,name:String,email:String,password:String,phone:String,address:String,latitude:Float, longitude:Float) : this() {
        this.id = id
        this.url = url
        this.name = name
        this.email = email
        this.password = password
        this.phone = phone
        this.address = address
        this.latitude = latitude
        this.longitude = longitude
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(url)
        parcel.writeString(name)
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(phone)
        parcel.writeString(address)
        parcel.writeFloat(latitude)
        parcel.writeFloat(longitude)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }

}