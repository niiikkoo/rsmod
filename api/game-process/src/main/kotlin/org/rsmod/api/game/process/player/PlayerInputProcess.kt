package org.rsmod.api.game.process.player

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.api.player.forceDisconnect
import org.rsmod.game.MapClock
import org.rsmod.game.entity.PathingEntity
import org.rsmod.game.entity.Player
import org.rsmod.game.entity.PlayerList

public class PlayerInputProcess
@Inject
constructor(private val mapClock: MapClock, private val players: PlayerList) {
    private val logger = InlineLogger()

    public fun process() {
        players.forEach { player ->
            player.tryOrDisconnect {
                updateMapClock()
                readClientInput()
            }
        }
    }

    /**
     * Updates the player's `currentMapClock` to match the latest value from the global `mapClock`.
     *
     * This ensures that any `delay` calls set by an incoming packet (e.g., an `IfButton` click for
     * an emote using delays) use an up-to-date reference for the map clock. Without this update,
     * delays would appear to be "sped up" by one cycle because the `delay` functions rely on the
     * [PathingEntity.currentMapClock] as a baseline to calculate their delay duration.
     */
    private fun Player.updateMapClock() {
        currentMapClock = mapClock.cycle
    }

    private fun Player.readClientInput() {
        client.read(this)
    }

    private inline fun Player.tryOrDisconnect(block: Player.() -> Unit) =
        try {
            block(this)
        } catch (e: Exception) {
            forceDisconnect()
            logger.error(e) { "Error processing input cycle for player: $this." }
        } catch (e: NotImplementedError) {
            forceDisconnect()
            logger.error(e) { "Error processing input cycle for player: $this." }
        }
}
