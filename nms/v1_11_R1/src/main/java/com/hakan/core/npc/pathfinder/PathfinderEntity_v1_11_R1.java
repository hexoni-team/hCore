package com.hakan.core.npc.pathfinder;

import com.hakan.core.HCore;
import com.hakan.core.listener.ListenerAdapter;
import com.hakan.core.utils.Validate;
import net.minecraft.server.v1_11_R1.EntityPig;
import net.minecraft.server.v1_11_R1.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_11_R1.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_11_R1.CraftWorld;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * PathfinderEntity is a
 * custom entity class.
 */
public final class PathfinderEntity_v1_11_R1 {

    /**
     * Creates a new PathfinderEntity
     * instance and add it to world.
     *
     * @param start        Location to spawn.
     * @param end          Location to end.
     * @param speed        Speed of entity.
     * @param walkRunnable Runnable to run when entity is walking.
     * @param endRunnable  Runnable to run when entity is ended.
     * @return PathfinderEntity_v1_11_R1 instance.
     */
    public static PathfinderEntity_v1_11_R1 create(@Nonnull Location start,
                                                   @Nonnull Location end,
                                                   double speed,
                                                   @Nonnull Consumer<EntityPig> walkRunnable,
                                                   @Nonnull Consumer<EntityPig> endRunnable) {
        Validate.notNull(start, "start location cannot be null!");
        Validate.notNull(start.getWorld(), "start world cannot be null!");

        World world = ((CraftWorld) start.getWorld()).getHandle();
        return new PathfinderEntity_v1_11_R1(world, start, end, speed, walkRunnable, endRunnable);
    }



    private final EntityPig entityPig;

    /**
     * Creates a new PathfinderEntity instance.
     *
     * @param world        World to spawn in.
     * @param start        Location to spawn.
     * @param end          Location to end.
     * @param speed        Speed of entity.
     * @param walkRunnable Runnable to run when entity is walking.
     * @param endRunnable  Runnable to run when entity is ended.
     */
    private PathfinderEntity_v1_11_R1(@Nonnull World world,
                                      @Nonnull Location start,
                                      @Nonnull Location end,
                                      double speed,
                                      @Nonnull Consumer<EntityPig> walkRunnable,
                                      @Nonnull Consumer<EntityPig> endRunnable) {
        this.entityPig = new EntityPig(Validate.notNull(world));

        Validate.notNull(start, "start location cannot be null!");
        Validate.notNull(end, "end location cannot be null!");
        Validate.notNull(walkRunnable, "walk runnable cannot be null!");
        Validate.notNull(endRunnable, "end runnable cannot be null!");

        this.entityPig.setSilent(true);
        this.entityPig.setInvisible(true);
        this.entityPig.setInvulnerable(true);
        this.entityPig.setCustomNameVisible(false);
        this.entityPig.setPosition(start.getX(), start.getY(), start.getZ());
        this.entityPig.setHealth(2.518f);
        world.addEntity(this.entityPig);

        ListenerAdapter<PlayerJoinEvent> listenerAdapter = HCore.registerEvent(PlayerJoinEvent.class)
                .consumeAsync(event -> HCore.sendPacket(event.getPlayer(), new PacketPlayOutEntityDestroy(this.entityPig.getId())));
        HCore.sendPacket(new ArrayList<>(Bukkit.getOnlinePlayers()),
                new PacketPlayOutEntityDestroy(this.entityPig.getId()));

        this.entityPig.goalSelector.a(2, new PathfinderGoal_v1_11_R1(this.entityPig, end, speed,
                () -> walkRunnable.accept(this.entityPig),
                () -> {
                    listenerAdapter.unregister();
                    endRunnable.accept(this.entityPig);
                }));
    }
}