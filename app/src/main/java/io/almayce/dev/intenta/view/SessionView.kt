package io.almayce.dev.intenta.view

import com.arellomobile.mvp.MvpView
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType

/**
 * Created by almayce on 20.07.17.
 */
@StateStrategyType(AddToEndSingleStrategy::class)
interface SessionView : MvpView {
    fun purchase()
    fun addSessionDialog()
    fun showToast(text: String)
}
