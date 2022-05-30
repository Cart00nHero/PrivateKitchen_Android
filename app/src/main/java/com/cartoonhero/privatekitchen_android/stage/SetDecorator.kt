package com.cartoonhero.privatekitchen_android.stage

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cartoonhero.privatekitchen_android.props.inlineTools.addFragment
import com.cartoonhero.privatekitchen_android.props.inlineTools.removeFragment
import com.cartoonhero.privatekitchen_android.props.inlineTools.replaceFragment

open class SetDecorator: AppCompatActivity() {
    private var sceneIndex = 0
    private val sceneFlow = ArrayList<Fragment>()
    private var resourceId: Int = 0

    fun setResourceId(resourceId: Int) {
        this.resourceId = resourceId
    }
    fun setOpening(scene: Fragment, resourceId: Int) {
        this.resourceId = resourceId
        if (sceneFlow.size > 0) {
            removeFragment(currentScene())
            sceneFlow.clear()
            clearFragmentBackStack()
        }
        addFragment(scene,resourceId)
        sceneFlow.add(0, scene)
    }
    fun goForward(fragments: List<Fragment>) {
        sceneIndex += fragments.size
        sceneFlow.addAll(fragments)
        replaceFragment(fragments.last(), resourceId)
    }
    fun goBackward() {
        if (sceneIndex > 0) {
            val currentFragment = sceneFlow[sceneIndex]
            val previousFragment = sceneFlow[sceneIndex-1]
            sceneFlow.remove(currentFragment)
            sceneIndex -= 1
            replaceFragment(previousFragment, resourceId)
        } else {
            finish()
        }
    }
    fun backTo(index: Int) {
        if (sceneIndex > index) {
            replaceFragment(sceneFlow[index], resourceId)
            sceneFlow.dropLast( (sceneIndex-index) )
        }
    }
    private fun currentScene(): Fragment{
        return sceneFlow.last()
    }
    private fun clearFragmentBackStack() {
        if (supportFragmentManager.fragments.size > 0) {
            for (fragment in supportFragmentManager.fragments) {
                supportFragmentManager.beginTransaction().remove(fragment!!).commit()
            }
        }
    }
}