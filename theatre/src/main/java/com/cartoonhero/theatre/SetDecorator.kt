package com.cartoonhero.theatre

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

open class SetDecorator: AppCompatActivity() {
    private var sceneIndex = 0
    private val sceneFlow = ArrayList<Fragment>()
    private var resourceId: Int = 0

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
    fun forward(scenes: List<Fragment>) {
        sceneIndex += scenes.size
        sceneFlow.addAll(scenes)
        replaceFragment(scenes.last(), resourceId)
    }
    fun backward() {
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
    fun goBackTo(index: Int) {
        if (sceneIndex > index) {
            replaceFragment(sceneFlow[index], resourceId)
            sceneFlow.dropLast( (sceneIndex-index) )
        }
    }
    private fun currentScene(): Fragment {
        return sceneFlow.last()
    }
    private fun clearFragmentBackStack() {
        if (supportFragmentManager.fragments.size > 0) {
            for (fragment in supportFragmentManager.fragments) {
                supportFragmentManager.beginTransaction().remove(fragment!!).commit()
            }
        }
    }
    private inline fun FragmentManager.inTransaction(
        func: FragmentTransaction.()-> FragmentTransaction
    ) {
        beginTransaction().func().commitNow()
//    beginTransaction().func().commitAllowingStateLoss()
    }
    private fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int) {
        supportFragmentManager.inTransaction {
            add(frameId,fragment)
        }
    }
    private fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
        supportFragmentManager.inTransaction {
            replace(frameId,fragment)
        }
    }
    private fun AppCompatActivity.removeFragment(fragment: Fragment) {
        supportFragmentManager.inTransaction {
            remove(fragment)
        }
    }
    private fun AppCompatActivity.findFragment(containerId: Int): Fragment? {
        return supportFragmentManager.findFragmentById(containerId)
    }
}