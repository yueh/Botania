/**
 * This class was created by <Vazkii>. It's distributed as
 * part of the Botania Mod. Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 * 
 * Botania is Open Source and distributed under a
 * Creative Commons Attribution-NonCommercial-ShareAlike 3.0 License
 * (http://creativecommons.org/licenses/by-nc-sa/3.0/deed.en_GB)
 * 
 * File Created @ [Jan 31, 2014, 3:02:58 PM (GMT)]
 */
package vazkii.botania.common.item;

import java.awt.Color;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import vazkii.botania.api.internal.IManaBurst;
import vazkii.botania.api.mana.BurstProperties;
import vazkii.botania.api.mana.ILens;
import vazkii.botania.client.core.helper.IconHelper;
import vazkii.botania.client.core.helper.Vector3;
import vazkii.botania.common.core.helper.ItemNBTHelper;
import vazkii.botania.common.lib.LibItemIDs;
import vazkii.botania.common.lib.LibItemNames;

public class ItemLens extends ItemMod implements ILens {

	private static final String TAG_COLOR = "color";
	
	public static Icon iconGlass;
	
	final int subtypes = 9;
	Icon[] ringIcons;
	
	public ItemLens() {
		super(LibItemIDs.idLens);
		setUnlocalizedName(LibItemNames.LENS);
		setMaxStackSize(1);
		setHasSubtypes(true);
	}
	
	@Override
	public void registerIcons(IconRegister par1IconRegister) {
		iconGlass = IconHelper.forName(par1IconRegister, "lensInside");
		
		ringIcons = new Icon[subtypes];
		for(int i = 0; i < ringIcons.length; i++)
			ringIcons[i] = IconHelper.forNameRaw(par1IconRegister, LibItemNames.LENS_NAMES[i]);
	}
	
	@Override
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List par3List) {
		for(int i = 0; i < subtypes; i++) {
			for(int j = -1; j < 17; j++) {
				ItemStack stack = new ItemStack(par1, 1, i);
				setLensColor(stack, j);
				par3List.add(stack);
			}
		}
	}
	
	@Override
	public boolean requiresMultipleRenderPasses() {
		return true;
	}
	
	@Override
	public Icon getIconFromDamageForRenderPass(int par1, int par2) {
		return par2 == 0 ? ringIcons[Math.min(subtypes - 1, par1)] : iconGlass;
	}
	
	@Override
	public Icon getIconFromDamage(int par1) {
		return getIconFromDamageForRenderPass(par1, 0);
	}

	@Override
	public int getColorFromItemStack(ItemStack par1ItemStack, int par2) {
		return par2 == 1 ? getLensColor(par1ItemStack) : 0xFFFFFF;
	}
	
	@Override
	public String getUnlocalizedName(ItemStack par1ItemStack) {
		return "item." + LibItemNames.LENS_NAMES[Math.min(subtypes - 1, par1ItemStack.getItemDamage())];
	}
	
	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4) {
		int storedColor = getStoredColor(par1ItemStack);
		if(storedColor != -1)
			par3List.add(String.format(StatCollector.translateToLocal("botaniamisc.color"), StatCollector.translateToLocal("botania.color" + storedColor)));
	}
	
	@Override
	public void apply(ItemStack stack, BurstProperties props) {
		int storedColor = getStoredColor(stack);
		if(storedColor != -1)
			props.color = getLensColor(stack);
		
		switch(stack.getItemDamage()) {
		case 1 : { // Speed
			props.motionModifier = 2F;
			props.maxMana *= 0.5F;
			props.ticksBeforeManaLoss /= 3F;
			props.manaLossPerTick *= 2F;
			break;
		}
		case 2 : { // Potency
			props.maxMana *= 2;
			props.motionModifier *= 0.85F;
			props.manaLossPerTick *= 2F;
			break;
		}
		case 3 : { // Resistance
			props.ticksBeforeManaLoss *= 2.25;
			props.motionModifier *= 0.8F;
			break;
		}
		case 4 : { // Efficiency
			props.manaLossPerTick /= 5F;
			props.ticksBeforeManaLoss *= 1.1F;
			break;
		}
		case 6 : { // Gravity
			props.gravity = 0.0015F;
			break;
		}
		}
	}
	
	@Override
	public void collideBurst(IManaBurst burst, MovingObjectPosition pos, boolean isManaBlock, ItemStack stack) {
		switch(stack.getItemDamage()) {
		case 5 : {
			if(!isManaBlock && pos.entityHit == null) {
				ChunkCoordinates coords = burst.getBurstSourceChunkCoordinates();
				if(coords.posX != pos.blockX || coords.posY != pos.blockY || coords.posZ != pos.blockZ) {
					EntityThrowable entity = (EntityThrowable) burst;
					Vector3 currentMovementVec = new Vector3(entity.motionX, entity.motionY, entity.motionZ);
					ForgeDirection dir = ForgeDirection.getOrientation(pos.sideHit);
					Vector3 normalVector = new Vector3(dir.offsetX, dir.offsetY, dir.offsetZ).normalize();
					Vector3 movementVec = normalVector.multiply(-2 * currentMovementVec.dotProduct(normalVector)).add(currentMovementVec);
					
					burst.setMotion(movementVec.x, movementVec.y, movementVec.z);
					entity.isDead = false;
				}
			}
		}
		}
	}
	
	@Override
	public void updateBurst(IManaBurst burst, ItemStack stack) {
		int storedColor = getStoredColor(stack);
		if(storedColor == 16)
			burst.setColor(getLensColor(stack));
	}

	@Override
	public int getLensColor(ItemStack stack) {
		int storedColor = getStoredColor(stack);
		
		if(storedColor == -1)
			return 0xFFFFFF;
		
		if(storedColor == 16) {
			World world = Minecraft.getMinecraft().theWorld;
			return world == null ? 0xFFFFFF : Color.HSBtoRGB((float) ((world.getTotalWorldTime() * 2) % 360) / 360F, 1F, 1F);
		}
		
		float[] color = EntitySheep.fleeceColorTable[storedColor];
		return new Color(color[0], color[1], color[2]).getRGB();
	}
	
	public int getStoredColor(ItemStack stack) {
		return ItemNBTHelper.getInt(stack, TAG_COLOR, -1);
	}
	
	public void setLensColor(ItemStack stack, int color) {
		ItemNBTHelper.setInt(stack, TAG_COLOR, color);
	}
}