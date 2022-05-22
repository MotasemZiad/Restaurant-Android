package com.motasemziad.restaurant.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.NonNull
import androidx.viewpager.widget.PagerAdapter
import com.motasemziad.restaurant.R
import com.motasemziad.restaurant.models.ScreenItem
import kotlinx.android.synthetic.main.layout_screen.view.*

class IntroViewPagerAdapter(mContext:Context, mListScreen:List<ScreenItem>): PagerAdapter() {
    internal var mContext: Context
    internal var mListScreen:List<ScreenItem>

    override fun getCount(): Int {
        return mListScreen.size
    }

    init{
        this.mContext = mContext
        this.mListScreen = mListScreen
    }
    @NonNull
    override fun instantiateItem(@NonNull container:ViewGroup, position:Int):Any {
        val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layoutScreen = inflater.inflate(R.layout.layout_screen, null)
        val imgSlide = layoutScreen.intro_img
        val title = layoutScreen.intro_title
        val description = layoutScreen.intro_description
        title.setText(mListScreen.get(position).title)
        description.setText(mListScreen.get(position).description)
        imgSlide.setImageResource(mListScreen.get(position).screenImg)
        container.addView(layoutScreen)
        return layoutScreen
    }
    override fun isViewFromObject(@NonNull view:View, @NonNull o:Any):Boolean {
        return view === o
    }
    override fun destroyItem(@NonNull container: ViewGroup, position:Int, @NonNull `object`:Any) {
        container.removeView(`object` as View)
    }
}