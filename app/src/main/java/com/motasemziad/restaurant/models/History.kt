package com.motasemziad.restaurant.models

import android.os.Parcel
import android.os.Parcelable

class History : Parcelable {
    var historyId: String = ""
    var actionId = ""
    var name: String = ""
    var title: String = ""
    var details: String = ""
    var date: String = ""

    /*
    * those vars to determine whether the operation is
    * delete or edit or add (so we can pick right image)
    * */
    var isItRestaurant: Boolean = false
    var isItAddOperation = false
    var isItEditOperation = false
    var isItDeleteOperation = false

    constructor()

    constructor(
        historyId:String,
        actionId:String,
        name:String,
        title:String,
        details:String,
        date:String,
        isItRestaurant:Boolean,
        isItAddOperation:Boolean,
        isItEditOperation:Boolean,
        isItDeleteOperation:Boolean)
    {
        this.historyId = historyId
        this.actionId = actionId
        this.name = name
        this.title = title
        this.details = details
        this.date = date
        this.isItRestaurant = isItRestaurant
        this.isItAddOperation = isItAddOperation
        this.isItEditOperation = isItEditOperation
        this.isItDeleteOperation = isItDeleteOperation
    }

    constructor(parcel: Parcel) : this() {
        historyId = parcel.readString()!!
        actionId = parcel.readString()!!
        name = parcel.readString()!!
        title = parcel.readString()!!
        details = parcel.readString()!!
        date = parcel.readString()!!
        isItRestaurant = parcel.readByte() != 0.toByte()
        isItAddOperation = parcel.readByte() != 0.toByte()
        isItEditOperation = parcel.readByte() != 0.toByte()
        isItDeleteOperation = parcel.readByte() != 0.toByte()
    }


    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(historyId)
        parcel.writeString(actionId)
        parcel.writeString(name)
        parcel.writeString(title)
        parcel.writeString(details)
        parcel.writeString(date)
        parcel.writeByte(if (isItRestaurant) 1 else 0)
        parcel.writeByte(if (isItAddOperation) 1 else 0)
        parcel.writeByte(if (isItEditOperation) 1 else 0)
        parcel.writeByte(if (isItDeleteOperation) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<History> {
        override fun createFromParcel(parcel: Parcel): History {
            return History(parcel)
        }

        override fun newArray(size: Int): Array<History?> {
            return arrayOfNulls(size)
        }
    }
}