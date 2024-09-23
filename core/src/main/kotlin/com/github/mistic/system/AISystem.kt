package com.github.mistic.system

import com.github.mistic.component.AIComponent
import com.github.mistic.component.DeadComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.NoneOf

@AllOf([AIComponent::class])
@NoneOf([DeadComponent::class])
class AISystem(
    private val aiCmps: ComponentMapper<AIComponent>
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        with(aiCmps[entity]) {
            if (treePath.isBlank()) {
                return
            }

            behaviorTree.step()
        }
    }
}
