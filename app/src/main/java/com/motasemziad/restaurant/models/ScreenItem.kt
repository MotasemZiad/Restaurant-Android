package com.motasemziad.restaurant.models

class ScreenItem(title:String, description:String, screenImg:Int) {
    var title:String
    var description:String
    var screenImg:Int = 0
    init{
        this.title = title
        this.description = description
        this.screenImg = screenImg
    }
}