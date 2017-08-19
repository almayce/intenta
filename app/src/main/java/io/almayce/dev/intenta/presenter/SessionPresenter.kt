package io.almayce.dev.intenta.presenter

import android.app.Activity
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.almayce.dev.intenta.adapter.CustomSessionAdapter
import io.almayce.dev.intenta.global.SchedulersTransformer
import io.almayce.dev.intenta.model.FirebaseManager
import io.almayce.dev.intenta.view.SessionView

/**
 * Created by almayce on 20.07.17.
 */
@InjectViewState
class SessionPresenter : MvpPresenter<SessionView>() {

    lateinit var adapter: CustomSessionAdapter
    var status = false

    fun onCreate(activity: Activity) {
        adapter = CustomSessionAdapter(activity, FirebaseManager.listSession)
        FirebaseManager.getPremiumStatusForAddDialog()

        FirebaseManager.listSession.onAddObservable
                .compose(SchedulersTransformer())
                .subscribe({ t ->
                    adapter.notifyDataSetChanged()
                })

        FirebaseManager.listSession.onRemoveObservable
                .compose(SchedulersTransformer())
                .subscribe({ t -> adapter.notifyDataSetChanged() })

        FirebaseManager.onPremiumStatusObservable
                .compose(SchedulersTransformer())
                .subscribe({ it ->
                    if (it != true)
                        viewState.purchase()
                    else viewState.showToast("Premium account activated.")
                })

        FirebaseManager.onPremiumStatusForAddDialogObservable
                .compose(SchedulersTransformer())
                .subscribe({ it ->
                    status = it
                })
    }

    fun getId(position: Int): String {
        return FirebaseManager.listSession.get(position).id
    }

    fun getTitle(position: Int): String {
        return FirebaseManager.listSession.get(position).title
    }

    fun read() {
        FirebaseManager.readSession()
    }

    fun addRequest() {
        if (FirebaseManager.listSession.size() >= 3)
            if (status) {
                viewState.addSessionDialog()
            } else viewState.purchase()
        else viewState.addSessionDialog()
    }

    fun addSession(title: String) {
        FirebaseManager.addSession(title)
    }

    fun removeSession(position: Int) {
        FirebaseManager.removeSession(position)
    }

    fun getPremiumStatus() {
        FirebaseManager.getPremiumStatus()
    }
}
