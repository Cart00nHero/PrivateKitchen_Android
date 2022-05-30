package com.cartoonhero.privatekitchen_android.props.obEntities

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany

@Entity
data class ObDashboard(
    @Id
    var id: Long = 0,
    val ownerId: String?,
    var closeTime: String? = ""
) {
    lateinit var steps: ToMany<ObWorkflow>
}
@Entity
data class ObWorkflow(
    @Id
    var id: Long = 0,
    var step: Int? = 0,
    var name: String?
)