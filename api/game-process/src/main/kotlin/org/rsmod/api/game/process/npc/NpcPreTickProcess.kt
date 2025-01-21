package org.rsmod.api.game.process.npc

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.game.MapClock
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.NpcList
import org.rsmod.game.entity.PathingEntity

public class NpcPreTickProcess
@Inject
constructor(
    private val npcs: NpcList,
    private val hunt: NpcHuntProcessor,
    private val mapClock: MapClock,
) {
    private val logger = InlineLogger()

    public fun process() {
        for (npc in npcs) {
            npc.tryOrDespawn {
                updateMapClock()
                huntProcess()
            }
        }
    }

    /**
     * Updates the npc's `currentMapClock` to match the latest value from the global `mapClock`.
     *
     * This ensures that any `delay` calls set by a player's incoming packet (e.g., delaying a pet
     * after `IfButton` on "Call follower" button) use an up-to-date reference for the map clock.
     * Without this update, delays would appear to be "sped up" by one cycle because the `delay`
     * functions rely on the [PathingEntity.currentMapClock] as a baseline to calculate their delay
     * duration.
     */
    private fun Npc.updateMapClock() {
        currentMapClock = mapClock.cycle
    }

    private fun Npc.huntProcess() {
        hunt.process(this)
    }

    private inline fun Npc.tryOrDespawn(block: Npc.() -> Unit) =
        try {
            block(this)
        } catch (e: Exception) {
            forceDespawn()
            logger.error(e) { "Error processing pre-tick for npc: $this." }
        } catch (e: NotImplementedError) {
            forceDespawn()
            logger.error(e) { "Error processing pre-tick for npc: $this." }
        }
}
