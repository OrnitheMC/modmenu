package com.terraformersmc.modmenu.gui.widget.entries;

import com.mojang.blaze3d.platform.GlStateManager;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.UpdateAvailableBadge;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.TextRenderer;
import net.minecraft.client.render.texture.DynamicTexture;
import net.minecraft.resource.Identifier;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Utils;

public class ModListEntry extends EntryListWidget.Entry<ModListEntry> {
	public static final Identifier UNKNOWN_ICON = new Identifier("textures/misc/unknown_pack.png");
	private static final Identifier MOD_CONFIGURATION_ICON = new Identifier("modmenu", "textures/gui/mod_configuration.png");
	private static final Identifier ERROR_ICON = new Identifier("minecraft", "textures/gui/world_selection.png");

	protected final Minecraft client;
	public final Mod mod;
	protected final ModListWidget list;
	protected Identifier iconLocation;
	protected static final int FULL_ICON_SIZE = 32;
	protected static final int COMPACT_ICON_SIZE = 19;
	protected long sinceLastClick;

	public ModListEntry(Mod mod, ModListWidget list) {
		this.mod = mod;
		this.list = list;
		this.client = Minecraft.getInstance();
	}

	@Override
	public void render(int rowWidth, int rowHeight, int mouseX, int mouseY, boolean hovered, float delta) {
		int x = getX() + getXOffset();
		int y = getY();
		rowWidth -= getXOffset();
		int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
		String modId = mod.getId();
		if ("java".equals(modId)) {
			DrawingUtil.drawRandomVersionBackground(mod, x, y, iconSize, iconSize);
		}
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		this.client.getTextureManager().bind(this.getIconTexture());
		GuiElement.drawTexture(x, y, 0.0F, 0.0F, iconSize, iconSize, iconSize, iconSize);
		GlStateManager.disableBlend();
		Text name = new LiteralText(mod.getTranslatedName());
		Text trimmedName = name;
		int maxNameWidth = rowWidth - iconSize - 3;
		TextRenderer font = this.client.textRenderer;
		if (font.getWidth(name.getFormattedString()) > maxNameWidth) {
			Text ellipsis = new LiteralText("...");
			trimmedName = new LiteralText("").append(font.trimToWidth(name.getFormattedString(), maxNameWidth - font.getWidth(ellipsis.getFormattedString()))).append(ellipsis);
		}
		font.draw(trimmedName.getFormattedString(), x + iconSize + 3, y + 1, 0xFFFFFF);
		int updateBadgeXOffset = 0;
		if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(modId) && (mod.getModrinthData() != null || mod.getChildHasUpdate())) {
			UpdateAvailableBadge.renderBadge(x + iconSize + 3 + font.getWidth(name.getFormattedString()) + 2, y);
			updateBadgeXOffset = 11;
		}
		if (!ModMenuConfig.HIDE_BADGES.getValue()) {
			new ModBadgeRenderer(x + iconSize + 3 + font.getWidth(name.getFormattedString()) + 2 + updateBadgeXOffset, y, x + rowWidth, mod, list.getParent()).draw(mouseX, mouseY);
		}
		if (!ModMenuConfig.COMPACT_LIST.getValue()) {
			String summary = mod.getSummary();
			DrawingUtil.drawWrappedString(summary, (x + iconSize + 3 + 4), (y + client.textRenderer.fontHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		} else {
			DrawingUtil.drawWrappedString(mod.getPrefixedVersion(), (x + iconSize + 3), (y + client.textRenderer.fontHeight + 2), rowWidth - iconSize - 7, 2, 0x808080);
		}

		if (!(this instanceof ParentEntry) && ModMenuConfig.QUICK_CONFIGURE.getValue() && (this.list.getParent().getModHasConfigScreen().get(modId) || this.list.getParent().modScreenErrors.containsKey(modId))) {
			final int textureSize = ModMenuConfig.COMPACT_LIST.getValue() ? (int) (256 / (FULL_ICON_SIZE / (double) COMPACT_ICON_SIZE)) : 256;
			if (this.client.options.touchscreen || hovered) {
				GuiElement.fill(x, y, x + iconSize, y + iconSize, -1601138544);
				boolean hoveringIcon = mouseX - x < iconSize;
				int v = hoveringIcon ? iconSize : 0;
				if (this.list.getParent().modScreenErrors.containsKey(modId)) {
					this.client.getTextureManager().bind(ERROR_ICON);
					GuiElement.drawTexture(x, y, 96.0F, (float) v, iconSize, iconSize, textureSize, textureSize);
					if (hoveringIcon) {
						Throwable e = this.list.getParent().modScreenErrors.get(modId);
						this.list.getParent().setTooltip(this.client.textRenderer.split(new TranslatableText("modmenu.configure.error", modId, modId).append("\n\n").append(e.toString()).setFormatting(Formatting.RED).getFormattedString(), 175));
					}
				} else {
					this.client.getTextureManager().bind(MOD_CONFIGURATION_ICON);
					GuiElement.drawTexture(x, y, 0.0F, (float) v, iconSize, iconSize, textureSize, textureSize);
				}
			}
		}
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int delta) {
		list.select(this);
		if (ModMenuConfig.QUICK_CONFIGURE.getValue() && this.list.getParent().getModHasConfigScreen().get(this.mod.getId())) {
			int iconSize = ModMenuConfig.COMPACT_LIST.getValue() ? COMPACT_ICON_SIZE : FULL_ICON_SIZE;
			if (mouseX - list.getRowLeft() <= iconSize) {
				this.openConfig();
			} else if (Utils.getTimeMillis() - this.sinceLastClick < 250) {
				this.openConfig();
			}
		}
		this.sinceLastClick = Utils.getTimeMillis();
		return true;
	}

	public void openConfig() {
		client.openScreen(ModMenu.getConfigScreen(mod.getId(), list.getParent()));
	}

	public Mod getMod() {
		return mod;
	}

	public Identifier getIconTexture() {
		if (this.iconLocation == null) {
			this.iconLocation = new Identifier(ModMenu.MOD_ID, mod.getId() + "_icon");
			DynamicTexture icon = mod.getIcon(list.getFabricIconHandler(), 64 * this.client.options.guiScale);
			if (icon != null) {
				this.client.getTextureManager().register(this.iconLocation, icon);
			} else {
				this.iconLocation = UNKNOWN_ICON;
			}
		}
		return iconLocation;
	}

	public int getXOffset() {
		return 0;
	}
}
