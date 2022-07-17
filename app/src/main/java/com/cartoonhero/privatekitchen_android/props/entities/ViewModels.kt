package com.cartoonhero.privatekitchen_android.props.entities

import com.cartoonhero.privatekitchen_android.props.obEntities.ObWorkflow

data class FlowGridVM(
    var steps: List<FlowStep> = listOf(),
    var flows: List<ObWorkflow> = listOf()
)
