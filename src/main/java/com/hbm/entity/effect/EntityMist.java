package com.hbm.entity.effect;


import com.hbm.capability.HbmLivingProps;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.RadiationSystemNT;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.*;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Gaseous_ART;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Liquid;
import com.hbm.inventory.fluid.trait.FluidTraitSimple.FT_Viscous;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class EntityMist extends Entity {

    private static final DataParameter<Float> WIDTH = EntityDataManager.createKey(EntityMist.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> HEIGHT = EntityDataManager.createKey(EntityMist.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> TYPE = EntityDataManager.createKey(EntityMist.class, DataSerializers.VARINT);
    public int maxAge = 150;

    public EntityMist(World world) {
        super(world);
        this.noClip = true;
    }

    public static SprayStyle getStyleFromType(FluidType type) {

        if (type.hasTrait(FT_Viscous.class)) {
            return SprayStyle.NULL;
        }

        if (type.hasTrait(FT_Gaseous.class) || type.hasTrait(FT_Gaseous_ART.class)) {
            return SprayStyle.GAS;
        }

        if (type.hasTrait(FT_Liquid.class)) {
            return SprayStyle.MIST;
        }

        return SprayStyle.NULL;
    }

    public EntityMist setArea(float width, float height) {
        this.dataManager.set(WIDTH, width);
        this.dataManager.set(HEIGHT, height);
        return this;
    }

    public EntityMist setDuration(int duration) {
        this.maxAge = duration;
        return this;
    }

    @Override
    protected void entityInit() {
        this.dataManager.register(TYPE, Integer.valueOf(0));
        this.dataManager.register(WIDTH, Float.valueOf(0));
        this.dataManager.register(HEIGHT, Float.valueOf(0));
    }

    public FluidType getType() {
        return Fluids.fromID(this.dataManager.get(TYPE));
    }

    public EntityMist setType(FluidType fluid) {
        this.dataManager.set(TYPE, fluid.getID());
        return this;
    }

    @Override
    public void onEntityUpdate() {

        float height = this.dataManager.get(HEIGHT);
        float width = this.dataManager.get(WIDTH);

        this.setSize(-width, height);
        this.setPosition(this.posX, this.posY, this.posZ);

        if (!world.isRemote) {

            if (this.ticksExisted >= this.getMaxAge()) {
                this.setDead();
            }

            FluidType type = this.getType();

            if (type.hasTrait(FT_VentRadiation.class)) {
                FT_VentRadiation trait = type.getTrait(FT_VentRadiation.class);
                //ChunkRadiationManager.proxy.incrementRad(world, (int) Math.floor(posX), (int) Math.floor(posY), (int) Math.floor(posZ), trait.getRadPerMB() * 2);
                //TODO: add newer radiation
                RadiationSystemNT.incrementRad(world, this.getPosition(), trait.getRadPerMB(), Integer.MAX_VALUE);

            }

            double intensity = 1D - (double) this.ticksExisted / (double) this.getMaxAge();

            if (type.hasTrait(FT_Flammable.class) && this.isBurning()) {
                world.createExplosion(this, posX, posY + height / 2, posZ, (float) intensity * 15F, true);
                this.setDead();
                return;
            }

            AxisAlignedBB aabb = this.getEntityBoundingBox();
            List<Entity> affected = world.getEntitiesWithinAABBExcludingEntity(this, aabb); //It has no offset now

            for (Entity e : affected) {
                if (!(e instanceof EntityMist))
                    this.affect(e, intensity);
            }
        } else {

            for (int i = 0; i < 2; i++) {
                AxisAlignedBB boundingBox = this.getEntityBoundingBox();
                double x = boundingBox.minX + rand.nextDouble() * (boundingBox.maxX - boundingBox.minX);
                double y = boundingBox.minY + rand.nextDouble() * (boundingBox.maxY - boundingBox.minY);
                double z = boundingBox.minZ + rand.nextDouble() * (boundingBox.maxZ - boundingBox.minZ);


                NBTTagCompound fx = new NBTTagCompound();
                fx.setString("type", "tower");
                fx.setFloat("lift", 0.5F);
                fx.setFloat("base", 0.75F);
                fx.setFloat("max", 2F);
                fx.setInteger("life", 50 + world.rand.nextInt(10));
                fx.setInteger("color", this.getType().getColor());
                fx.setDouble("posX", x);
                fx.setDouble("posY", y);
                fx.setDouble("posZ", z);
                MainRegistry.proxy.effectNT(fx);
            }
        }
    }

    /* can't reuse EntityChemical here, while similar or identical in some places, the actual effects are often different */
    protected void affect(Entity entity, double intensity) {

        FluidType type = this.getType();
        EntityLivingBase living = entity instanceof EntityLivingBase ? (EntityLivingBase) entity : null;

        if (type.temperature >= 100) {
            //TODO
//            EntityDamageUtil.attackEntityFromIgnoreIFrame(e, new DamageSource(ModDamageSource.s_boil), 0.2F + (type.temperature - 100) * 0.02F);
//
//            if(type.temperature >= 500) {
//                e.setFire(10); //afterburn for 10 seconds
//            }
        }
        if (type.temperature < -20) {
            if (living != null) { //only living things are affected
                //TODO: propably add that and use hazard cold
                //EntityDamageUtil.attackEntityFromIgnoreIFrame(e, new DamageSource(ModDamageSource.s_cryolator), 0.2F + (type.temperature + 20) * -0.05F); //5 damage at -20°C with one extra damage every -20°C
                living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 100, 2));
                living.addPotionEffect(new PotionEffect(MobEffects.MINING_FATIGUE, 100, 4));
            }
        }

        if (type.hasTrait(Fluids.DELICIOUS.getClass())) {
            if (living != null && living.isEntityAlive()) {
                living.heal(2F * (float) intensity);
            }
        }

        if (type.hasTrait(FT_Flammable.class) && type.hasTrait(FT_Liquid.class)) {
            if (living != null) {
                HbmLivingProps.setOil(living, 200); //doused in oil for 10 seconds
            }
        }

        if (this.isExtinguishing(type)) {
            entity.extinguish();
        }

        if (type.hasTrait(FT_Corrosive.class)) {
            FT_Corrosive trait = type.getTrait(FT_Corrosive.class);

            if (living != null) {
                EntityDamageUtil.attackEntityFromIgnoreIFrame(living, ModDamageSource.acid, trait.getRating() / 60F);
                if (living instanceof EntityPlayer) {
                    for (int i = 0; i < 4; i++) {
                        ArmorUtil.damageSuit((EntityPlayer) living, i, trait.getRating() / 50);
                    }
                }
            }
        }

        if (type.hasTrait(FT_VentRadiation.class)) {
            FT_VentRadiation trait = type.getTrait(FT_VentRadiation.class);
            if (living != null) {
                ContaminationUtil.contaminate(living, HazardType.RADIATION, ContaminationType.CREATIVE, trait.getRadPerMB() * 5);
            }
        }

        if (type.hasTrait(FT_Poison.class)) {
            FT_Poison trait = type.getTrait(FT_Poison.class);

            if (living != null) {
                living.addPotionEffect(new PotionEffect(trait.isWithering() ? MobEffects.WITHER : MobEffects.POISON, (int) (5 * 20 * intensity)));
            }
        }

        if (type.hasTrait(FT_Toxin.class)) {
            FT_Toxin trait = type.getTrait(FT_Toxin.class);

            if (living != null) {
                trait.affect(living, intensity);
            }
        }

        if (type == Fluids.ENDERJUICE && living != null) {
            teleportRandomly(living);
        }

        //TODO
//        if(type.hasTrait(FT_Pheromone.class)){
//
//            FT_Pheromone pheromone = type.getTrait(FT_Pheromone.class);
//
//            if(living != null) {
//                if ((living instanceof EntityGlyphid && pheromone.getType() == 1) || (living instanceof EntityPlayer && pheromone.getType() == 2)) {
//                    int mult = pheromone.getType();
//
//                    living.addPotionEffect(new PotionEffect(Potion.moveSpeed.id,  mult * 60 * 20, 1));
//                    living.addPotionEffect(new PotionEffect(Potion.digSpeed.id, mult * 60 * 20, 1));
//                    living.addPotionEffect(new PotionEffect(Potion.regeneration.id,  mult * 2 * 20, 0));
//                    living.addPotionEffect(new PotionEffect(Potion.resistance.id,  mult * 60 * 20, 0));
//                    living.addPotionEffect(new PotionEffect(Potion.damageBoost.id,  mult * 60 * 20, 1));
//                    living.addPotionEffect(new PotionEffect(Potion.fireResistance.id,  mult * 60 * 20, 0));
//
//                }
//            }
//        }
    }

    protected boolean isExtinguishing(FluidType type) {
        return this.getType().temperature < 50 && !type.hasTrait(FT_Flammable.class);
    }

    public int getMaxAge() {
        return maxAge;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        this.setType(Fluids.fromID(nbt.getInteger("type")));
        this.setArea(nbt.getFloat("width"), nbt.getFloat("height"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setInteger("type", this.getType().getID());
        nbt.setFloat("width", this.dataManager.get(WIDTH));
        nbt.setFloat("height", this.dataManager.get(HEIGHT));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderOnFire() {
        return false;
    }

    @Override
    public void move(MoverType type, double x, double y, double z) {
    }

    @Override
    public void addVelocity(double x, double y, double z) {
    }

    @Override
    public void setPosition(double x, double y, double z) {
        if (this.ticksExisted == 0) super.setPosition(x, y, z); //honest to fucking god mojang suck my fucking nuts
    }

    //terribly copy-pasted from EntityChemical.class, whose method was terribly copy-pasted from EntityEnderman.class
    //the fun never ends
    public void teleportRandomly(Entity e) {
        double x = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
        double y = this.posY + (double) (this.rand.nextInt(64) - 32);
        double z = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
        this.teleportTo(e, x, y, z);
    }

    public void teleportTo(Entity entity, double x, double y, double z) {

        double targetX = entity.posX;
        double targetY = entity.posY;
        double targetZ = entity.posZ;
        entity.posX = x;
        entity.posY = y;
        entity.posZ = z;
        boolean flag = false;
        BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);


        if (entity.world.isBlockLoaded(pos)) {
            boolean flag1 = false;

            while (!flag1 && pos.getY() > 0) {
                IBlockState block = entity.world.getBlockState(pos.down());

                if (block.getMaterial().blocksMovement()) {
                    flag1 = true;
                } else {
                    --entity.posY;
                    pos.down();
                }
            }

            if (flag1) {
                entity.setPosition(entity.posX, entity.posY, entity.posZ);

                if (entity.world.getCollisionBoxes(entity, entity.getCollisionBoundingBox()).isEmpty() && !entity.world.containsAnyLiquid(entity.getCollisionBoundingBox())) {
                    flag = true;
                }
            }
        }

        if (!flag) {
            entity.setPosition(targetX, targetY, targetZ);
        } else {
            short short1 = 128;

            for (int l = 0; l < short1; ++l) {
                double d6 = (double) l / ((double) short1 - 1.0D);
                float f = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float f2 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                double d7 = targetX + (entity.posX - targetX) * d6 + (this.rand.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
                double d8 = targetY + (entity.posY - targetY) * d6 + this.rand.nextDouble() * (double) entity.height;
                double d9 = targetZ + (entity.posZ - targetZ) * d6 + (this.rand.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
                entity.world.spawnParticle(EnumParticleTypes.PORTAL, d7, d8, d9, f, f1, f2);
            }

            entity.world.playSound(null, targetX, targetY, targetZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            entity.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);

        }

    }

    public static enum SprayStyle {
        MIST,    //liquids that have been sprayed into a mist
        GAS,    //things that were already gaseous
        NULL
    }
}

