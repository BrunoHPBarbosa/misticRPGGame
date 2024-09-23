package com.github.mistic.ui.model

import com.github.mistic.component.ItemCategory

data class ItemModel(
    val itemEntityId: Int,
    val category: ItemCategory,
    val atlasKey: String,
    var slotIdx: Int,
    var equipped: Boolean
)
