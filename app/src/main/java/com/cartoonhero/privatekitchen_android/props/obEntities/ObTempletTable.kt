package com.cartoonhero.privatekitchen_android.props.obEntities

import com.cartoonhero.privatekitchen_android.props.generateSpotId
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

@Entity
data class ObTemplate(
    @Id
    var id: Long = 0,
    val ownerId: String?
) {
    lateinit var pages: ToMany<ObPage>
}
@Entity
data class ObPage(
    @Id
    var id: Long = 0,
    var pagination: Int? = 0,
) {
    lateinit var categories: ToMany<ObCategory>
}
@Entity
data class ObCategory(
    @Id
    var id: Long = 0,
    var spotId: Long = generateSpotId(),
    var sequence: Int? = 0,
    var titleText: String = "",
) {
    lateinit var items: ToMany<ObMenuItem>
}