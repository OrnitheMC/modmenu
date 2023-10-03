package com.terraformersmc.modmenu.gui.widget;

import org.jetbrains.annotations.Nullable;

import com.terraformersmc.modmenu.config.option.ConfigOption;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.gui.widget.OptionButtonWidget;

public class ConfigOptionListWidget extends EntryListWidget<ConfigOptionListWidget.Entry> {
	private int nextId;

	public ConfigOptionListWidget(Minecraft minecraft, int width, int height, int yStart, int yEnd, int entryHeight, ConfigOption ... options) {
        super(minecraft, width, height, yStart, yEnd, entryHeight);
        this.centerAlongY = false;
        for (int i = 0; i < options.length; i += 2) {
            ConfigOption option = options[i];
            ConfigOption option2 = i < options.length - 1 ? options[i + 1] : null;
            this.add(new Entry(width, option, option2));
        }
    }

	@Nullable
	private static ButtonWidget createWidget(final Minecraft minecraft, int id, int x, int y, int width, final @Nullable ConfigOption option) {
		if (option == null) {
			return null;
		}
		return new OptionButtonWidget(id, x, y, width, 20, null, option.getValueLabel()) {
			@Override
			public void m_9319498(double d, double e) {
				option.click();
				this.message = option.getValueLabel();
			}
		};
	}

	@Override
	public int getRowWidth() {
		return 400;
	}

	@Override
	protected int getScrollbarPosition() {
		return super.getScrollbarPosition() + 32;
	}

	public final class Entry extends EntryListWidget.Entry<Entry> {
		@Nullable
		private final ButtonWidget left;
		@Nullable
		private final ButtonWidget right;

		public Entry(@Nullable ButtonWidget buttonWidget, ButtonWidget buttonWidget2) {
			this.left = buttonWidget;
			this.right = buttonWidget2;
		}

		public Entry(int x, ConfigOption option) {
			this(ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155, 0, 310, option), null);
		}

		public Entry(int x, @Nullable ConfigOption option, ConfigOption option2) {
			this(ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155, 0, 150, option),
					ConfigOptionListWidget.createWidget(minecraft, nextId++, x / 2 - 155 + 160, 0, 150, option2));
		}

		@Override
		public void render(int width, int height, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			if (this.left != null) {
				this.left.y = this.getY();
				this.left.render(mouseX, mouseY, tickDelta);
			}
			if (this.right != null) {
				this.right.y = this.getY();
				this.right.render(mouseX, mouseY, tickDelta);
			}
		}

		@Override
		public boolean mouseClicked(double d, double e, int i) {
			if (this.left.mouseClicked(d, e, i)) {
				return true;
			}
			return this.right != null && this.right.mouseClicked(d, e, i);
		}

		@Override
		public boolean mouseReleased(double d, double e, int i) {
			boolean bl = this.left != null && this.left.mouseReleased(d, e, i);
			boolean bl2 = this.right != null && this.right.mouseReleased(d, e, i);
			return bl || bl2;
		}

		@Override
		public void renderOutOfBounds(float tickDelta) {
		}
	}
}
