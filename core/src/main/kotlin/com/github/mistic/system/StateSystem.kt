package com.github.mistic.system

import com.github.mistic.component.StateComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem

@AllOf([StateComponent::class])
class StateSystem(
    private val stateCmps: ComponentMapper<StateComponent>
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        val stateCmp = stateCmps[entity]

        if (stateCmp.stateMachine.currentState != stateCmp.nextState) {
            stateCmp.stateMachine.changeState(stateCmp.nextState)
        }

        stateCmp.stateMachine.update()
    }
}
