package com.hbm.items.machine;

import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.items.ItemEnumMulti;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemPistons extends ItemEnumMulti {

  public ItemPistons(String name) {
    super(name, EnumPistonType.class, true, true);
  }

  @Override
  public void addInformation(
      ItemStack stack,
      @Nullable World worldIn,
      List<String> tooltip,
      @NotNull ITooltipFlag flagIn) {
    EnumPistonType type = EnumUtil.grabEnumSafely(theEnum, stack.getItemDamage());

    tooltip.add(ChatFormatting.YELLOW + "Fuel efficiency:");
    for (int i = 0; i < type.eff.length; i++) {
      tooltip.add(
          ChatFormatting.YELLOW
              + "-"
              + I18nUtil.resolveKey(FT_Combustible.FuelGrade.values()[i].getGrade())
              + ": "
              + ChatFormatting.RED
              + (int) (type.eff[i] * 100)
              + "%");
    }
  }

  public enum EnumPistonType {
    STEEL(1.00, 0.75, 0.25, 0.00, 0.00),
    DURA(0.50, 1.00, 0.90, 0.50, 0.00),
    DESH(0.00, 0.50, 1.00, 0.75, 0.00),
    STARMETAL(0.50, 0.75, 1.00, 0.90, 0.50);

    public final double[] eff;

    EnumPistonType(double... eff) {
      this.eff = new double[Math.min(FT_Combustible.FuelGrade.values().length, eff.length)];
      System.arraycopy(eff, 0, this.eff, 0, eff.length);
    }
  }
}
