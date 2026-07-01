package com.terraformersmc.modmenu.gui.widget;

import com.terraformersmc.modmenu.api.UpdateInfo;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.ConfirmationListener;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.*;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.vertex.BufferBuilder;
import net.minecraft.client.render.vertex.DefaultVertexFormat;
import net.minecraft.client.render.vertex.Tesselator;
import net.minecraft.util.Utils;
import net.minecraft.util.math.MathHelper;
import net.ornithemc.osl.text.api.Formatting;
import net.ornithemc.osl.text.api.TextComponent;
import net.ornithemc.osl.text.api.TextComponents;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.lwjgl.opengl.GL11;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> implements ConfirmationListener {

	private static final TextComponent HAS_UPDATE_TEXT = TextComponents.translatable("modmenu.hasUpdate");
	private static final TextComponent EXPERIMENTAL_TEXT = TextComponents.translatable("modmenu.experimental").format(Formatting.GOLD);
	private static final TextComponent DOWNLOAD_TEXT = TextComponents.translatable("modmenu.downloadLink").format(Formatting.BLUE, Formatting.UNDERLINED);
	private static final TextComponent CHILD_HAS_UPDATE_TEXT = TextComponents.translatable("modmenu.childHasUpdate");
	private static final TextComponent LINKS_TEXT = TextComponents.translatable("modmenu.links");
	private static final TextComponent SOURCE_TEXT = TextComponents.translatable("modmenu.source").format(Formatting.BLUE, Formatting.UNDERLINED);
	private static final TextComponent LICENSE_TEXT = TextComponents.translatable("modmenu.license");
	private static final TextComponent VIEW_CREDITS_TEXT = TextComponents.translatable("modmenu.viewCredits").format(Formatting.BLUE, Formatting.UNDERLINED);
	private static final TextComponent CREDITS_TEXT = TextComponents.translatable("modmenu.credits");

	private final ModsScreen parent;
	private final TextRenderer textRenderer;
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(Minecraft client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent) {
		super(client, width, height, top, bottom, entryHeight);
		this.parent = parent;
		this.textRenderer = client.textRenderer;
	}

	@Override
	public int getRowWidth() {
		return this.width - 10;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6 + this.minX;
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		capScrolling();
		ModListEntry selectedEntry = parent.getSelectedEntry();
		if (selectedEntry != lastSelected) {
			lastSelected = selectedEntry;
			clear();
			// Prevent text jumping around
			// scroll(-Integer.MAX_VALUE);
			if (lastSelected != null) {
				DescriptionEntry emptyEntry = new DescriptionEntry("");
				int wrapWidth = getRowWidth() - 5;

				Mod mod = lastSelected.getMod();
				String description = mod.getTranslatedDescription();
				if (!description.isEmpty()) {
					for (String line : textRenderer.split(description.replaceAll("\n", "\n\n"), wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}
				}

				if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(mod.getId())) {
					UpdateInfo updateInfo = mod.getUpdateInfo();
					if (updateInfo != null && updateInfo.isUpdateAvailable()) {
						children().add(emptyEntry);

						int index = 0;
						for (String line : textRenderer.split(HAS_UPDATE_TEXT.buildFormattedString(), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry(line);
							if (index == 0) entry.setUpdateTextEntry();

							children().add(entry);
							index += 1;
						}

						for (String line : textRenderer.split(EXPERIMENTAL_TEXT.buildFormattedString(), wrapWidth - 16)) {
							children().add(new DescriptionEntry(line, 8));
						}

						TextComponent updateMessage = updateInfo.getUpdateMessage();
						String downloadLink = updateInfo.getDownloadLink();
						if (updateMessage == null) {
							updateMessage = DOWNLOAD_TEXT;
						} else {
							if (downloadLink != null) {
								updateMessage = updateMessage.copy().format(Formatting.BLUE, Formatting.UNDERLINED);
							}
						}
						for (String line : textRenderer.split(updateMessage.buildFormattedString(), wrapWidth - 16)) {
							if (downloadLink != null) {
								children().add(new LinkEntry(line, downloadLink, 8));
							} else {
								children().add(new DescriptionEntry(line, 8));
							}
						}
					}
					if (mod.getChildHasUpdate()) {
						children().add(emptyEntry);

						int index = 0;
						for (String line : textRenderer.split(CHILD_HAS_UPDATE_TEXT.buildFormattedString(), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry(line);
							if (index == 0) entry.setUpdateTextEntry();

							children().add(entry);
							index += 1;
						}
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
					children().add(emptyEntry);

					for (String line : textRenderer.split(LINKS_TEXT.buildFormattedString(), wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					if (sourceLink != null) {
						int indent = 8;
						for (String line : textRenderer.split(SOURCE_TEXT.buildFormattedString(), wrapWidth - 16)) {
							children().add(new LinkEntry(line, sourceLink, indent));
							indent = 16;
						}
					}

					links.forEach((key, value) -> {
						int indent = 8;
						for (String line : textRenderer.split(TextComponents.translatable(key).format(Formatting.BLUE, Formatting.UNDERLINED).buildFormattedString(), wrapWidth - 16)) {
							children().add(new LinkEntry(line, value, indent));
							indent = 16;
						}
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					children().add(emptyEntry);

					for (String line : textRenderer.split(LICENSE_TEXT.buildFormattedString(), wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					for (String license : licenses) {
						int indent = 8;
						for (String line : textRenderer.split(license, wrapWidth - 16)) {
							children().add(new DescriptionEntry(line, indent));
							indent = 16;
						}
					}
				}

				if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						children().add(emptyEntry);

						for (String line : textRenderer.split(VIEW_CREDITS_TEXT.buildFormattedString(), wrapWidth)) {
							children().add(new MojangCreditsEntry(line));
						}
					} else if (!"java".equals(mod.getId())) {
						SortedMap<String, Set<String>> credits = mod.getCredits();

						if (!credits.isEmpty()) {
							children().add(emptyEntry);

							for (String line : textRenderer.split(CREDITS_TEXT.buildFormattedString(), wrapWidth)) {
								children().add(new DescriptionEntry(line));
							}

							Iterator<Map.Entry<String, Set<String>>> iterator = credits.entrySet().iterator();

							while (iterator.hasNext()) {
								int indent = 8;

								Map.Entry<String, Set<String>> role = iterator.next();
								String roleName = role.getKey();

								for (String line : textRenderer.split(this.creditsRoleText(roleName).buildFormattedString(), wrapWidth - 16)) {
									children().add(new DescriptionEntry(line, indent));
									indent = 16;
								}

								for (String contributor : role.getValue()) {
									indent = 16;

									for (String line : textRenderer.split(TextComponents.literal(contributor).buildFormattedString(), wrapWidth - 24)) {
										children().add(new DescriptionEntry(line, indent));
										indent = 24;
									}
								}

								if (iterator.hasNext()) {
									children().add(emptyEntry);
								}
							}
						}
					}
				}
			}
		}

		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuffer();

		{
			this.minecraft.getTextureManager().bind(Screen.BACKGROUND_LOCATION);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(this.minX, this.maxY, 0.0D).texture(this.minX / 32.0F, (this.maxY + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).nextVertex();
			bufferBuilder.vertex(this.maxX, this.maxY, 0.0D).texture(this.maxX / 32.0F, (this.maxY + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).nextVertex();
			bufferBuilder.vertex(this.maxX, this.minY, 0.0D).texture(this.maxX / 32.0F, (this.minY + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).nextVertex();
			bufferBuilder.vertex(this.minX, this.minY, 0.0D).texture(this.minX / 32.0F, (this.minY + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).nextVertex();
			tesselator.end();
		}

		int listX = this.minX + this.width / 2 - this.getRowWidth() / 2 + 2;
		int listY = this.minY + 4 - (int)this.scrollAmount;
		this.renderList(listX, listY, mouseX, mouseY, delta);

		GlStateManager.depthFunc(GL11.GL_LEQUAL);
		GlStateManager.disableDepthTest();
		GlStateManager.enableBlend();
		GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		GlStateManager.disableAlphaTest();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableTexture();

		bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferBuilder.vertex(this.minX, (this.minY + 4), 0.0D).

				color(0, 0, 0, 0).

				nextVertex();
		bufferBuilder.vertex(this.maxX, (this.minY + 4), 0.0D).

				color(0, 0, 0, 0).

				nextVertex();
		bufferBuilder.vertex(this.maxX, this.minY, 0.0D).

				color(0, 0, 0, 255).

				nextVertex();
		bufferBuilder.vertex(this.minX, this.minY, 0.0D).

				color(0, 0, 0, 255).

				nextVertex();
		bufferBuilder.vertex(this.minX, this.maxY, 0.0D).

				color(0, 0, 0, 255).

				nextVertex();
		bufferBuilder.vertex(this.maxX, this.maxY, 0.0D).

				color(0, 0, 0, 255).

				nextVertex();
		bufferBuilder.vertex(this.maxX, (this.maxY - 4), 0.0D).

				color(0, 0, 0, 0).

				nextVertex();
		bufferBuilder.vertex(this.minX, (this.maxY - 4), 0.0D).

				color(0, 0, 0, 0).

				nextVertex();
		tesselator.end();

		this.renderScrollBar(bufferBuilder, tesselator);

		GlStateManager.enableTexture();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.enableAlphaTest();
		GlStateManager.disableBlend();
	}

	@Override
	protected void renderEntry(int index, int x, int y, int height, int mouseX, int mouseY, float tickDelta) {
		if (y >= this.minY && y + height <= this.maxY) {
			super.renderEntry(index, x, y, height, mouseX, mouseY, tickDelta);
		}
	}

	public void renderScrollBar(BufferBuilder bufferBuilder, Tesselator tesselator) {
		int scrollbarStartX = this.getScrollbarPosition();
		int scrollbarEndX = scrollbarStartX + 6;
		int maxScroll = this.getMaxScroll();
		if (maxScroll > 0) {
			int p = (int) ((float) ((this.maxY - this.minY) * (this.maxY - this.minY)) / (float) this.getMaxScroll());
			p = MathHelper.clamp(p, 32, this.maxY - this.minY - 8);
			int q = (int) this.getScrollAmount() * (this.maxY - this.minY - p) / maxScroll + this.minY;
			if (q < this.minY) {
				q = this.minY;
			}

			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_COLOR);
			bufferBuilder.vertex(scrollbarStartX, this.maxY, 0.0D).color(0, 0, 0, 255).nextVertex();
			bufferBuilder.vertex(scrollbarEndX, this.maxY, 0.0D).color(0, 0, 0, 255).nextVertex();
			bufferBuilder.vertex(scrollbarEndX, this.minY, 0.0D).color(0, 0, 0, 255).nextVertex();
			bufferBuilder.vertex(scrollbarStartX, this.minY, 0.0D).color(0, 0, 0, 255).nextVertex();
			bufferBuilder.vertex(scrollbarStartX, q + p, 0.0D).color(128, 128, 128, 255).nextVertex();
			bufferBuilder.vertex(scrollbarEndX, q + p, 0.0D).color(128, 128, 128, 255).nextVertex();
			bufferBuilder.vertex(scrollbarEndX, q, 0.0D).color(128, 128, 128, 255).nextVertex();
			bufferBuilder.vertex(scrollbarStartX, q, 0.0D).color(128, 128, 128, 255).nextVertex();
			bufferBuilder.vertex(scrollbarStartX, q + p - 1, 0.0D).color(192, 192, 192, 255).nextVertex();
			bufferBuilder.vertex(scrollbarEndX - 1, q + p - 1, 0.0D).color(192, 192, 192, 255).nextVertex();
			bufferBuilder.vertex(scrollbarEndX - 1, q, 0.0D).color(192, 192, 192, 255).nextVertex();
			bufferBuilder.vertex(scrollbarStartX, q, 0.0D).color(192, 192, 192, 255).nextVertex();
			tesselator.end();
		}
	}

	@Override
	public void confirmResult(boolean result, int id) {
		if (result) {
			List<DescriptionEntry> entries = this.children();

			if (id >= 0 && id < entries.size()) {
				DescriptionEntry entry = entries.get(id);

				if (entry instanceof LinkEntry) {
					String link = ((LinkEntry) entry).link;
					Utils.getOS().openUri(link);
				}
			}
		}

		minecraft.openScreen(this.parent);
	}

	private TextComponent creditsRoleText(String roleName) {
		// Replace spaces and dashes in role names with underscores if they exist
		// Notably Quilted Fabric API does this with FabricMC as "Upstream Owner"
		String translationKey = roleName.replaceAll("[\\s-]", "_").toLowerCase();

		return TextComponents.translatable("modmenu.credits.role." + translationKey).append(TextComponents.literal(":"));
	}

	protected class DescriptionEntry extends EntryListWidget.Entry<DescriptionEntry> {
		protected String text;
		protected int indent;
		public boolean updateTextEntry = false;

		public DescriptionEntry(String text, int indent) {
			this.text = text;
			this.indent = indent;
		}

		public DescriptionEntry(String text) {
			this(text, 0);
		}

		public DescriptionEntry setUpdateTextEntry() {
			this.updateTextEntry = true;
			return this;
		}

		@Override
		public void render(int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
			int x = getX();
			int y = getY();
			if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(x + indent, y);
				x+=11;
			}
			textRenderer.drawWithShadow(text, x + indent, y, 0xAAAAAA);
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(String text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseInList(mouseX, mouseY)) {
				minecraft.openScreen(new MinecraftCredits());
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}

		class MinecraftCredits extends CreditsScreen {
			public MinecraftCredits() {
				super(false, () -> { });
			}
		}
	}

	protected class LinkEntry extends DescriptionEntry {
		private final String link;

		public LinkEntry(String text, String link, int indent) {
			super(text, indent);
			this.link = link;
		}

		public LinkEntry(String text, String link) {
			this(text, link, 0);
		}

		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int button) {
			if (isMouseInList(mouseX, mouseY)) {
				minecraft.openScreen(new ConfirmChatLinkScreen(DescriptionListWidget.this, link, index, false));
			}
			return super.mouseClicked(mouseX, mouseY, button);
		}
	}

}
