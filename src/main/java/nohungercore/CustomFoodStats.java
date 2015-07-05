package nohungercore;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.FoodStats;

public class CustomFoodStats extends FoodStats
{
    private EntityPlayer player;

    public CustomFoodStats(EntityPlayer player)
    {
        this.player = player;
    }

    @Override
    public void addStats(int food, float saturation)
    {
        player.setHealth(player.getHealth() + food);
    }

    @Override
    public boolean needFood()
    {
        return player.shouldHeal();
    }

    @Override
    public void onUpdate(EntityPlayer player) {}

    @Override
    public int getFoodLevel()
    {
        return 19;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setFoodLevel(int i) {}
}
