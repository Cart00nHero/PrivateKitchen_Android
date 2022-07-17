package com.cartoonhero.privatekitchen_android.scenery.specialEffects.rvItemDragDrop

interface ItemMoveSwipeListener {
    /**
     * 設置1個監聽的interface
     *
     * onItemMove : 當item移動完的時候
     * onItemSwipe : 當item滑動完的時候
     */
    fun onItemMove(fromPosition: Int, toPosition: Int): Boolean

    fun onItemSwipe(position: Int)
}