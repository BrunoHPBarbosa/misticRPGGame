package com.github.mistic.system

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.mistic.component.FloatingTextComponent
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.Qualifier
import ktx.math.vec2

@AllOf([FloatingTextComponent::class])
class FloatingTextSystem(
    private val textCmps: ComponentMapper<FloatingTextComponent>,
    @Qualifier("GameStage") private val gameStage: Stage,
    @Qualifier("UiStage") private val uiStage: Stage,
) : IteratingSystem() {
    private val uiLocation = vec2()
    private val uiTarget = vec2()

    override fun onTickEntity(entity: Entity) {
        with(textCmps[entity]) {
            if (time >= lifeSpan) {
                world.remove(entity)
                return
            }

            /**
             * convert game coordinates to UI coordinates
             * 1) project = stage to screen coordinates
             * 2) unproject = screen to stage coordinates
             */
            uiLocation.set(txtLocation)
            gameStage.viewport.project(uiLocation)
            uiStage.viewport.unproject(uiLocation)
            uiTarget.set(txtTarget)
            gameStage.viewport.project(uiTarget)
            uiStage.viewport.unproject(uiTarget)

            // interpolate
            uiLocation.interpolate(uiTarget, (time / lifeSpan).coerceAtMost(1f), Interpolation.smooth2)
            label.setPosition(uiLocation.x, uiStage.viewport.worldHeight - uiLocation.y)

            time += deltaTime
        }
    }
}
