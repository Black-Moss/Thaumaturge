package com.leclowndu93150.thaumaturge.content.entity;

import net.minecraft.server.level.ServerLevel;

interface RiftTask {
    int interval();

    void run(ServerLevel level, EntityFluxRift rift);
}
