package com.cartoonhero.privatekitchen_android.stage

import android.os.Bundle
import com.cartoonhero.privatekitchen_android.R
import com.cartoonhero.privatekitchen_android.props.mainContext
import com.cartoonhero.privatekitchen_android.stage.scene.main.MainFragment
import com.cartoonhero.theatre.SetDecorator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ExperimentalCoroutinesApi
@ObsoleteCoroutinesApi
class MainStage: SetDecorator() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainContext = this
        setContentView(R.layout.main_stage)
        if (savedInstanceState == null) {
            setOpening(MainFragment.newInstance(), R.id.container)
            /*
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
            */
        }
    }
}