package com.cartoonhero.privatekitchen_android.scenery.specialEffects.bounceRecyclerView

import android.view.View
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import androidx.recyclerview.widget.RecyclerView
import com.cartoonhero.privatekitchen_android.scenery.specialEffects.rvItemDragDrop.ItemTouchHelperAdapter

abstract class BounceRecyclerAdapter :
    RecyclerView.Adapter<BounceRecyclerAdapter.BounceViewHolder>(),
    ItemTouchHelperAdapter {
    override fun onMoveItem(from: Int, to: Int) {
        TODO("Not yet implemented")
    }

    override fun onRemoveItem(from: Int) {
        TODO("Not yet implemented")
    }
    inner class BounceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val springAnimY: SpringAnimation = SpringAnimation(itemView, SpringAnimation.TRANSLATION_Y)
            .setSpring(SpringForce().apply {
                finalPosition = 0f
                dampingRatio = SpringForce.DAMPING_RATIO_LOW_BOUNCY
                stiffness = SpringForce.STIFFNESS_VERY_LOW
            })
    }
}