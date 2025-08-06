package com.hbm.entity.projectile;

import com.hbm.entity.logic.IChunkLoader;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.main.MainRegistry;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;

import java.util.ArrayList;
import java.util.List;
@AutoRegister(name = "entity_bullet_mk4_cl", sendVelocityUpdates = false)
public class EntityBulletBaseMK4CL extends EntityBulletBaseMK4 implements IChunkLoader {
    private ForgeChunkManager.Ticket loaderTicket;
    private List<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();

    public EntityBulletBaseMK4CL(World world) {
        super(world);
    }

    public EntityBulletBaseMK4CL(EntityLivingBase entity, BulletConfig config, float damage, float spread, double sideOffset, double heightOffset, double forwardOffset) {
        super(entity, config, damage, spread, sideOffset, heightOffset, forwardOffset);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        init(ForgeChunkManager.requestTicket(MainRegistry.instance, world, ForgeChunkManager.Type.ENTITY));
    }

    @Override
    public void init(ForgeChunkManager.Ticket ticket) {
        if(!world.isRemote && ticket != null) {
            if(loaderTicket == null) {
                loaderTicket = ticket;
                loaderTicket.bindEntity(this);
                loaderTicket.getModData();
            }
            ForgeChunkManager.forceChunk(loaderTicket, new ChunkPos(chunkCoordX, chunkCoordZ));
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if(!world.isRemote) loadNeighboringChunks((int)Math.floor(posX / 16D), (int)Math.floor(posZ / 16D));
    }

    @Override
    public void setDead() {
        super.setDead();
        clearChunkLoader();
    }

    public void clearChunkLoader() {
        if(!world.isRemote && loaderTicket != null) {
            for(ChunkPos chunk : loadedChunks) {
                ForgeChunkManager.unforceChunk(loaderTicket, chunk);
            }
        }
    }

    public void loadNeighboringChunks(int newChunkX, int newChunkZ) {
        if(!world.isRemote && loaderTicket != null) {

            clearChunkLoader();
            loadedChunks.clear();
            loadedChunks.add(new ChunkPos(newChunkX, newChunkZ));

            for(ChunkPos chunk : loadedChunks) {
                ForgeChunkManager.forceChunk(loaderTicket, chunk);
            }
        }
    }
}
