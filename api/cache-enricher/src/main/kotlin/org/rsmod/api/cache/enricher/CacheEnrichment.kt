package org.rsmod.api.cache.enricher

import jakarta.inject.Inject
import org.openrs2.cache.Cache
import org.rsmod.api.cache.enricher.loc.LocCacheEnricher
import org.rsmod.api.cache.enricher.npc.NpcCacheEnricher
import org.rsmod.api.cache.enricher.obj.ObjCacheEnricher
import org.rsmod.api.cache.types.loc.LocTypeEncoder
import org.rsmod.api.cache.types.npc.NpcTypeEncoder
import org.rsmod.api.cache.types.obj.ObjTypeEncoder
import org.rsmod.api.cache.util.EncoderContext
import org.rsmod.game.type.loc.LocTypeList
import org.rsmod.game.type.npc.NpcTypeList
import org.rsmod.game.type.obj.ObjTypeList
import org.rsmod.game.type.param.ParamTypeList

public class CacheEnrichment
@Inject
constructor(
    private val locTypes: LocTypeList,
    private val locEnrichments: Set<LocCacheEnricher>,
    private val npcTypes: NpcTypeList,
    private val npcEnrichments: Set<NpcCacheEnricher>,
    private val objTypes: ObjTypeList,
    private val objEnrichments: Set<ObjCacheEnricher>,
    private val paramTypes: ParamTypeList,
) {
    public fun encodeAll(dest: Cache) {
        val encoderContext = EncoderContext(encodeFull = true, paramTypes.filterTransmitKeys())
        dest.use { cache ->
            val locs = locEnrichments.collect(locTypes).asIterable()
            val npcs = npcEnrichments.collect(npcTypes).asIterable()
            val objs = objEnrichments.collect(objTypes).asIterable()
            LocTypeEncoder.encodeAll(cache, locs, encoderContext)
            NpcTypeEncoder.encodeAll(cache, npcs, encoderContext)
            ObjTypeEncoder.encodeAll(cache, objs, encoderContext)
        }
    }

    private fun <T, E : CacheEnricher<T>> Set<E>.collect(
        cacheTypes: Map<Int, T>,
        reference: CacheEnricher<T> = first(),
    ): Map<Int, T> {
        val list = map(CacheEnricher<T>::generate).flatten()
        val grouped = list.groupBy(reference::idOf)
        val merged = mutableMapOf<Int, T>()
        for ((id, types) in grouped) {
            check(types.isNotEmpty()) {
                "Grouped types for enricher must not be empty: enricher=$reference"
            }
            val cacheType = cacheTypes[id] ?: continue // Skip types not in cache.
            if (types.size == 1) {
                merged[id] = reference.merge(cacheType, types[0])
                continue
            }
            val folded = types.fold(types[0]) { curr, next -> reference.merge(next, curr) }
            merged[id] = reference.merge(cacheType, folded)
        }
        return merged
    }

    private fun <T> Map<Int, T>.asIterable(): Iterable<T> = values
}
