package com.cartoonhero.privatekitchen_android.props.inlineTools

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction

inline fun FragmentManager.inTransaction(func: FragmentTransaction.()-> FragmentTransaction) {
    beginTransaction().func().commitNow()
//    beginTransaction().func().commitAllowingStateLoss()
}
fun AppCompatActivity.addFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction {
        add(frameId,fragment)
    }
}
fun AppCompatActivity.replaceFragment(fragment: Fragment, frameId: Int) {
    supportFragmentManager.inTransaction {
        replace(frameId,fragment)
    }
}
fun AppCompatActivity.removeFragment(fragment: Fragment) {
    supportFragmentManager.inTransaction {
        remove(fragment)
    }
}
fun AppCompatActivity.findFragment(containerId: Int): Fragment? {
    return supportFragmentManager.findFragmentById(containerId)
}