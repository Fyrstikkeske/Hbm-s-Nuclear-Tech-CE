package com.hbm.lib;

import com.hbm.handler.WeightedRandomChestContentFrom1710;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class HbmChestContents {

	public static WeightedRandomChestContentFrom1710 weighted(Item item, int meta, int min, int max, int weight) { return new WeightedRandomChestContentFrom1710(item, meta, Math.min(min, max), Math.max(min, max), weight); }
	public static WeightedRandomChestContentFrom1710 weighted(Block block, int meta, int min, int max, int weight) { return new WeightedRandomChestContentFrom1710(Item.getItemFromBlock(block), meta, Math.min(min, max), Math.max(min, max), weight); }
	public static WeightedRandomChestContentFrom1710 weighted(ItemStack item, int min, int max, int weight) { return new WeightedRandomChestContentFrom1710(item, Math.min(min, max), Math.max(min, max), weight); }

	/** ITEMBOOKLORE SHIT */
	//one downside of all this huge flexibility, make a wrapper if it's too annoying
	/*public static ItemStack generateOfficeBook(Random rand) { //TODO make ItemBookLore
		String key;
		int pages;
		switch(rand.nextInt(5)) {
			case 0: key = "resignation_note"; pages = 3; break;
			case 1: key = "memo_stocks"; pages = 1; break;
			case 2: key = "memo_schrab_gsa"; pages = 2; break;
			case 3: key = "memo_schrab_rd"; pages = 4; break;
			case 4: key = "memo_schrab_nuke"; pages = 3; break;
			default: return null;
		}

		return ItemBookLore.createBook(key, pages, 0x6BC8FF, 0x0A0A0A);
	}

	public static ItemStack generateLabBook(Random rand) {
		String key;
		int pages;

		switch(rand.nextInt(5)) {
			case 0: key = "bf_bomb_1"; pages = 4; break;
			case 1: key = "bf_bomb_2"; pages = 6; break;
			case 2: key = "bf_bomb_3"; pages = 6; break;
			case 3: key = "bf_bomb_4"; pages = 5; break;
			case 4: key = "bf_bomb_5"; pages = 9; break;
			default: return null;
		}

		return ItemBookLore.createBook(key, pages, 0x1E1E1E, 0x46EA44);
	}*/
}
