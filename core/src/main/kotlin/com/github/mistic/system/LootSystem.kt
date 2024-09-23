package com.github.mistic.system

import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.mistic.component.AnimationComponent
import com.github.mistic.component.AnimationType
import com.github.mistic.component.LootComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.Qualifier
import com.github.mistic.event.EntityLootEvent
import com.github.mistic.event.fire

@AllOf([LootComponent::class])
class LootSystem(
    private val lootCmps: ComponentMapper<LootComponent>,
    private val aniCmps: ComponentMapper<AnimationComponent>,
    @Qualifier("GameStage") private val stage: Stage
) : IteratingSystem() {
    override fun onTickEntity(entity: Entity) {
        with(lootCmps[entity]) {
            if (interactEntity == null) {
                return
            }

            aniCmps[entity].run {
                nextAnimation(AnimationType.OPEN)
                mode = Animation.PlayMode.NORMAL
            }
            stage.fire(EntityLootEvent())

            configureEntity(entity) { lootCmps.remove(it) }
        }
    }
}
