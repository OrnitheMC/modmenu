package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.ConfirmationListener;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.client.render.*;
import net.minecraft.text.Formatting;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Utils;
import net.minecraft.util.math.MathHelper;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

public class DescriptionListWidget extends EntryListWidget<DescriptionListWidget.DescriptionEntry> implements ConfirmationListener {

	private static final Text HAS_UPDATE_TEXT = new TranslatableText("modmenu.hasUpdate");
	private static final Text EXPERIMENTAL_TEXT = new TranslatableText("modmenu.experimental").setFormatting(Formatting.GOLD);
	private static final Text MODRINTH_TEXT = new TranslatableText("modmenu.modrinth");
	private static final Text CHILD_HAS_UPDATE_TEXT = new TranslatableText("modmenu.childHasUpdate");
	private static final Text LINKS_TEXT = new TranslatableText("modmenu.links");
	private static final Text SOURCE_TEXT = new TranslatableText("modmenu.source").setFormatting(Formatting.BLUE).setFormatting(Formatting.UNDERLINE);
	private static final Text LICENSE_TEXT = new TranslatableText("modmenu.license");
	private static final Text VIEW_CREDITS_TEXT = new TranslatableText("modmenu.viewCredits").setFormatting(Formatting.BLUE).setFormatting(Formatting.UNDERLINE);
	private static final Text CREDITS_TEXT = new TranslatableText("modmenu.credits");

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
			scroll(-Integer.MAX_VALUE);
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
					if (mod.getModrinthData() != null) {
						children().add(emptyEntry);

						int index = 0;
						for (String line : textRenderer.split(HAS_UPDATE_TEXT.getFormattedString(), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry(line);
							if (index == 0) entry.setUpdateTextEntry();

							children().add(entry);
							index += 1;
						}

						for (String line : textRenderer.split(EXPERIMENTAL_TEXT.getFormattedString(), wrapWidth - 16)) {
							children().add(new DescriptionEntry(line, 8));
						}

						Text updateText = new TranslatableText("modmenu.updateText", VersionUtil.stripPrefix(mod.getModrinthData().versionNumber()), MODRINTH_TEXT)
							.setFormatting(Formatting.BLUE)
							.setFormatting(Formatting.UNDERLINE);

						String versionLink = String.format("https://modrinth.com/project/%s/version/%s", mod.getModrinthData().projectId(), mod.getModrinthData().versionId());

						for (String line : textRenderer.split(updateText.getFormattedString(), wrapWidth - 16)) {
							children().add(new LinkEntry(line, versionLink, 8));
						}
					}
					if (mod.getChildHasUpdate()) {
						children().add(emptyEntry);

						int index = 0;
						for (String line : textRenderer.split(CHILD_HAS_UPDATE_TEXT.getFormattedString(), wrapWidth - 11)) {
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

					for (String line : textRenderer.split(LINKS_TEXT.getFormattedString(), wrapWidth)) {
						children().add(new DescriptionEntry(line));
					}

					if (sourceLink != null) {
						int indent = 8;
						for (String line : textRenderer.split(SOURCE_TEXT.getFormattedString(), wrapWidth - 16)) {
							children().add(new LinkEntry(line, sourceLink, indent));
							indent = 16;
						}
					}

					links.forEach((key, value) -> {
						int indent = 8;
						for (String line : textRenderer.split(new TranslatableText(key).setFormatting(Formatting.BLUE).setFormatting(Formatting.UNDERLINE).getFormattedString(), wrapWidth - 16)) {
							children().add(new LinkEntry(line, value, indent));
							indent = 16;
						}
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					children().add(emptyEntry);

					for (String line : textRenderer.split(LICENSE_TEXT.getFormattedString(), wrapWidth)) {
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

						for (String line : textRenderer.split(VIEW_CREDITS_TEXT.getFormattedString(), wrapWidth)) {
							children().add(new MojangCreditsEntry(line));
						}
					} else if (!"java".equals(mod.getId())) {
						List<String> credits = mod.getCredits();
						if (!credits.isEmpty()) {
							children().add(emptyEntry);

							for (String line : textRenderer.split(CREDITS_TEXT.getFormattedString(), wrapWidth)) {
								children().add(new DescriptionEntry(line));
							}

							for (String credit : credits) {
								int indent = 8;
								for (String line : textRenderer.split(credit, wrapWidth - 16)) {
									children().add(new DescriptionEntry(line, indent));
									indent = 16;
								}
							}
						}
					}
				}
			}
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuilder();

		{
			this.minecraft.getTextureManager().bind(Screen.BACKGROUND_LOCATION);
			GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
			bufferBuilder.vertex(this.minX, this.maxY, 0.0D).texture(this.minX / 32.0F, (this.maxY + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).nextVertex();
			bufferBuilder.vertex(this.maxX, this.maxY, 0.0D).texture(this.maxX / 32.0F, (this.maxY + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).nextVertex();
			bufferBuilder.vertex(this.maxX, this.minY, 0.0D).texture(this.maxX / 32.0F, (this.minY + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).nextVertex();
			bufferBuilder.vertex(this.minX, this.minY, 0.0D).texture(this.minX / 32.0F, (this.minY + (int) this.getScrollAmount()) / 32.0F).color(32, 32, 32, 255).nextVertex();
			tessellator.end();
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
		tessellator.end();

		this.renderScrollBar(bufferBuilder, tessellator);

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

	public void renderScrollBar(BufferBuilder bufferBuilder, Tessellator tessellator) {
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
			tessellator.end();
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
