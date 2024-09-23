package com.github.mistic.input

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Group
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton
import com.badlogic.gdx.scenes.scene2d.utils.DragListener
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.viewport.Viewport
import com.github.mistic.input.PlayerInputProcessor

class VirtualGamepad(viewport: Viewport, private val playerInputProcessor: PlayerInputProcessor) : Group() {
    private val joystickBg = Image(Texture("sprite/transparentdark/transparentDark05.png"))
    private val joystickKnob = Image(Texture("sprite/shadeddark/shadedDark01.png"))

    // Ajustar a posição do botão de ataque no canto inferior direito
    private val btnAttack = createButton(
        "sprite/linedark/lineDark44.png",
        viewport.worldWidth - 70f, // Posição calculada baseada na largura da tela
        10f, // Posição Y próxima ao canto inferior
        PlayerInputProcessor.ATTACK
    )

    private val joystickCenter = Vector2()
    private val joystickRadius = 50f

    init {
        // Configura joystick
        joystickBg.setSize(50f, 50f) // Ajuste o tamanho do fundo do joystick
        joystickBg.setPosition(10f, 10f) // Posição no canto inferior esquerdo
        joystickKnob.setSize(30f, 30f) // Ajuste o tamanho do knob
        joystickKnob.setPosition(
            joystickBg.x + joystickBg.width / 2 - joystickKnob.width / 2,
            joystickBg.y + joystickBg.height / 2 - joystickKnob.height / 2
        )

        joystickCenter.set(joystickKnob.x + joystickKnob.width / 2, joystickKnob.y + joystickKnob.height / 2)

        addActor(joystickBg)
        addActor(joystickKnob)
        addActor(btnAttack)

        // Listener para o joystick
        joystickKnob.addListener(object : DragListener() {
            override fun drag(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                val touchPos = Vector2(x, y)
                val displacement = touchPos.sub(joystickCenter).limit(joystickRadius)

                joystickKnob.setPosition(
                    joystickCenter.x + displacement.x - joystickKnob.width / 2,
                    joystickCenter.y + displacement.y - joystickKnob.height / 2
                )

                // Calcula direção baseada na posição do knob
                playerInputProcessor.updateJoystickMovement(displacement.x / joystickRadius, displacement.y / joystickRadius)
            }

            override fun dragStop(event: InputEvent?, x: Float, y: Float, pointer: Int) {
                joystickKnob.setPosition(joystickCenter.x - joystickKnob.width / 2, joystickCenter.y - joystickKnob.height / 2)
                playerInputProcessor.updateJoystickMovement(0f, 0f)
            }
        })
    }

    private fun createButton(imagePath: String, x: Float, y: Float, keycode: Int): ImageButton {
        val buttonTexture = Texture(imagePath)
        val buttonDrawable = TextureRegionDrawable(buttonTexture)
        val button = ImageButton(buttonDrawable)
        button.setPosition(x, y)
        button.setSize(50f, 50f) // Define o tamanho do botão de ataque
        button.addListener(object : com.badlogic.gdx.scenes.scene2d.InputListener() {
            override fun touchDown(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int): Boolean {
                playerInputProcessor.onVirtualKeyDown(keycode)
                return true
            }

            override fun touchUp(event: InputEvent?, x: Float, y: Float, pointer: Int, button: Int) {
                playerInputProcessor.onVirtualKeyUp(keycode)
            }
        })
        return button
    }
}
