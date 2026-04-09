package com.lubover.singularity.order.config;

import com.lubover.singularity.api.Actor;
import com.lubover.singularity.api.Registry;
import com.lubover.singularity.api.ShardPolicy;
import com.lubover.singularity.api.Slot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Configuration
public class AllocatorRuntimeConfig {

    @Bean
    public Registry registry(@Value("${order.alloc.slot-count:16}") int slotCount) {
        int normalizedCount = Math.max(slotCount, 1);
        List<Slot> slots = java.util.stream.IntStream.range(0, normalizedCount)
                .mapToObj(index -> new StaticSlot("slot-" + index))
                .map(slot -> (Slot) slot)
                .toList();
        return () -> slots;
    }

    @Bean
    public ShardPolicy shardPolicy() {
        return (actor, slotList) -> {
            if (slotList == null || slotList.isEmpty()) {
                return Optional.empty();
            }

            Actor safeActor = actor;
            String actorId = safeActor == null ? null : safeActor.getId();
            if (actorId == null || actorId.isBlank()) {
                return Optional.of(slotList.get(0));
            }

            int index = Math.floorMod(actorId.hashCode(), slotList.size());
            return Optional.of(slotList.get(index));
        };
    }

    private static final class StaticSlot implements Slot {
        private final String id;
        private final Map<String, Object> metadata;

        private StaticSlot(String id) {
            this.id = id;
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("source", "order-runtime");
            this.metadata = Collections.unmodifiableMap(map);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public Map<String, ?> getMetadata() {
            return metadata;
        }
    }
}
