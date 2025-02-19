package com.github.mistic.system

import com.badlogic.gdx.scenes.scene2d.Stage
import com.github.mistic.component.InventoryComponent
import com.github.mistic.component.ItemComponent
import com.github.mistic.component.ItemType
import com.github.quillraven.fleks.AllOf
import com.github.quillraven.fleks.ComponentMapper
import com.github.quillraven.fleks.Entity
import com.github.quillraven.fleks.IteratingSystem
import com.github.quillraven.fleks.Qualifier
import com.github.mistic.event.EntityAddItemEvent
import com.github.mistic.event.fire

@AllOf([InventoryComponent::class])
class InventorySystem(
    private val inventoryCmps: ComponentMapper<InventoryComponent>,
    private val itemCmps: ComponentMapper<ItemComponent>,
    @Qualifier("GameStage") private val gameStage: Stage,
) : IteratingSystem() {

    override fun onTickEntity(entity: Entity) {
        val inventory = inventoryCmps[entity]
        if (inventory.itemsToAdd.isEmpty()) {
            return
        }

        inventory.itemsToAdd.forEach { itemType ->
            val slotIdx: Int = emptySlotIndex(inventory)
            if (slotIdx == -1) {
                // inventory is full -> cannot add more items
                return
            }

            val newItem = spawnItem(itemType, slotIdx)
            inventory.items += newItem
            gameStage.fire(EntityAddItemEvent(entity, newItem))
        }
        inventory.itemsToAdd.clear()
    }

    private fun emptySlotIndex(inventory: InventoryComponent): Int {
        for (i in 0 until InventoryComponent.INVENTORY_CAPACITY) {
            if (inventory.items.none { itemCmps[it].slotIdx == i }) {
                return i
            }
        }

        return -1
    }

    private fun spawnItem(type: ItemType, slotIdx: Int): Entity {
        return world.entity {
            add<ItemComponent> {
                this.itemType = type
                this.slotIdx = slotIdx
            }
        }
    }
}
