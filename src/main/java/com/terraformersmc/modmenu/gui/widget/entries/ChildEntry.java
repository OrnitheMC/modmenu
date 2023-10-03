package com.terraformersmc.modmenu.gui.widget.entries;

import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.gui.GuiElement;
import org.lwjgl.glfw.GLFW;

public class ChildEntry extends ModListEntry {
	private boolean bottomChild;
	private ParentEntry parent;

	public ChildEntry(Mod mod, ParentEntry parent, ModListWidget list, boolean bottomChild) {
		super(mod, list);
		this.bottomChild = bottomChild;
		this.parent = parent;
	}

	@Override
	public void render(int rowWidth, int rowHeight, int mouseX, int mouseY, boolean isSelected, float delta) {
		super.render(rowWidth, rowHeight, mouseX, mouseY, isSelected, delta);
		int x = getX() + 4;
		int y = getY();
		int color = 0xFFA0A0A0;
		GuiElement.fill(x, y - 2, x + 1, y + (bottomChild ? rowHeight / 2 : rowHeight + 2), color);
		GuiElement.fill(x, y + rowHeight / 2, x + 7, y + rowHeight / 2 + 1, color);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_LEFT) {
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
