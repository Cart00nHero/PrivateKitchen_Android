package com.cartoonhero.privatekitchen_android.stage.scene.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartoonhero.privatekitchen_android.R
import com.cartoonhero.privatekitchen_android.stage.scenarios.MainScenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.reduxkotlin.StoreSubscription
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi

class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
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
        context?.let { director.beInitDb(it) }
        lateinit var storeSubscription: StoreSubscription
    }

}