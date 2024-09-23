package com.github.mistic.behavior

import com.badlogic.gdx.ai.btree.LeafTask
import com.badlogic.gdx.ai.btree.Task
import com.github.mistic.component.AIEntity

abstract class Action : LeafTask<AIEntity>() {
    val aiEntity: AIEntity
        get() = `object`

    override fun copyTo(task: Task<AIEntity>) = task
}
