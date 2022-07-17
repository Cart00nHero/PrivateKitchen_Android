package com.cartoonhero.privatekitchen_android.stage.scene.map

import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.cartoonhero.privatekitchen_android.R
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Spell
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.props.entities.PlaceMarker
import com.cartoonhero.privatekitchen_android.stage.scenarios.map.MapScenario
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class PhMapScene : AppCompatActivity(), OnMapReadyCallback {

    private val director: MapDirector = MapScenario(this)
    private lateinit var mMap: GoogleMap

    private val waypoint: Teleporter = object : Teleporter {
        override fun beSpellCraft(spell: Spell) {
            if (spell is LiveScene) {
                when(val prop = spell.prop) {
                    is Location -> {
                        val sydney = LatLng(prop.latitude, prop.longitude)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                    }
                    is List<*> -> {
                        mMap.clear()
                        val markers: List<PlaceMarker> = prop.filterIsInstance<PlaceMarker>()
                        markers.forEach {
                            mMap.addMarker(
                                MarkerOptions().position(it.coordinate)
                                    .title(it.name).snippet(it.addressText)
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.phone_map_scene)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()
        director.beShowTime(waypoint)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMarkerClickListener {
            true
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        director.beNewLocation()
    }
}