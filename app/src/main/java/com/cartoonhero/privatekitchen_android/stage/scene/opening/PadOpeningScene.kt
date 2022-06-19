package com.cartoonhero.privatekitchen_android.stage.scene.opening

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.cartoonhero.privatekitchen_android.R
import com.cartoonhero.privatekitchen_android.actors.archmage.*
import com.cartoonhero.privatekitchen_android.props.entities.EnterListSource
import com.cartoonhero.privatekitchen_android.stage.scenarios.opening.OpeningScenario
import kotlinx.coroutines.*

/**
 * A simple [Fragment] subclass.
 * Use the [PadOpeningScene.newInstance] factory method to
 * create an instance of this fragment.
 */
@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class PadOpeningScene : Fragment() {
    // TODO: Rename and change types of parameters
    private val director: OpeningDirector = OpeningScenario()
    private var dataSource: List<EnterListSource> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.scene_opening, container, false)
    }

    override fun onStart() {
        super.onStart()
        director.beShowTime(teleporter)
        director.beBuildSource()
    }

    override fun onStop() {
        super.onStop()
        director.beLowerCurtain()
    }

    companion object {
        fun newInstance() = PadOpeningScene()
    }

    private val teleporter: Teleporter = object : Teleporter {
        override fun beSpellCraft(spell: Spell) {
            CoroutineScope(Dispatchers.Main).launch {
                if (spell is LiveScene) {
                    when(spell.prop) {
                        is List<*> ->
                            dataSource = spell.prop.filterIsInstance<EnterListSource>()
                    }
                }
            }
        }
    }
}