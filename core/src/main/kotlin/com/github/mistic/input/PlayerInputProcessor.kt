package com.github.mistic.input

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input.Keys.DOWN
import com.badlogic.gdx.Input.Keys.I
import com.badlogic.gdx.Input.Keys.LEFT
import com.badlogic.gdx.Input.Keys.P
import com.badlogic.gdx.Input.Keys.RIGHT
import com.badlogic.gdx.Input.Keys.SPACE
import com.badlogic.gdx.Input.Keys.UP
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.mistic.component.AttackComponent
import com.github.mistic.component.MoveComponent
import com.github.mistic.component.PlayerComponent
import com.github.mistic.ui.view.InventoryView
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.World
import com.github.mistic.event.GamePauseEvent
import com.github.mistic.event.GameResumeEvent
import com.github.mistic.event.fire
import ktx.app.KtxInputAdapter

fun gdxInputProcessor(processor: InputProcessor) {
    val currProcessor = Gdx.input.inputProcessor
    if (currProcessor == null) {
        Gdx.input.inputProcessor = processor
    } else {
        if (currProcessor is InputMultiplexer) {
            if (processor !in currProcessor.processors) {
                currProcessor.addProcessor(processor)
            }
        } else {
            Gdx.input.inputProcessor = InputMultiplexer(currProcessor, processor)
        }
    }
}

class PlayerInputProcessor(
    world: World,
    private val gameStage: Stage,
    private val uiStage: Stage,
    private val moveCmps: ComponentMapper<MoveComponent> = world.mapper(),
    private val attackCmps: ComponentMapper<AttackComponent> = world.mapper(),
) : KtxInputAdapter {
    private val playerEntities = world.family(allOf = arrayOf(PlayerComponent::class))
    private var playerCos = 0f
    private var playerSin = 0f
    private var paused = false
    private val pressedKeys = mutableSetOf<Int>()

    companion object {
        const val ATTACK = 100 // Pode ser qualquer valor único que represente o ataque
    }
    init {
        gdxInputProcessor(this)
    }

    private fun Int.isMovementKey(): Boolean {
        return this == UP || this == DOWN || this == LEFT || this == RIGHT
    }

    private fun isPressed(keycode: Int): Boolean = keycode in pressedKeys

    private fun updatePlayerMovement() {
        playerEntities.forEach { player ->
            with(moveCmps[player]) {
                cosSin.set(playerCos, playerSin).nor()
            }
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        pressedKeys += keycode
        if (keycode.isMovementKey()) {
            when (keycode) {
                UP -> playerSin = 1f
                DOWN -> playerSin = -1f
                RIGHT -> playerCos = 1f
                LEFT -> playerCos = -1f
            }
            updatePlayerMovement()
            return true
        } else if (keycode == SPACE || keycode == ATTACK) {
            playerEntities.forEach { attackCmps[it].doAttack = true }
            return true
        } else if (keycode == I) {
            // Inventário
        } else if (keycode == P) {
            // Pausa
        }
        return false
    }


    override fun keyUp(keycode: Int): Boolean {
        pressedKeys -= keycode
        if (keycode.isMovementKey()) {
            when (keycode) {
                UP -> playerSin = if (isPressed(DOWN)) -1f else 0f
                DOWN -> playerSin = if (isPressed(UP)) 1f else 0f
                RIGHT -> playerCos = if (isPressed(LEFT)) -1f else 0f
                LEFT -> playerCos = if (isPressed(RIGHT)) 1f else 0f
            }
            updatePlayerMovement()
            return true
        }
        return false
    }
    fun updateJoystickMovement(cos: Float, sin: Float) {
        playerCos = cos
        playerSin = sin
        updatePlayerMovement()
    }

    fun onVirtualKeyDown(keycode: Int) {
        keyDown(keycode) // Simula o comportamento do teclado ao apertar o botão virtual
    }

    fun onVirtualKeyUp(keycode: Int) {
        keyUp(keycode) // Simula o comportamento do teclado ao soltar o botão virtual
    }

}
