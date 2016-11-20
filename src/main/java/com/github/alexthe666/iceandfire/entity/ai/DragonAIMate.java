package com.github.alexthe666.iceandfire.entity.ai;

import java.util.List;
import java.util.Random;

import com.github.alexthe666.iceandfire.entity.EntityDragonEgg;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.item.EntityXPOrb;
import com.github.alexthe666.iceandfire.entity.EntityDragonBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.stats.AchievementList;
import net.minecraft.stats.StatList;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.World;

public class DragonAIMate extends EntityAIBase
{
    private final EntityDragonBase dragon;
    World theWorld;
    private EntityDragonBase targetMate;
    int spawnBabyDelay;
    double moveSpeed;

    public DragonAIMate(EntityDragonBase dragon, double speedIn) {
        this.dragon = dragon;
        this.theWorld = dragon.worldObj;
        this.moveSpeed = speedIn;
        this.setMutexBits(3);
    }

    public boolean shouldExecute()
    {
        if (!this.dragon.isInLove())
        {
            return false;
        }
        else
        {
            this.targetMate = this.getNearbyMate();
            return this.targetMate != null;
        }
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean continueExecuting()
    {
        return this.targetMate.isEntityAlive() && this.targetMate.isInLove() && this.spawnBabyDelay < 60;
    }

    /**
     * Resets the task
     */
    public void resetTask()
    {
        this.targetMate = null;
        this.spawnBabyDelay = 0;
    }

    /**
     * Updates the task
     */
    public void updateTask()
    {
        this.dragon.getLookHelper().setLookPositionWithEntity(this.targetMate, 10.0F, (float)this.dragon.getVerticalFaceSpeed());
        this.dragon.getNavigator().tryMoveToEntityLiving(this.targetMate, this.moveSpeed);
        ++this.spawnBabyDelay;
        if (this.spawnBabyDelay >= 60 && this.dragon.getDistanceSqToEntity(this.targetMate) < 12.0D) {
            this.spawnBaby();
        }
    }

    /**
     * Loops through nearby animals and finds another animal of the same type that can be mated with. Returns the first
     * valid mate found.
     */
    private EntityDragonBase getNearbyMate()
    {
        List<EntityDragonBase> list = this.theWorld.<EntityDragonBase>getEntitiesWithinAABB(this.dragon.getClass(), this.dragon.getEntityBoundingBox().expandXyz(8.0D));
        double d0 = Double.MAX_VALUE;
        EntityDragonBase EntityDragonBase = null;

        for (EntityDragonBase EntityDragonBase1 : list)
        {
            if (this.dragon.canMateWith(EntityDragonBase1) && this.dragon.getDistanceSqToEntity(EntityDragonBase1) < d0)
            {
                EntityDragonBase = EntityDragonBase1;
                d0 = this.dragon.getDistanceSqToEntity(EntityDragonBase1);
            }
        }

        return EntityDragonBase;
    }

    /**
     * Spawns a baby animal of the same type.
     */
    private void spawnBaby() {

        EntityDragonEgg egg = this.dragon.createEgg(this.targetMate);

        if (egg != null)
        {
            EntityPlayer entityplayer = this.dragon.getPlayerInLove();

            if (entityplayer == null && this.targetMate.getPlayerInLove() != null)
            {
                entityplayer = this.targetMate.getPlayerInLove();
            }

            if (entityplayer != null)
            {
                entityplayer.addStat(StatList.ANIMALS_BRED);
            }

            this.dragon.setGrowingAge(6000);
            this.targetMate.setGrowingAge(6000);
            this.dragon.resetInLove();
            this.targetMate.resetInLove();
            egg.setLocationAndAngles(this.dragon.posX, this.dragon.posY, this.dragon.posZ, 0.0F, 0.0F);
            this.theWorld.spawnEntityInWorld(egg);
            Random random = this.dragon.getRNG();

            for (int i = 0; i < 17; ++i)
            {
                double d0 = random.nextGaussian() * 0.02D;
                double d1 = random.nextGaussian() * 0.02D;
                double d2 = random.nextGaussian() * 0.02D;
                double d3 = random.nextDouble() * (double)this.dragon.width * 2.0D - (double)this.dragon.width;
                double d4 = 0.5D + random.nextDouble() * (double)this.dragon.height;
                double d5 = random.nextDouble() * (double)this.dragon.width * 2.0D - (double)this.dragon.width;
                this.theWorld.spawnParticle(EnumParticleTypes.HEART, this.dragon.posX + d3, this.dragon.posY + d4, this.dragon.posZ + d5, d0, d1, d2, new int[0]);
            }

            if (this.theWorld.getGameRules().getBoolean("doMobLoot"))
            {
                this.theWorld.spawnEntityInWorld(new EntityXPOrb(this.theWorld, this.dragon.posX, this.dragon.posY, this.dragon.posZ, random.nextInt(15) + 10));
            }
        }
    }
}