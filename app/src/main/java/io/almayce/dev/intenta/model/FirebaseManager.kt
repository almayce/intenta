package io.almayce.dev.intenta.model

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.almayce.dev.intenta.global.SelectedPoint
import io.almayce.dev.intenta.global.SelectedSession
import io.reactivex.subjects.PublishSubject
import java.io.File

/**
 * Created by almayce on 02.08.17.
 */
object FirebaseManager {

    private var auth: FirebaseAuth
    private var ref: DatabaseReference
    private var stoRef: StorageReference
    private var user: FirebaseUser

    private lateinit var pointSnapshot: Iterable<DataSnapshot>

    var listSession = ObservableList<Session>()
    var listPoint = ObservableList<Point>()

    val onPremiumStatusObservable = PublishSubject.create<Boolean>()
    val onPremiumStatusForAddDialogObservable = PublishSubject.create<Boolean>()
    var onPointLoadedObservable = PublishSubject.create<Int>()

    init {
        auth = FirebaseAuth.getInstance()
        ref = FirebaseDatabase.getInstance().getReference()
        stoRef = FirebaseStorage.getInstance().getReference()
        user = auth.currentUser as FirebaseUser
    }

    fun setPremiumStatus() {
        ref.child(user.uid)
                .child("premium")
                .setValue(true)
    }

    fun getPremiumStatus() {
        ref.child(user.uid)
                .child("premium")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(o: DataSnapshot) {
                        var premium: Boolean = false
                        try {
                            premium = o.getValue(Boolean::class.java)!!
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }
                        onPremiumStatusObservable.onNext(premium)
                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })
    }

    fun getPremiumStatusForAddDialog() {
        ref.child(user.uid)
                .child("premium")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(o: DataSnapshot) {
                        var premium: Boolean = false
                        try {
                            premium = o.getValue(Boolean::class.java)!!
                        } catch (e: NullPointerException) {
                            e.printStackTrace()
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                        }
                        onPremiumStatusForAddDialogObservable.onNext(premium)

                    }

                    override fun onCancelled(p0: DatabaseError) {
                    }
                })
    }

    fun readSession() {
        var sessionId = StringBuilder()
        var title = StringBuilder()
        var progress = StringBuilder()

        ref.child(user.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(o: DataSnapshot) {
                listSession.clear()
                for (oo: DataSnapshot in o.children)
                    for (ooo: DataSnapshot in oo.children) {
                        for (oooo: DataSnapshot in ooo.children) {
                            var key = oooo.key

                            when {
                                key == "title" -> title.append(oooo.getValue(String::class.java))
                                key == "progress" -> progress.append(oooo.getValue(String::class.java))
                            }

                            if (title.isNotEmpty() && progress.isNotEmpty()) {
                                sessionId.append(ooo.key)
                                listSession.add(Session(
                                        sessionId.toString(),
                                        title.toString(),
                                        progress.toString().toInt()))

                                sessionId.setLength(0)
                                title.setLength(0)
                                progress.setLength(0)
                            }
                        }
                    }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        })
    }

    fun getSessionProgress(id: String?): Int {
        for (s: Session in listSession.getList()!!)
            if (s.id == id) {
                return s.progress
            }
        return 0
    }

    fun addSession(title: String) {
        var map = HashMap<String, String>()
        map.put("title", title)
        map.put("progress", "0")

        ref.child(user.uid)
                .child("session")
                .push()
                .setValue(map)
    }

    fun updateSessionProgress() {
        ref.child(user.uid)
                .child("session")
                .child(SelectedSession.idSession)
                .child("progress")
                .setValue(SelectedSession.progress.toString())
    }

    fun selectPoint(position: Int) {
        SelectedPoint.setPoint(listPoint.get(position))
    }

    fun readPoint(id: String?) {
        var pointId = StringBuilder()
        var title = StringBuilder()
        var status = StringBuilder()
        var description = StringBuilder()
        var photo = StringBuilder()

        ref.child(user.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(o: DataSnapshot) {
                listPoint.clear()
                for (oo: DataSnapshot in o.children)
                    for (ooo: DataSnapshot in oo.children)
                        if (ooo.key == id)
                            for (oooo: DataSnapshot in ooo.children)
                                if (oooo.key == "point") {
                                    pointSnapshot = oooo.children
                                    for (ooooo: DataSnapshot in oooo.children) {
                                        for (oooooo: DataSnapshot in ooooo.children) {
                                            var key = oooooo.key
                                            var value = oooooo.getValue(String::class.java)

                                            when {
                                                key == "title" -> title.append(value)
                                                key == "status" -> status.append(value)
                                                key == "description" -> description.append(value)
                                                key == "photo" -> photo.append(value)
                                            }

                                            if (title.isNotEmpty() && status.isNotEmpty()) {
                                                pointId.append(ooooo.key)
                                                listPoint.add(Point(
                                                        pointId.toString(),
                                                        title.toString(),
                                                        status.toString(),
                                                        description.toString(),
                                                        photo.toString()))

                                                pointId.setLength(0)
                                                title.setLength(0)
                                                status.setLength(0)
                                                description.setLength(0)
                                                photo.setLength(0)
                                            }
                                        }
                                    }
                                    onPointLoadedObservable.onNext(listPoint.size())
                                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    fun addPoint(title: String) {
        var map = HashMap<String, String>()
        map.put("title", title)
        map.put("description", "")
        map.put("status", "Undone")
        map.put("photo", "")

        ref.child(user.uid)
                .child("session")
                .child(SelectedSession.idSession)
                .child("point")
                .push()
                .setValue(map)
    }

    fun updatePoint() {
        var map = HashMap<String, String>()
        map.put("title", SelectedPoint.title!!)
        map.put("description", SelectedPoint.description!!)
        map.put("status", SelectedPoint.status!!)
        map.put("photo", SelectedPoint.photo!!)

        ref.child(user.uid)
                .child("session")
                .child(SelectedSession.idSession)
                .child("point")
                .child(SelectedPoint.idPoint)
                .setValue(map)
    }

    fun resetStatus() {
        for (p: Point in listPoint.getList()!!) {
            var map = HashMap<String, String>()
            map.put("title", p.title)
            map.put("description", p.description)
            map.put("status", "Undone")
            map.put("photo", p.photo)

            ref.child(user.uid)
                    .child("session")
                    .child(SelectedSession.idSession)
                    .child("point")
                    .child(p.idPoint)
                    .setValue(map)
        }
    }

    fun removePoint() {
        ref.child(user.uid)
                .child("session")
                .child(SelectedSession.idSession)
                .child("point")
                .child(SelectedPoint.idPoint)
                .removeValue()
    }

    fun removeSession(position: Int) {
        var s = listSession.get(position)
        ref.child(user.uid)
                .child("session")
                .child(s.id)
                .removeValue()
        listSession.remove(position)
    }

    fun removePoint(position: Int) {
        var s = listPoint.get(position)
        ref.child(user.uid)
                .child("session")
                .child(SelectedSession.idSession)
                .child("point")
                .child(s.idPoint)
                .removeValue()
        listPoint.remove(position)
    }

    fun addPointPhoto(photo: File) {
        stoRef.child(user.uid)
                .child(SelectedSession.idSession!!)
                .child(SelectedPoint.idPoint!!)
                .putFile(Uri.fromFile(photo))
                .addOnSuccessListener { taskSnapshot ->
                    SelectedPoint.photo = taskSnapshot.downloadUrl.toString()
                    FirebaseManager.updatePoint()
                }
    }
}