package net.ccbluex.liquidbounce.features.module.modules.combat.killaura.features

import net.ccbluex.liquidbounce.config.ToggleableConfigurable
import net.ccbluex.liquidbounce.event.events.MovementInputEvent
import net.ccbluex.liquidbounce.event.handler
import net.ccbluex.liquidbounce.event.repeatable
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.ModuleKillAura
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.ModuleKillAura.clickScheduler
import net.ccbluex.liquidbounce.features.module.modules.combat.killaura.ModuleKillAura.targetTracker
import net.ccbluex.liquidbounce.utils.aiming.Rotation
import net.ccbluex.liquidbounce.utils.aiming.RotationManager
import net.ccbluex.liquidbounce.utils.client.chat
import net.ccbluex.liquidbounce.utils.client.player
import net.ccbluex.liquidbounce.utils.entity.box
import net.ccbluex.liquidbounce.utils.entity.boxedDistanceTo
import net.ccbluex.liquidbounce.utils.entity.prevPos
import net.ccbluex.liquidbounce.utils.entity.rotation
import net.ccbluex.liquidbounce.utils.kotlin.random
import net.ccbluex.liquidbounce.utils.kotlin.randomDouble
import net.ccbluex.liquidbounce.utils.math.minus
import net.ccbluex.liquidbounce.utils.math.plus
import net.ccbluex.liquidbounce.utils.math.times
import net.ccbluex.liquidbounce.utils.movement.DirectionalInput
import net.minecraft.entity.Entity
import kotlin.random.Random

/**
 * A fight bot, fights for you, probably better than you. Lol.
 */
object FightBot : ToggleableConfigurable(ModuleKillAura, "FightBot", false) {

    val safeRange by float("SafeRange", 4f, 0.1f..5f)
    var sideToGo = false

    val repeatable = repeatable {
        sideToGo = !sideToGo
        waitTicks((10..35).random())
    }

    val inputHandler = handler<MovementInputEvent>(priority = 1000) { ev ->
        val enemy = targetTracker.lockedOnTarget ?: return@handler
        val distance = enemy.boxedDistanceTo(player)

        if (clickScheduler.isClickOnNextTick()) {
            if (distance < ModuleKillAura.range) {
                ev.directionalInput = DirectionalInput.NONE
                sideToGo = !sideToGo
            } else {
                ev.directionalInput = DirectionalInput.FORWARDS
            }
        } else if (distance < safeRange) {
            ev.directionalInput = DirectionalInput.BACKWARDS
        } else {
            ev.directionalInput = DirectionalInput.NONE
        }

        // We are now in range of the player, so try to circle around him
        ev.directionalInput = ev.directionalInput.copy(left = !sideToGo, right = sideToGo)
    }

    fun makeClientSideRotationNeeded(target: Entity): Rotation? {
        if (!enabled) return null

        val targetDistance = target.boxedDistanceTo(player)

        // Unlikely that we can travel such distance with such basic methods
        if (targetDistance > 69) {
            return null
        }

        // Cause lag behind
        var box = target.box.center

        val positionNow = target.pos
        val prevTargetPosition = target.prevPos
        val diff = positionNow - prevTargetPosition

        box -= (diff * ((targetDistance - 4.0).coerceAtLeast(1.0)))

        val directRotation = RotationManager.makeRotation(box, player.eyePos)

        if (directRotation != player.rotation) {
            // Introduce pitch drift
            directRotation.pitch = player.rotation.pitch + (-0.12f..0.12f).random().toFloat()
        }

        // This is very basic and should be handled by the path finder in the future
        return directRotation
    }

}
