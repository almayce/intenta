package io.almayce.dev.intenta.global

import io.almayce.dev.intenta.model.Point

/**
 * Created by almayce on 07.08.17.
 */
object SelectedPoint {
    var idPoint: String? = null
    var title: String? = null
    var status: String? = null
    var description: String? = null
    var photo: String? = null

    fun setPoint(point: Point) {
        idPoint = point.idPoint
        title = point.title
        status = point.status
        description = point.description
        photo = point.photo
    }
}