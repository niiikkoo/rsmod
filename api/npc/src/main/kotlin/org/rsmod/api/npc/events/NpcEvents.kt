package org.rsmod.api.npc.events

import org.rsmod.events.KeyedEvent
import org.rsmod.events.UnboundEvent
import org.rsmod.game.entity.Npc

public class NpcEvents {
    public data class Create(val npc: Npc) : UnboundEvent

    public data class Spawn(val npc: Npc) : UnboundEvent

    public data class Delete(val npc: Npc) : UnboundEvent

    public data class Hide(val npc: Npc) : UnboundEvent

    public data class Reveal(val npc: Npc) : UnboundEvent
}

public class NpcAIEvents {
    public class Default(public val npc: Npc) : UnboundEvent

    public class Type(public val npc: Npc) : KeyedEvent {
        override val id: Long = npc.id.toLong()
    }

    public class Content(public val npc: Npc, contentGroup: Int) : KeyedEvent {
        override val id: Long = contentGroup.toLong()
    }
}

public class NpcTimerEvents {
    public class Default(public val npc: Npc, timerType: Int) : KeyedEvent {
        override val id: Long = timerType.toLong()
    }

    public class Type(public val npc: Npc, timerType: Int) : KeyedEvent {
        override val id: Long = (npc.id.toLong() shl 32) or timerType.toLong()
    }

    public class Content(public val npc: Npc, contentGroup: Int, timerType: Int) : KeyedEvent {
        override val id: Long = (contentGroup.toLong() shl 32) or timerType.toLong()
    }
}

public class NpcQueueEvents {
    public class Default(public val npc: Npc, queueType: Int) : KeyedEvent {
        override val id: Long = queueType.toLong()
    }

    public class Type(public val npc: Npc, queueType: Int) : KeyedEvent {
        override val id: Long = (npc.id.toLong() shl 32) or queueType.toLong()
    }

    public class Content(public val npc: Npc, contentGroup: Int, queueType: Int) : KeyedEvent {
        override val id: Long = (contentGroup.toLong() shl 32) or queueType.toLong()
    }
}
