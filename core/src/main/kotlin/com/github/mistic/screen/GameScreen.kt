package com.github.mistic.screen

import box2dLight.Light
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.ai.GdxAI
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.EventListener
import com.github.mistic.MysticWoods
import com.github.mistic.component.AIComponent
import com.github.mistic.component.FloatingTextComponent
import com.github.mistic.component.ImageComponent
import com.github.mistic.component.LightComponent
import com.github.mistic.component.PhysicComponent
import com.github.mistic.component.StateComponent
import com.github.mistic.input.PlayerInputProcessor
import com.github.mistic.input.VirtualGamepad
import com.github.mistic.input.gdxInputProcessor
import com.github.mistic.system.AISystem
import com.github.mistic.system.AnimationSystem
import com.github.mistic.system.AttackSystem
import com.github.mistic.system.AudioSystem
import com.github.mistic.system.CameraSystem
import com.github.mistic.system.CollisionDespawnSystem
import com.github.mistic.system.CollisionSpawnSystem
import com.github.mistic.system.DeadSystem
import com.github.mistic.system.DebugSystem
import com.github.mistic.system.DialogSystem
import com.github.mistic.system.EntitySpawnSystem
import com.github.mistic.system.FloatingTextSystem
import com.github.mistic.system.InventorySystem
import com.github.mistic.system.LifeSystem
import com.github.mistic.system.LightSystem
import com.github.mistic.system.LootSystem
import com.github.mistic.system.MoveSystem
import com.github.mistic.system.PhysicSystem
import com.github.mistic.system.PortalSystem
import com.github.mistic.system.RenderSystem
import com.github.mistic.system.StateSystem
import com.github.mistic.ui.disposeSkin
import com.github.mistic.ui.loadSkin
import com.github.mistic.ui.model.DialogModel
import com.github.mistic.ui.model.GameModel
import com.github.mistic.ui.model.InventoryModel
import com.github.mistic.ui.view.PauseView
import com.github.mistic.ui.view.dialogView
import com.github.mistic.ui.view.gameView
import com.github.mistic.ui.view.inventoryView
import com.github.mistic.ui.view.pauseView
import com.github.quillraven.fleks.world
import ktx.app.KtxScreen
import ktx.assets.disposeSafely
import ktx.box2d.createWorld
import ktx.scene2d.actors
import java.awt.AWTEventMulticaster.add

class GameScreen(game: MysticWoods) : KtxScreen {
    private val gameStage = game.gameStage
    private val uiStage = game.uiStage
    private val gameAtlas = TextureAtlas("graphics/game.atlas")
    private val phWorld = createWorld(gravity = Vector2.Zero).apply {
        autoClearForces = false
    }
    private val rayHandler = RayHandler(phWorld).apply {
        // don't make light super bright
        RayHandler.useDiffuseLight(true)

        // player only throws shadows for map environment but not for enemies like slimes
        Light.setGlobalContactFilter(LightComponent.b2dPlayer, 1, LightComponent.b2dEnvironment)

        setAmbientLight(LightSystem.dayLightColor)
    }

    private val eWorld = world {
        injectables {
            add(phWorld)
            add("GameStage", gameStage)
            add("UiStage", uiStage)
            add("GameAtlas", gameAtlas)
            add(rayHandler)
        }

        components {
            add<PhysicComponent.Companion.PhysicComponentListener>()
            add<ImageComponent.Companion.ImageComponentListener>()
            add<StateComponent.Companion.StateComponentListener>()
            add<AIComponent.Companion.AIComponentListener>()
            add<FloatingTextComponent.Companion.FloatingTextComponentListener>()
            add<LightComponent.Companion.LightComponentListener>()
        }

        systems {
            add<EntitySpawnSystem>()
            add<CollisionSpawnSystem>()
            add<CollisionDespawnSystem>()
            add<AISystem>()
            add<PhysicSystem>()
            add<AnimationSystem>()
            add<MoveSystem>()
            add<AttackSystem>()
            add<LootSystem>()
            add<DialogSystem>()
            add<InventorySystem>()
            // DeadSystem must come before LifeSystem
            // because LifeSystem will add DeadComponent to an entity and sets its death animation.
            // Since the DeadSystem is checking if the animation is done it needs to be called after
            // the death animation is set which will be in the next frame in the AnimationSystem above.
            add<DeadSystem>()
            add<LifeSystem>()
            add<StateSystem>()
            add<PortalSystem>()
            add<CameraSystem>()
            add<FloatingTextSystem>()
            add<RenderSystem>()
            add<LightSystem>()
            add<AudioSystem>()
            add<DebugSystem>()
        }
    }

    init {
        loadSkin()
        eWorld.systems.forEach { sys ->
            if (sys is EventListener) {
                gameStage.addListener(sys)
            }
        }

        // Inicializa o PlayerInputProcessor para inputs físicos
        PlayerInputProcessor(eWorld, gameStage, uiStage)

        // Inicializa o com.github.mistic.input.VirtualGamepad para inputs virtuais
        val virtualGamepad = VirtualGamepad(uiStage.viewport, PlayerInputProcessor(eWorld, gameStage, uiStage))
        uiStage.addActor(virtualGamepad)

        gdxInputProcessor(uiStage)

        // UI Views
        uiStage.actors {
            gameView(GameModel(eWorld, gameStage))
            dialogView(DialogModel(gameStage))
            inventoryView(InventoryModel(eWorld, gameStage)) {
                this.isVisible = false
            }
            pauseView { this.isVisible = false }
        }
    }

    override fun show() {
        eWorld.system<PortalSystem>().setMap("maps/demo.tmx")
    }

    private fun pauseWorld(pause: Boolean) {
        val mandatorySystems = setOf(
            AnimationSystem::class,
            CameraSystem::class,
            RenderSystem::class,
            DebugSystem::class
        )
        eWorld.systems
            .filter { it::class !in mandatorySystems }
            .forEach { it.enabled = !pause }

        uiStage.actors.filterIsInstance<PauseView>().first().isVisible = pause
    }

    override fun resize(width: Int, height: Int) {
        val screenX = gameStage.viewport.screenX
        val screenY = gameStage.viewport.screenY
        val screenW = gameStage.viewport.screenWidth
        val screenH = gameStage.viewport.screenHeight
        rayHandler.useCustomViewport(screenX, screenY, screenW, screenH)
    }

    override fun pause() = pauseWorld(true)

    override fun resume() = pauseWorld(false)

    override fun render(delta: Float) {
        Gdx.app.log("GameScreen", "Render method called")
        val dt = delta.coerceAtMost(0.25f)
        GdxAI.getTimepiece().update(dt)
        eWorld.update(dt)
    }

    override fun dispose() {
        eWorld.dispose()
        phWorld.disposeSafely()
        gameAtlas.disposeSafely()
        disposeSkin()
        rayHandler.disposeSafely()
    }
}
