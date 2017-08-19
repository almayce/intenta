package io.almayce.dev.intenta.view

import android.graphics.Bitmap
import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by almayce on 20.07.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface CheckView : MvpView {
    fun setInfo(title: String?, description: String?, photo: String?)
    fun setTitle(title: String?)
    fun setPhoto(bitmap: Bitmap?)
    fun addPointDialog()
}
