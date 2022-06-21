package com.cartoonhero.privatekitchen_android.props.obEntities

import com.cartoonhero.privatekitchen_android.props.entities.LocalizedText
import com.cartoonhero.privatekitchen_android.props.entities.StationInfo
import com.cartoonhero.privatekitchen_android.props.generateSpotId
import com.cartoonhero.privatekitchen_android.props.inlineTools.toEntity
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.relation.ToMany
import io.objectbox.relation.ToOne

@Entity
data class ObWorkstation(
    @Id
    var id: Long = 0,
    var uniqueId: String? = "",
    var chefId: String? = "",
    var kitchen_id: String? = "",
    var info: String = ""
) {
    lateinit var kitchen: ToOne<ObStKitchen>
}
fun ObWorkstation.beInfo(): StationInfo {
    val info = this.info.toEntity<StationInfo>()
    return info ?: StationInfo()
}
@Entity
data class ObStKitchen(
    @Id
    var id: Long = 0,
    var info: String = ""
) {
    lateinit var address: ToOne<ObAddress>
}
@Entity
data class ObAddress(
    @Id
    var id: Long = 0,
    var administrativeArea: String? = "",
    var completion: String? = "",
    var isoNationCode: String? = "",
    var latitude: String = "0.0",
    var longitude: String = "0.0",
    var locality: String? = "",
    var nation: String? = "",
    var postalCode: String? = "",
    var subAdministrativeArea: String? = "",
    var subLocality: String? = "",
    var thoroughfare: String? = "",
    var subThoroughfare: String? = "",
    var floor: String? = "",
    var plusCode: String? = ""
)
@Entity
data class ObDiningWay(
    @Id
    var id: Long = 0,
    var spotId: Long = generateSpotId(),
    var sequence: Int? = 0,
    var optionText: String = ""
)
fun ObDiningWay.beOption(): LocalizedText {
    val localText = this.optionText.toEntity<LocalizedText>()
    return localText ?: LocalizedText()
}
@Entity
data class ObStorehouse(
    @Id
    var id: Long = 0,
    val ownerId: String?
) {
    lateinit var items: ToMany<ObMenuItem>
    lateinit var options: ToMany<ObOption>
    lateinit var diningWays: ToMany<ObDiningWay>
}
@Entity
data class ObMenuItem(
    @Id
    var id: Long = 0,
    var spotId: Long = generateSpotId(),
    var sequence: Int = 0,
    var nameText: String = "",
    var introText: String = "",
    var price: Double = 0.0,
    var photo: String = "",
    var quota: Int = 0
) {
    lateinit var options: ToMany<ObOption>
    lateinit var toCategory: ToOne<ObCategory>
}
fun ObMenuItem.beName():LocalizedText {
    val theText = this.nameText.toEntity<LocalizedText>()
    return theText ?: LocalizedText()
}
fun ObMenuItem.beIntro():LocalizedText {
    val theText = this.introText.toEntity<LocalizedText>()
    return theText ?: LocalizedText()
}
@Entity
data class ObOption(
    @Id
    var id: Long = 0,
    var spotId: Long = generateSpotId(),
    var sequence: Int? = 0,
    var price: Double? = 0.0,
    var titleText: String = ""
)