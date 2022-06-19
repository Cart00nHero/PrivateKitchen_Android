package com.cartoonhero.privatekitchen_android.stage.scene.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.cartoonhero.privatekitchen_android.R
import com.cartoonhero.privatekitchen_android.actors.archmage.AppState
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveList
import com.cartoonhero.privatekitchen_android.actors.archmage.LiveScene
import com.cartoonhero.privatekitchen_android.actors.archmage.Teleporter
import com.cartoonhero.privatekitchen_android.stage.scenarios.MainScenario
import kotlinx.coroutines.*

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }
    private val currentName: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    private val director: MainDirector = MainScenario()
    private lateinit var viewModel: MainViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        director.beTestRedux(teleporter)
    }

    private val teleporter: Teleporter = object : Teleporter {
        override fun beNewState(state: AppState) {
            if (state.spell is LiveScene) {
                when(state.spell.prop) {
                    is String -> {
                        print(state.spell.prop)
                    }
                }
                return
            }
            if (state.spell is LiveList){
                when(state.spell.prop) {
                    is String -> {
                        print(state.spell.idx)
                    }
                }
            }
        }
    }
}