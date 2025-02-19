package com.github.mistic.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.mistic.component.AnimationComponent
import com.github.mistic.component.DeadComponent
import com.github.mistic.component.LifeComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.Qualifier
import com.github.mistic.event.EntityReviveEvent
import com.github.mistic.event.fire
import ktx.log.logger

@AllOf([DeadComponent::class])
class DeadSystem(
    private val deadCmps: ComponentMapper<DeadComponent>,
    private val aniCmps: ComponentMapper<AnimationComponent>,
    private val lifeCmps: ComponentMapper<LifeComponent>,
    @Qualifier("GameStage") private val gameStage: Stage,
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        if (entity !in aniCmps) {
            // entity has no special animation
            // -> remove it
            log.debug { "Entity $entity without animation gets removed" }
            world.remove(entity)
            return
        }

        if (aniCmps[entity].isAnimationFinished()) {
            val deadCmp = deadCmps[entity]
            if (deadCmp.reviveTime == 0f) {
                // animation done and no revival planned
                // -> remove entity
                log.debug { "Entity $entity with animation gets removed" }
                world.remove(entity)
                return
            }

            deadCmp.reviveTime -= deltaTime
            if (deadCmp.reviveTime <= 0f) {
                // animation done and revival time passed
                // -> revive entity
                log.debug { "Entity $entity gets resurrected" }
                with(lifeCmps[entity]) { life = max }
                configureEntity(entity) { deadCmps.remove(it) }
                gameStage.fire(EntityReviveEvent(entity))
            }
        }
    }

    companion object {
        private val log = logger<DeadSystem>()
    }
}
