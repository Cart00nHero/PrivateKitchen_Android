package com.cartoonhero.privatekitchen_android.props.obEntities

import com.cartoonhero.privatekitchen_android.props.entities.OrderInfo
import com.cartoonhero.privatekitchen_android.props.entities.Orderer
import com.cartoonhero.privatekitchen_android.props.inlineTools.toEntity
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne

@Entity
data class ObOrderForm(
    @Id
    var id: Long = 0,
    var uniqueId: String? = "",
    var kitchenId: String? = "",
    var userId: String? = "",
    // datetime: 2022-01-26 10:01:17Z
    var setUp: String? = "",
    var state: Int? = 0,
    var paid: Boolean = false,
    // datetime
    var arrival: String? = "",
    var orderer: String = "",
    var info: String = ""
) {
    lateinit var diningWay: ToOne<ObDiningWay>
    lateinit var items: ToMany<ObOrderItem>
}
fun ObOrderForm.beOrderer(): Orderer {
    val odrer = this.orderer.toEntity<Orderer>()
    return odrer ?: Orderer("","")
}
fun ObOrderForm.beInfo(): OrderInfo {
    val info = this.info.toEntity<OrderInfo>()
    return info ?: OrderInfo()
}
@Entity
data class ObOrderItem(
    @Id
    var id: Long = 0,
    var pagination: Int? = 0,
    var categoryId: String?,
    var quantity: Int? = 0
) {
    lateinit var item: ToOne<ObMenuItem>
    lateinit var customize: ToMany<ObChoice>
}
@Entity
data class ObChoice(
    @Id
    var id: Long = 0,
    var amount: Int? = 0,
    var cost: Double? = 0.0
) {
    lateinit var choices: ToMany<ObOption>
}