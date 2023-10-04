package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.GuiElement;
import org.lwjgl.input.Keyboard;

public class ChildEntry extends ModListEntry {
	private boolean bottomChild;
	private ParentEntry parent;

	public ChildEntry(Mod mod, ParentEntry parent, ModListWidget list, boolean bottomChild) {
		super(mod, list);
		this.bottomChild = bottomChild;
		this.parent = parent;
	}

	@Override
	public void render(int index, int x, int y, int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered) {
		super.render(index, x, y, rowWidth, rowHeight, mouseX, mouseY, hovered);
		x += 4;
		int color = 0xFFA0A0A0;
		GuiElement.fill(x, y - 2, x + 1, y + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);
		GuiElement.fill(x, y + rowHeight / 2, x + 7, y + rowHeight / 2 + 1, color);
	}

	@Override
	public boolean keyPressed(char chr, int key) {
		if (key == Keyboard.KEY_LEFT) {
			list.setSelected(parent);
			return true;
		}
		return false;
	}

	@Override
	public int getXOffset() {
		return 13;
	}
}
