package com.cartoonhero.privatekitchen_android.stage.scenarios.workstation

import com.cartoonhero.privatekitchen_android.props.entities.WkStTabScene
import com.cartoonhero.privatekitchen_android.stage.scene.workstation.WorkStDirector
import com.cartoonhero.theatre.Scenario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.ObsoleteCoroutinesApi

@ObsoleteCoroutinesApi
@ExperimentalCoroutinesApi
class WorkStScenario: Scenario(), WorkStDirector {

    private fun actShowTime() {
    }
    private fun actSubscribeSceneContent(
        subscriber: (WkStTabScene) -> Unit
    ) {
    }
    private fun actPrepare(scene: WkStTabScene, complete: (() -> Unit)?) {
    }
    private fun actPackWkStParcel(recipient: String, complete: (() -> Unit)?) {
    }
    private fun actLowerCurtain() {
    }

    override fun beShowTime() {
        tell {
            actShowTime()
        }
    }

    override fun beSubscribeSceneContent(subscriber: (WkStTabScene) -> Unit) {
        tell {
            actSubscribeSceneContent(subscriber)
        }
    }

    override fun bePrepare(scene: WkStTabScene, complete: (() -> Unit)?) {
        tell {
            actPrepare(scene, complete)
        }
    }

    override fun bePackWkStParcel(recipient: String, complete: (() -> Unit)?) {
        tell {
            actPackWkStParcel(recipient, complete)
        }
    }

    override fun beLowerCurtain() {
        tell { actLowerCurtain() }
    }
}