package io.almayce.dev.intenta.presenter

import android.graphics.Bitmap
import android.os.AsyncTask
import com.arellomobile.mvp.InjectViewState
import com.arellomobile.mvp.MvpPresenter
import io.almayce.dev.intenta.global.BitmapManager
import io.almayce.dev.intenta.global.SelectedPoint
import io.almayce.dev.intenta.global.SelectedSession
import io.almayce.dev.intenta.model.FirebaseManager
import io.almayce.dev.intenta.model.ObservableList
import io.almayce.dev.intenta.model.Point
import io.almayce.dev.intenta.view.ProcessView

/**
 * Created by almayce on 09.08.17.
 */

@InjectViewState
class ProcessPresenter : MvpPresenter<ProcessView>() {

    private lateinit var points: ObservableList<Point>
    private var bitmapManager = BitmapManager()
    private var indexPoint = 0;

    fun initMode(mode: Int) {
        points = ObservableList()
        when {
            mode == 1 -> {
                for (p: Point in FirebaseManager.listPoint.getList()!!)
                    if (p.status == "Undone")
                        points.add(p)
            }
            mode == 2 -> {
                FirebaseManager.resetStatus()
                points = FirebaseManager.listPoint
            }

            mode == 3 -> {
                indexPoint = FirebaseManager.getSessionProgress(SelectedSession.idSession)
                points = FirebaseManager.listPoint
            }
        }

        if (points.isEmpty) {
            viewState.onBackPressed()
            viewState.showToast("Session completed.")
        } else
            setInfo()
    }

    fun onNext(done: Boolean) {
        viewState.setPhoto(null)
        SelectedPoint.setPoint(points.get(indexPoint))

        if (!done)
            SelectedPoint.status = "Undone"
        else
            SelectedPoint.status = "Done"

        FirebaseManager.updatePoint()
        indexPoint++
        checkIndex()
    }

    fun checkIndex() {
        if (indexPoint == points.size()) {
            SelectedSession.progress = 0
            FirebaseManager.updateSessionProgress()
            viewState.onBackPressed()
        } else {
            SelectedSession.progress = indexPoint
            FirebaseManager.updateSessionProgress()
            setInfo()
        }
    }

    private fun setInfo() {
        viewState.showPointInfo(points.get(indexPoint),
                indexPoint + 1,
                points.size())
    }

    fun getImage() {
        var url = points.get(indexPoint).photo
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