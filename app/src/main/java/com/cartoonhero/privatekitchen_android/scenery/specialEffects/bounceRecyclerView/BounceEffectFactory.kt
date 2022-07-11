package com.cartoonhero.privatekitchen_android.scenery.specialEffects.bounceRecyclerView

import android.content.Context
import android.widget.EdgeEffect
import androidx.recyclerview.widget.RecyclerView

fun makeBounceEffect(context: Context): RecyclerView.EdgeEffectFactory {
    return object : RecyclerView.EdgeEffectFactory() {
        override fun createEdgeEffect(view: RecyclerView, direction: Int): EdgeEffect {
            return object : EdgeEffect(context) {
                val OVER_SCROLL_COEF = 0.4f
                val OVER_FLICK_COEF = 0.4f

                override fun onPull(deltaDistance: Float, displacement: Float) {
                    super.onPull(deltaDistance, displacement)
                    val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                    val deltaY =
                        sign * view.height * deltaDistance * OVER_SCROLL_COEF
                    for (i in 0 until view.childCount) {
                        view.apply {
                            val holder =
                                getChildViewHolder(getChildAt(i)) as BounceRecyclerAdapter.BounceViewHolder
                            holder.springAnimY.cancel()
                            holder.itemView.translationY += deltaY
                        }
                    }
                }

                override fun onRelease() {
                    super.onRelease()
                    for (i in 0 until view.childCount) {
                        view.apply {
                            val holder =
                                getChildViewHolder(getChildAt(i)) as BounceRecyclerAdapter.BounceViewHolder
                            holder.springAnimY.start()
                        }
                    }
                }

                override fun onAbsorb(velocity: Int) {
                    super.onAbsorb(velocity)
                    val sign = if (direction == DIRECTION_BOTTOM) -1 else 1
                    val translationVelocity = sign * velocity * OVER_FLICK_COEF
                    for (i in 0 until view.childCount) {
                        view.apply {
                            val holder =
                                getChildViewHolder(getChildAt(i)) as BounceRecyclerAdapter.BounceViewHolder
                            holder.springAnimY
                                .setStartVelocity(translationVelocity)
                                .start()
                        }
                    }
                }
            }
        }
    }
}