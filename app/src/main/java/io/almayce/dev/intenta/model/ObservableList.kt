package io.almayce.dev.intenta.model

import io.reactivex.subjects.PublishSubject
import java.util.*

/**
 * Created by almayce on 30.05.17.
 */

class ObservableList<T> {
    private lateinit var list: MutableList<T>
    val onRemoveObservable: PublishSubject<T>
    val onAddObservable: PublishSubject<T>
    private var backup: T? = null

    init {
        this.list = ArrayList<T>()
        this.onRemoveObservable = PublishSubject.create<T>()
        this.onAddObservable = PublishSubject.create<T>()
    }

    fun init(list: MutableList<T>) {
        this.list = list
    }

    fun add(v: T) {
        onAddObservable.onNext(v)
        list.add(v)
    }

    fun remove(v: T) {
        backup = v
        onRemoveObservable.onNext(v)
        list.remove(v)
    }

    fun remove(position: Int) {
        backup = list[position]
        onRemoveObservable.onNext(list[position])
        list.removeAt(position)
    }

    operator fun contains(v: T): Boolean {
        return list.contains(v)
    }

    val isEmpty: Boolean
        get() = list.isEmpty()

    fun resurrect() {
        onAddObservable.onNext(backup)
        list.add(backup!!)
    }

    fun size(): Int {
        return list.size
    }

    operator fun get(position: Int): T {
        return list[position]
    }

    fun getList(): MutableList<T>? {
        return list
    }

    fun clear() {
        list.clear()
    }
}
