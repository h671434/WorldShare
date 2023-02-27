package ankel.worlshare.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.list.ExtendedList;

public class AddWorldList extends ExtendedList<AddWorldList.Entry> {
	
	
	public AddWorldList(Minecraft p_i45010_1_, int p_i45010_2_, int p_i45010_3_, int p_i45010_4_, int p_i45010_5_,
			int p_i45010_6_) {
		super(p_i45010_1_, p_i45010_2_, p_i45010_3_, p_i45010_4_, p_i45010_5_, p_i45010_6_);
		// TODO Auto-generated constructor stub
	}

	public static class Entry extends ExtendedList.AbstractListEntry<AddWorldList.Entry> {

		@Override
		public void render(MatrixStack pMatrixStack, int pIndex, int pTop, int pLeft, int pWidth, int pHeight,
				int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTicks) {
			// TODO Auto-generated method stub
			
		}
	}
}
