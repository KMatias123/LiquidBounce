package net.ccbluex.liquidbounce.script.bindings.api

import net.ccbluex.liquidbounce.utils.aiming.*
import net.ccbluex.liquidbounce.utils.client.mc
import net.ccbluex.liquidbounce.utils.entity.eyes

import net.minecraft.entity.Entity
import kotlin.math.sqrt

/**
 * A collection of useful rotation utilities for the ScriptAPI.
 * This SHOULD not be changed in a way that breaks backwards compatibility.
 *
 * This is a singleton object, so it can be accessed from the script API like this:
 * ```js
 * api.rotationUtil.newRaytracedRotationEntity(entity, 4.2, 0.0)
 * rotationUtil.newRotationEntity(entity)
 * rotationUtil.aimAtRotation(rotation, true)
 * ```
 */
object JsRotationUtil {

    /**
     * Creates a new [Rotation] from [entity]'s bounding box.
     * This uses raytracing, so it's guaranteed to be the best spot.
     *
     * It has a performance impact, so it's recommended to use [newRotationEntity] if you don't need the best spot.
     */
    @JvmName("newRaytracedRotationEntity")
    fun newRaytracedRotationEntity(entity: Entity, range: Double, throughWallsRange: Double): Rotation? {
        val box = entity.boundingBox

        // Finds the best spot (and undefined if no spot was found)
        val (rotation, _) = raytraceBox(
            mc.player!!.eyes,
            box,
            range = sqrt(range),
            wallsRange = throughWallsRange
        ) ?: return null

        return rotation
    }

    /**
     * Creates a new [Rotation] from [entity]'s bounding box.
     * This uses no raytracing, so it's not guaranteed to be the best spot.
     * It will aim at the center of the bounding box.
     *
     * It has almost zero performance impact, so it's recommended to use this if you don't need the best spot.
     */
    @JvmName("newRotationEntity")
    fun newRotationEntity(entity: Entity)
        = RotationManager.makeRotation(entity.boundingBox.center, mc.player!!.eyes)

    /**
     * Aims at the given [rotation] using the in-built RotationManager.
     *
     * @param rotation The rotation to aim at.
     * @param fixVelocity Whether to fix the player's velocity.
     *   This means bypassing anti-cheat checks for aim-related movement.
     */
    @JvmName("aimAtRotation")
    fun aimAtRotation(rotation: Rotation, fixVelocity: Boolean) {
        RotationManager.aimAt(rotation, considerInventory = true, RotationsConfigurable(180f..180f).also {
            it.fixVelocity = fixVelocity
        })
    }

}
