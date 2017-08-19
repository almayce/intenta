package io.almayce.dev.intenta.presenter

import android.app.Activity
import android.graphics.Bitmap
import android.os.AsyncTask
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.almayce.dev.intenta.adapter.CustomCheckAdapter
import io.almayce.dev.intenta.global.BitmapManager
import io.almayce.dev.intenta.global.SchedulersTransformer
import io.almayce.dev.intenta.global.SelectedPoint
import io.almayce.dev.intenta.global.SelectedSession
import io.almayce.dev.intenta.model.FirebaseManager
import io.almayce.dev.intenta.view.CheckView

/**
 * Created by almayce on 20.07.17.
 */

@InjectViewState
class CheckPresenter : MvpPresenter<CheckView>() {

    lateinit var adapter: CustomCheckAdapter
    private var bitmapManager = BitmapManager()

    fun onCreate(activity: Activity) {
        adapter = CustomCheckAdapter(activity, FirebaseManager.listPoint)

        bitmapManager.onTransformedFileObservable
                .compose(SchedulersTransformer())
                .subscribe({ it ->
                    FirebaseManager.addPointPhoto(it)
                })

        bitmapManager.onTransformedBitmapObservable
                .compose(SchedulersTransformer())
                .subscribe({ it ->
                    viewState.setPhoto(it)
                })

        FirebaseManager.listPoint.onAddObservable
                .compose(SchedulersTransformer())
                .subscribe({ t ->
                    adapter.notifyDataSetChanged()
                })

        FirebaseManager.listPoint.onRemoveObservable
                .compose(SchedulersTransformer())
                .subscribe({ t ->
                    adapter.notifyDataSetChanged()
                    setInfo(0)
                })

        FirebaseManager.onPointLoadedObservable
                .compose(SchedulersTransformer())
                .take(1)
                .subscribe({ t ->
                    setInfo(0)
                })
    }

    fun read() {
        FirebaseManager.readPoint(SelectedSession.idSession)
    }

    fun setInfo(position: Int) {
        try {
            FirebaseManager.selectPoint(position)
            setPointInfo()
        } catch (e: IndexOutOfBoundsException) {
            e.printStackTrace()
            viewState.addPointDialog()
        }

    }

    fun setTitle() {
        viewState.setTitle(SelectedSession.title)
    }

    fun addPoint(title: String) {
        FirebaseManager.addPoint(title)
    }


    fun updatePoint() {
        setPointInfo()
        FirebaseManager.updatePoint()
    }

    private fun setPointInfo() {
        viewState.setInfo(
                SelectedPoint.title,
                SelectedPoint.description,
                SelectedPoint.photo)
    }

    fun removePoint() {
        FirebaseManager.removePoint()
    }

    fun removePoint(position: Int) {
        FirebaseManager.removePoint(position)
    }

    fun listIsEmpty(): Boolean {
        return FirebaseManager.listPoint.isEmpty
    }

    fun transformBitmap(path: String) {
        bitmapManager.transformBitmap(path)
    }

    fun getImageFromURL(url: String) {
        if (!url.isNullOrEmpty())
            object : AsyncTask<Void, Bitmap?, Bitmap>() {
                override fun doInBackground(vararg params: Void): Bitmap? {
                    return bitmapManager.getBitmapFromURL(url)
                }

                override fun onPostExecute(bitmap: Bitmap?) {
                    viewState.setPhoto(bitmap)
                }
            }.execute()
    }


}
