package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.EntryListWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.GlUtil;
import com.terraformersmc.modmenu.util.ScreenUtil;
import com.terraformersmc.modmenu.util.VersionUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.CreditsScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.*;
import net.minecraft.text.Formatting;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.opengl.GL11;

public class DescriptionListWidget extends EntryListWidget {

	private static final Text HAS_UPDATE_TEXT = Text.translatable("modmenu.hasUpdate");
	private static final Text EXPERIMENTAL_TEXT = Text.translatable("modmenu.experimental").setColor(Formatting.GOLD);
	private static final Text MODRINTH_TEXT = Text.translatable("modmenu.modrinth");
	private static final Text CHILD_HAS_UPDATE_TEXT = Text.translatable("modmenu.childHasUpdate");
	private static final Text LINKS_TEXT = Text.translatable("modmenu.links");
	private static final Text SOURCE_TEXT = Text.translatable("modmenu.source").setColor(Formatting.BLUE).setUnderlined(true);
	private static final Text LICENSE_TEXT = Text.translatable("modmenu.license");
	private static final Text VIEW_CREDITS_TEXT = Text.translatable("modmenu.viewCredits").setColor(Formatting.BLUE).setUnderlined(true);
	private static final Text CREDITS_TEXT = Text.translatable("modmenu.credits");

	private final Minecraft minecraft;
	private final ModsScreen parent;
	private final TextRenderer textRenderer;
	private final List<DescriptionEntry> entries = new ArrayList<>();
	private ModListEntry lastSelected = null;

	public DescriptionListWidget(Minecraft client, int width, int height, int top, int bottom, int entryHeight, ModsScreen parent) {
		super(client, width, height, top, bottom, entryHeight);
		this.minecraft = client;
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

	public boolean isMouseInList(int mouseX, int mouseY) {
		return mouseY >= this.minY && mouseY <= this.maxY && mouseX >= this.minX && mouseX <= this.maxX;
	}

	@Override
	public int size() {
		return this.entries.size();
	}

	public void clear() {
		this.entries.clear();
	}

	@Override
	public DescriptionEntry getEntry(int index) {
		return this.entries.get(index);
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
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
					for (Object line : textRenderer.split(description.replaceAll("\n", "\n\n"), wrapWidth)) {
						this.entries.add(new DescriptionEntry((String) line));
					}
				}

				if (ModMenuConfig.UPDATE_CHECKER.getValue() && !ModMenuConfig.DISABLE_UPDATE_CHECKER.getValue().contains(mod.getId())) {
					if (mod.getModrinthData() != null) {
						this.entries.add(emptyEntry);

						int index = 0;
						for (Object line : textRenderer.split(HAS_UPDATE_TEXT.buildString(true), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry((String) line);
							if (index == 0) entry.setUpdateTextEntry();

							this.entries.add(entry);
							index += 1;
						}

						for (Object line : textRenderer.split(EXPERIMENTAL_TEXT.buildString(true), wrapWidth - 16)) {
							this.entries.add(new DescriptionEntry((String) line, 8));
						}

						Text updateText = Text.translatable("modmenu.updateText", VersionUtil.stripPrefix(mod.getModrinthData().versionNumber()), MODRINTH_TEXT)
							.setColor(Formatting.BLUE).setUnderlined(true);

						String versionLink = String.format("https://modrinth.com/project/%s/version/%s", mod.getModrinthData().projectId(), mod.getModrinthData().versionId());

						for (Object line : textRenderer.split(updateText.buildString(true), wrapWidth - 16)) {
							this.entries.add(new LinkEntry((String) line, versionLink, 8));
						}
					}
					if (mod.getChildHasUpdate()) {
						this.entries.add(emptyEntry);

						int index = 0;
						for (Object line : textRenderer.split(CHILD_HAS_UPDATE_TEXT.buildString(true), wrapWidth - 11)) {
							DescriptionEntry entry = new DescriptionEntry((String) line);
							if (index == 0) entry.setUpdateTextEntry();

							this.entries.add(entry);
							index += 1;
						}
					}
				}

				Map<String, String> links = mod.getLinks();
				String sourceLink = mod.getSource();
				if ((!links.isEmpty() || sourceLink != null) && !ModMenuConfig.HIDE_MOD_LINKS.getValue()) {
					this.entries.add(emptyEntry);

					for (Object line : textRenderer.split(LINKS_TEXT.buildString(true), wrapWidth)) {
						this.entries.add(new DescriptionEntry((String) line));
					}

					if (sourceLink != null) {
						int indent = 8;
						for (Object line : textRenderer.split(SOURCE_TEXT.buildString(true), wrapWidth - 16)) {
							this.entries.add(new LinkEntry((String) line, sourceLink, indent));
							indent = 16;
						}
					}

					links.forEach((key, value) -> {
						int indent = 8;
						for (Object line : textRenderer.split(Text.translatable(key).setColor(Formatting.BLUE).setUnderlined(true).buildString(true), wrapWidth - 16)) {
							this.entries.add(new LinkEntry((String) line, value, indent));
							indent = 16;
						}
					});
				}

				Set<String> licenses = mod.getLicense();
				if (!ModMenuConfig.HIDE_MOD_LICENSE.getValue() && !licenses.isEmpty()) {
					this.entries.add(emptyEntry);

					for (Object line : textRenderer.split(LICENSE_TEXT.buildString(true), wrapWidth)) {
						this.entries.add(new DescriptionEntry((String) line));
					}

					for (String license : licenses) {
						int indent = 8;
						for (Object line : textRenderer.split(license, wrapWidth - 16)) {
							this.entries.add(new DescriptionEntry((String) line, indent));
							indent = 16;
						}
					}
				}

				if (!ModMenuConfig.HIDE_MOD_CREDITS.getValue()) {
					if ("minecraft".equals(mod.getId())) {
						this.entries.add(emptyEntry);

						for (Object line : textRenderer.split(VIEW_CREDITS_TEXT.buildString(true), wrapWidth)) {
							this.entries.add(new MojangCreditsEntry((String) line));
						}
					} else if (!"java".equals(mod.getId())) {
						List<String> credits = mod.getCredits();
						if (!credits.isEmpty()) {
							this.entries.add(emptyEntry);

							for (Object line : textRenderer.split(CREDITS_TEXT.buildString(true), wrapWidth)) {
								this.entries.add(new DescriptionEntry((String) line));
							}

							for (String credit : credits) {
								int indent = 8;
								for (Object line : textRenderer.split(credit, wrapWidth - 16)) {
									this.entries.add(new DescriptionEntry((String) line, indent));
									indent = 16;
								}
							}
						}
					}
				}
			}
		}

		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;

		{
			this.minecraft.getTextureManager().bind(Screen.BACKGROUND_LOCATION);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			bufferBuilder.start(GL11.GL_QUADS);
			bufferBuilder.color(0x20, 0x20, 0x20);
            bufferBuilder.vertex(this.minX, this.maxY, 0.0, (this.minX / 32.0F), ((this.maxY + this.scrollAmount) / 32.0F));
            bufferBuilder.vertex(this.maxX, this.maxY, 0.0, (this.maxX / 32.0F), ((this.maxY + this.scrollAmount) / 32.0F));
            bufferBuilder.vertex(this.maxX, this.minY, 0.0, (this.maxX / 32.0F), ((this.minY + this.scrollAmount) / 32.0F));
            bufferBuilder.vertex(this.minX, this.minY, 0.0, (this.minX / 32.0F), ((this.minY + this.scrollAmount) / 32.0F));
			bufferBuilder.end();
		}

		int listX = this.minX + this.width / 2 - this.getRowWidth() / 2 + 2;
		int listY = this.minY + 4 - (int)this.scrollAmount;
		this.renderList(listX, listY, mouseX, mouseY);

		GL11.glDepthFunc(GL11.GL_LEQUAL);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_BLEND);
		GlUtil.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ZERO, GL11.GL_ONE);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		GL11.glDisable(GL11.GL_TEXTURE_2D);

		bufferBuilder.start(GL11.GL_QUADS);
		bufferBuilder.color(0, 0, 0, 0);
		bufferBuilder.vertex(this.minX, this.minY + 4, 0.0, 0.0, 1.0);
		bufferBuilder.vertex(this.maxX, this.minY + 4, 0.0, 1.0, 1.0);
		bufferBuilder.color(0, 0, 0, 255);
		bufferBuilder.vertex(this.maxX, this.minY, 0.0, 1.0, 0.0);
		bufferBuilder.vertex(this.minX, this.minY, 0.0, 0.0, 0.0);
		bufferBuilder.end();
		bufferBuilder.start(GL11.GL_QUADS);
		bufferBuilder.color(0, 0, 0, 255);
		bufferBuilder.vertex(this.minX, this.maxY, 0.0, 0.0, 1.0);
		bufferBuilder.vertex(this.maxX, this.maxY, 0.0, 1.0, 1.0);
		bufferBuilder.color(0, 0, 0, 0);
		bufferBuilder.vertex(this.maxX, this.maxY - 4, 0.0, 1.0, 0.0);
		bufferBuilder.vertex(this.minX, this.maxY - 4, 0.0, 0.0, 0.0);
		bufferBuilder.end();

		this.renderScrollBar(bufferBuilder);

		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glShadeModel(GL11.GL_FLAT);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_BLEND);
	}

	@Override
	protected void renderEntry(int index, int x, int y, int height, BufferBuilder bufferBuilder) {
		if (y >= this.minY && y + height <= this.maxY) {
			super.renderEntry(index, x, y, height, bufferBuilder);
		}
	}

	public void renderScrollBar(BufferBuilder bufferBuilder) {
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

			bufferBuilder.start(GL11.GL_QUADS);
			bufferBuilder.color(0, 0, 0, 0xFF);
			bufferBuilder.vertex(scrollbarStartX, this.maxY, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX, this.maxY, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX, this.minY, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(scrollbarStartX, this.minY, 0.0, 0.0, 0.0);
			bufferBuilder.end();
			bufferBuilder.start(GL11.GL_QUADS);
			bufferBuilder.color(0x80, 0x80, 0x80, 0xFF);
			bufferBuilder.vertex(scrollbarStartX, q + p, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX, q + p, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX, q, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(scrollbarStartX, q, 0.0, 0.0, 0.0);
			bufferBuilder.end();
			bufferBuilder.start(GL11.GL_QUADS);
			bufferBuilder.color(0xC0, 0xC0, 0xC0, 0xFF);
			bufferBuilder.vertex(scrollbarStartX, q + p - 1, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX - 1, q + p - 1, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(scrollbarEndX - 1, q, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(scrollbarStartX, q, 0.0, 0.0, 0.0);
			bufferBuilder.end();
		}
	}

	public void confirmResult(boolean result, int id) {
		if (result) {
			int index = id - ModsScreen.MODS_LIST_CONFIRM_ID_OFFSET;
			List<DescriptionEntry> entries = this.entries;

			if (index >= 0 && index < entries.size()) {
				DescriptionEntry entry = entries.get(index);

				if (entry instanceof LinkEntry) {
					String link = ((LinkEntry) entry).link;
					ScreenUtil.openLink(parent, link, parent.getSelectedEntry().mod.getId() + "/link");
				}
			}
		}

		minecraft.openScreen(this.parent);
	}

	protected class DescriptionEntry implements EntryListWidget.Entry {
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
		public void render(int index, int x, int y, int width, int height, BufferBuilder bufferBuilder, int mouseX, int mouseY, boolean hovered) {
			if (updateTextEntry) {
				UpdateAvailableBadge.renderBadge(x + indent, y);
			}
			textRenderer.drawWithShadow(text, x + indent, y, 0xAAAAAA);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			return false;
		}

		@Override
		public void mouseReleased(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
		}
	}

	protected class MojangCreditsEntry extends DescriptionEntry {
		public MojangCreditsEntry(String text) {
			super(text);
		}

		@Override
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			if (isMouseInList(mouseX, mouseY)) {
				minecraft.openScreen(new CreditsScreen());
			}
			return super.mouseClicked(index, mouseX, mouseY, button, entryMouseX, entryMouseY);
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
		public boolean mouseClicked(int index, int mouseX, int mouseY, int button, int entryMouseX, int entryMouseY) {
			if (isMouseInList(mouseX, mouseY)) {
				minecraft.openScreen(new ConfirmChatLinkScreen(DescriptionListWidget.this.parent, link, ModsScreen.MODS_LIST_CONFIRM_ID_OFFSET + index, false));
			}
			return super.mouseClicked(index, mouseX, mouseY, button, entryMouseX, entryMouseY);
		}
	}

}
