package com.terraformersmc.modmenu.gui.widget;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.terraformersmc.modmenu.config.option.ConfigOption;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;

public class ConfigOptionListWidget extends EntryListWidget {
	private final List<Entry> entries = new ArrayList<>();

	private int nextId;

	public ConfigOptionListWidget(Minecraft minecraft, int width, int height, int yStart, int yEnd, int entryHeight, ConfigOption ... options) {
        super(minecraft, width, height, yStart, yEnd, entryHeight);
        this.centerAlongY = false;
        for (int i = 0; i < options.length; i += 2) {
            ConfigOption option = options[i];
            ConfigOption option2 = i < options.length - 1 ? options[i + 1] : null;
            this.entries.add(new Entry(width, option, option2));
        }
    }

	@Nullable
	private static ButtonWidget createWidget(final Minecraft minecraft, int id, int x, int y, int width, final @Nullable ConfigOption option) {
		if (option == null) {
			return null;
		}
		ButtonWidget button = new OptionButtonWidget(id, x, y, null, option.getValueLabel());
		button.setWidth(width);
		return button;
	}

	@Override
	public Entry getEntry(int i) {
		return this.entries.get(i);
	}

	@Override
	protected int size() {
		return this.entries.size();
	}

	@Override
	public int getRowWidth() {
		return 400;
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 32;
	}

	public boolean isMouseInList(int mouseX, int mouseY) {
		return mouseY >= this.minY && mouseY <= this.maxY && mouseX >= this.minX && mouseX <= this.maxX;
	}

	public final class Entry implements EntryListWidget.Entry {
		@Nullable
		private final ConfigOption leftOption;
		@Nullable
		private final ButtonWidget left;
		@Nullable
		private final ConfigOption rightOption;
		@Nullable
		private final ButtonWidget right;

		public Entry(@Nullable ButtonWidget left, @Nullable ConfigOption leftOption, ButtonWidget right, @Nullable ConfigOption rightOption) {
			this.left = left;
			this.leftOption = leftOption;
			this.right = right;
			this.rightOption = rightOption;
		}

		public Entry(int x, ConfigOption option) {
			this(ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155, 0, 310, option), option, null, null);
		}

		public Entry(int x, @Nullable ConfigOption option, ConfigOption option2) {
			this(ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155, 0, 150, option), option,
					ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155 + 160, 0, 150, option2), option2);
		}

		@Override
		public void render(int index, int x, int y, int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (this.left != null) {
				this.left.y = y;
				this.left.render(minecraft, mouseX, mouseY, tickDelta);
			}
			if (this.right != null) {
				this.right.y = y;
				this.right.render(minecraft, mouseX, mouseY, tickDelta);
			}
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			if (button == 0) {
				if (this.left != null && this.left.isMouseOver(minecraft, mouseX, mouseY)) {
					this.leftOption.click();
					this.left.playDownSound(minecraft.getSoundManager());
					this.left.message = this.leftOption.getValueLabel();
					return true;
				}
				if (this.right != null && this.right.isMouseOver(minecraft, mouseX, mouseY)) {
					this.rightOption.click();
					this.right.playDownSound(minecraft.getSoundManager());
					this.right.message = this.rightOption.getValueLabel();
					return true;
				}
			}
			return false;
		}

		@Override
		public void mouseReleased(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			this.left.mouseReleased(mouseX, mouseY);
			this.right.mouseReleased(mouseX, mouseY);
		}

		@Override
		public void renderOutOfBounds(int index, int x, int y, float tickDelta) {
		}
	}
}
