package io.almayce.dev.intenta.view

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType
import io.almayce.dev.intenta.model.Point

/**
 * Created by almayce on 09.08.17.
 */

@StateStrategyType(AddToEndSingleStrategy::class)

interface ProcessView : MvpView {
    fun showPointInfo(p: Point, index: Int, count: Int)
    fun setPhoto(b: Bitmap?)
    fun showToast(text: String)
    fun onBackPressed()
}