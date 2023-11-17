package com.terraformersmc.modmenu.gui;

import com.google.common.base.Joiner;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.config.ModMenuConfigManager;
import com.terraformersmc.modmenu.gui.widget.Controller;
import com.terraformersmc.modmenu.gui.widget.DescriptionListWidget;
import com.terraformersmc.modmenu.gui.widget.ModListWidget;
import com.terraformersmc.modmenu.gui.widget.TextFieldAccess;
import com.terraformersmc.modmenu.gui.widget.TexturedButtonWidget;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.OsUtil;
import com.terraformersmc.modmenu.util.ScreenUtil;
import com.terraformersmc.modmenu.util.TranslationUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.ModBadgeRenderer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiElement;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.screen.ResultListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.resource.Identifier;
import net.minecraft.text.Formatting;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

public class ModsScreen extends Screen implements ResultListener, Controller {
	private static final Identifier FILTERS_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/filters_button.png");
	private static final Identifier CONFIGURE_BUTTON_LOCATION = new Identifier(ModMenu.MOD_ID, "textures/gui/configure_button.png");

	private static final int SEARCH_BOX = 0;
	private static final int DESCRIPTION_LIST = 1;
	private static final int WEBSITE = 2;
	private static final int ISSUES = 3;
	private static final int FILTERS = 4;
	private static final int SORTING = 5;
	private static final int LIBRARIES = 6;
	private static final int MODS_FOLDER = 7;
	private static final int DONE = 8;
	private static final Text TOGGLE_FILTER_OPTIONS = new TranslatableText("modmenu.toggleFilterOptions");
	private static final Text CONFIGURE = new TranslatableText("modmenu.configure");
	private static final Logger LOGGER = LogManager.getLogger("Mod Menu | ModsScreen");
	private TextFieldWidget searchBox;
	private DescriptionListWidget descriptionListWidget;
	private final Text title;
	private final Screen previousScreen;
	private ModListWidget modList;
	private ModListEntry selected;
	private ModBadgeRenderer modBadgeRenderer;
	private float scrollPercent = 0;
	private boolean init = false;
	private boolean filterOptionsShown = false;
	private int paneY;
	private static final int RIGHT_PANE_Y = 48;
	private int paneWidth;
	private int rightPaneX;
	private int searchBoxX;
	private int filtersX;
	private int filtersWidth;
	private int searchRowWidth;
	public final Set<String> showModChildren = new HashSet<>();
	private int mouseX;
	private int mouseY;
	private List<String> tooltip;

	public final Map<String, Boolean> modHasConfigScreen = new HashMap<>();
	public final Map<String, Throwable> modScreenErrors = new HashMap<>();

	public ModsScreen(Screen previousScreen) {
		this.title = new TranslatableText("modmenu.title");
		this.previousScreen = previousScreen;
	}

	@Override
	public void setValue(String value) {
		modList.filter(value, false);
	}

	@Override
	public void handleMouse() {
		super.handleMouse();
		if (modList.isMouseInList(mouseX, mouseY)) {
//			this.modList.handleMouse();
		} else if (descriptionListWidget.isMouseInList(mouseX, mouseY)) {
//			this.descriptionListWidget.handleMouse();
		}
	}

	@Override
	public void tick() {
		this.searchBox.tick();
	}

	@Override
	public void init() {
		paneY = ModMenuConfig.CONFIG_MODE.getValue() ? 48 : 48 + 19;
		paneWidth = this.width / 2 - 8;
		rightPaneX = width - paneWidth;

		int filtersButtonSize = (ModMenuConfig.CONFIG_MODE.getValue() ? 0 : 22);
		int searchWidthMax = paneWidth - 32 - filtersButtonSize;
		int searchBoxWidth = ModMenuConfig.CONFIG_MODE.getValue() ? Math.min(200, searchWidthMax) : searchWidthMax;
		searchBoxX = paneWidth / 2 - searchBoxWidth / 2 - filtersButtonSize / 2;
		this.searchBox = new TextFieldWidget(this.textRenderer, searchBoxX, 22, searchBoxWidth, 20);
		((TextFieldAccess) this.searchBox).setController(this);

		for (Mod mod : ModMenu.MODS.values()) {
			String id = mod.getId();
			if (!modHasConfigScreen.containsKey(id)) {
				try {
					Screen configScreen = ModMenu.getConfigScreen(id, this);
					modHasConfigScreen.put(id, configScreen != null);
				} catch (java.lang.NoClassDefFoundError e) {
					LOGGER.warn("The '" + id + "' mod config screen is not available because " + e.getLocalizedMessage() + " is missing.");
					modScreenErrors.put(id, e);
					modHasConfigScreen.put(id, false);
				} catch (Throwable e) {
					LOGGER.error("Error from mod '" + id + "'", e);
					modScreenErrors.put(id, e);
					modHasConfigScreen.put(id, false);
				}
			}
		}

		this.modList = new ModListWidget(this.minecraft, paneWidth, this.height, paneY, this.height - 36, ModMenuConfig.COMPACT_LIST.getValue() ? 23 : 36, this.searchBox.getText(), this.modList, this);
		this.modList.setX(0);
		modList.reloadFilters();

		this.descriptionListWidget = new DescriptionListWidget(this.minecraft, paneWidth, this.height, RIGHT_PANE_Y + 60, this.height - 36, textRenderer.fontHeight + 1, this);
		this.descriptionListWidget.setX(rightPaneX);
		ButtonWidget configureButton = new TexturedButtonWidget(DESCRIPTION_LIST, width - 24, RIGHT_PANE_Y, 20, 20, 0, 0, 20, CONFIGURE_BUTTON_LOCATION, 32, 64) {
			private Text tooltip;
			@Override
			public void render(Minecraft minecraft, int mouseX, int mouseY) {
				String modId = selected.getMod().getId();
				if (selected != null) {
					active = modHasConfigScreen.get(modId);
				} else {
					active = false;
					visible = false;
				}
				visible = selected != null && modHasConfigScreen.get(modId) || modScreenErrors.containsKey(modId);
				if (modScreenErrors.containsKey(modId)) {
					Throwable e = modScreenErrors.get(modId);
					this.tooltip = new TranslatableText("modmenu.configure.error", modId, modId).copy().append("\n\n").append(e.toString()).setStyle(new Style().setColor(Formatting.RED));
				} else {
					this.tooltip = CONFIGURE;
				}
				super.render(minecraft, mouseX, mouseY);
			}

			@Override
			public void renderTooltip(int mouseX, int mouseY) {
				ModsScreen.this.renderTooltip(this.tooltip.getFormattedString(), mouseX, mouseY);
			}
		};
		int urlButtonWidths = paneWidth / 2 - 2;
		int cappedButtonWidth = Math.min(urlButtonWidths, 200);
		ButtonWidget websiteButton = new ButtonWidget(WEBSITE, rightPaneX + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36, Math.min(urlButtonWidths, 200), 20, I18n.translate("modmenu.website")) {
			@Override
			public void render(Minecraft minecraft, int mouseX, int mouseY) {
				visible = selected != null;
				active = visible && selected.getMod().getWebsite() != null;
				super.render(minecraft, mouseX, mouseY);
			}
		};
		ButtonWidget issuesButton = new ButtonWidget(ISSUES, rightPaneX + urlButtonWidths + 4 + (urlButtonWidths / 2) - (cappedButtonWidth / 2), RIGHT_PANE_Y + 36, Math.min(urlButtonWidths, 200), 20, I18n.translate("modmenu.issues")) {
			@Override
			public void render(Minecraft minecraft, int mouseX, int mouseY) {
				visible = selected != null;
				active = visible && selected.getMod().getIssueTracker() != null;
				super.render(minecraft, mouseX, mouseY);
			}
		};
		ButtonWidget filtersButton = new TexturedButtonWidget(FILTERS, paneWidth / 2 + searchBoxWidth / 2 - 20 / 2 + 2, 22, 20, 20, 0, 0, 20, FILTERS_BUTTON_LOCATION, 32, 64) {
			@Override
			public void renderTooltip(int mouseX, int mouseY) {
				ModsScreen.this.renderTooltip(TOGGLE_FILTER_OPTIONS.getFormattedString(), mouseX, mouseY);
			}
		};
		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			this.buttons.add(filtersButton);
		}
		String showLibrariesText = ModMenuConfig.SHOW_LIBRARIES.getValueLabel();
		String sortingText = ModMenuConfig.SORTING.getValueLabel();
		int showLibrariesWidth = textRenderer.getWidth(showLibrariesText) + 20;
		int sortingWidth = textRenderer.getWidth(sortingText) + 20;
		filtersWidth = showLibrariesWidth + sortingWidth + 2;
		searchRowWidth = searchBoxX + searchBoxWidth + 22;
		updateFiltersX();
		this.buttons.add(new ButtonWidget(SORTING, filtersX, 45, sortingWidth, 20, sortingText) {
			@Override
			public void render(Minecraft minecraft, int mouseX, int mouseY) {
				visible = filterOptionsShown;
				this.message = ModMenuConfig.SORTING.getValueLabel();
				super.render(minecraft, mouseX, mouseY);
			}
		});
		this.buttons.add(new ButtonWidget(LIBRARIES, filtersX + sortingWidth + 2, 45, showLibrariesWidth, 20, showLibrariesText) {
			@Override
			public void render(Minecraft minecraft, int mouseX, int mouseY) {
				visible = filterOptionsShown;
				this.message = ModMenuConfig.SHOW_LIBRARIES.getValueLabel();
				super.render(minecraft, mouseX, mouseY);
			}
		});
		if (!ModMenuConfig.HIDE_CONFIG_BUTTONS.getValue()) {
			this.buttons.add(configureButton);
		}
		this.buttons.add(websiteButton);
		this.buttons.add(issuesButton);
		this.buttons.add(new ButtonWidget(MODS_FOLDER, this.width / 2 - 154, this.height - 28, 150, 20, I18n.translate("modmenu.modsFolder")));
		this.buttons.add(new ButtonWidget(DONE, this.width / 2 + 4, this.height - 28, 150, 20, I18n.translate("gui.done")));
		this.searchBox.setFocused(true);

		init = true;
	}

	@Override
	public void buttonClicked(ButtonWidget button) {
		switch (button.id) {
		case DESCRIPTION_LIST: {
			final String id = Objects.requireNonNull(selected).getMod().getId();
			if (modHasConfigScreen.get(id)) {
				Screen configScreen = ModMenu.getConfigScreen(id, this);
				minecraft.openScreen(configScreen);
			} else {
				button.active = false;
			}
			break;
		}
		case WEBSITE: {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			minecraft.openScreen(new ConfirmChatLinkScreen(this, mod.getWebsite(), WEBSITE, false));
			break;
		}
		case ISSUES: {
			final Mod mod = Objects.requireNonNull(selected).getMod();
			minecraft.openScreen(new ConfirmChatLinkScreen(this, mod.getIssueTracker(), ISSUES, false));
			break;
		}
		case FILTERS: {
			filterOptionsShown = !filterOptionsShown;
			break;
		}
		case SORTING: {
			ModMenuConfig.SORTING.cycleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
			break;
		}
		case LIBRARIES: {
			ModMenuConfig.SHOW_LIBRARIES.toggleValue();
			ModMenuConfigManager.save();
			modList.reloadFilters();
			break;
		}
		case MODS_FOLDER: {
			OsUtil.openFolder(new File(FabricLoader.getInstance().getGameDir().toFile(), "mods"));
			break;
		}
		case DONE: {
			minecraft.openScreen(previousScreen);
			break;
		}
		}
	}

	@Override
	public void keyPressed(char chr, int key) {
		this.searchBox.keyPressed(chr, key);
		this.modList.reloadFilters();
		super.keyPressed(chr, key);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int button) {
		if (this.modList.isMouseInList(mouseX, mouseY)) {
			this.modList.mouseClicked(mouseX, mouseY, button);
		}
		if (this.descriptionListWidget.isMouseInList(mouseX, mouseY)) {
			this.descriptionListWidget.mouseClicked(mouseX, mouseY, button);
		}
		super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void render(int mouseX, int mouseY, float delta) {
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.tooltip = null;
		this.renderBackground();
		ModListEntry selectedEntry = selected;
		if (selectedEntry != null) {
			this.descriptionListWidget.render(mouseX, mouseY, delta);
		}
		this.modList.render(mouseX, mouseY, delta);
		this.searchBox.render();
		GL11.glDisable(GL11.GL_BLEND);
		this.drawCenteredString(this.textRenderer, this.title.getFormattedString(), this.modList.getWidth() / 2, 8, 16777215);
		if (!ModMenuConfig.CONFIG_MODE.getValue()) {
			Text fullModCount = computeModCountText(true);
			if (!ModMenuConfig.CONFIG_MODE.getValue() && updateFiltersX()) {
				if (filterOptionsShown) {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() || textRenderer.getWidth(fullModCount.getFormattedString()) <= filtersX - 5) {
						this.textRenderer.draw(fullModCount.getFormattedString(), searchBoxX, 52, 0xFFFFFF);
					} else {
						this.textRenderer.draw(computeModCountText(false).getFormattedString(), searchBoxX, 46, 0xFFFFFF);
						this.textRenderer.draw(computeLibraryCountText().getFormattedString(), searchBoxX, 57, 0xFFFFFF);
					}
				} else {
					if (!ModMenuConfig.SHOW_LIBRARIES.getValue() || textRenderer.getWidth(fullModCount.getFormattedString()) <= modList.getWidth() - 5) {
						this.textRenderer.draw(fullModCount.getFormattedString(), searchBoxX, 52, 0xFFFFFF);
					} else {
						this.textRenderer.draw(computeModCountText(false).getFormattedString(), searchBoxX, 46, 0xFFFFFF);
						this.textRenderer.draw(computeLibraryCountText().getFormattedString(), searchBoxX, 57, 0xFFFFFF);
					}
				}
			}
		}
		if (selectedEntry != null) {
			Mod mod = selectedEntry.getMod();
			int x = rightPaneX;
			if ("java".equals(mod.getId())) {
				DrawingUtil.drawRandomVersionBackground(mod, x, RIGHT_PANE_Y, 32, 32);
			}
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glEnable(GL11.GL_BLEND);
			this.minecraft.getTextureManager().bind(this.selected.getIconTexture());
			GuiElement.drawTexture(x, RIGHT_PANE_Y, 0.0F, 0.0F, 32, 32, 32, 32);
			GL11.glDisable(GL11.GL_BLEND);
			int lineSpacing = textRenderer.fontHeight + 1;
			int imageOffset = 36;
			Text name = new LiteralText(mod.getTranslatedName());
			Text trimmedName = name;
			int maxNameWidth = this.width - (x + imageOffset);
			if (textRenderer.getWidth(name.getFormattedString()) > maxNameWidth) {
				Text ellipsis = new LiteralText("...");
				trimmedName = new LiteralText("").append(textRenderer.trim(name.getFormattedString(), maxNameWidth - textRenderer.getWidth(ellipsis.getFormattedString()))).append(ellipsis);
			}
			this.textRenderer.draw(trimmedName.getFormattedString(), x + imageOffset, RIGHT_PANE_Y + 1, 0xFFFFFF);
			if (mouseX > x + imageOffset && mouseY > RIGHT_PANE_Y + 1 && mouseY < RIGHT_PANE_Y + 1 + textRenderer.fontHeight && mouseX < x + imageOffset + textRenderer.getWidth(trimmedName.getFormattedString())) {
				setTooltip(Arrays.asList(I18n.translate("modmenu.modIdToolTip", mod.getId())));
			}
			if (init || modBadgeRenderer == null || modBadgeRenderer.getMod() != mod) {
				modBadgeRenderer = new ModBadgeRenderer(x + imageOffset + this.minecraft.textRenderer.getWidth(trimmedName.getFormattedString()) + 2, RIGHT_PANE_Y, width - 28, selectedEntry.mod, this);
				init = false;
			}
			if (!ModMenuConfig.HIDE_BADGES.getValue()) {
				modBadgeRenderer.draw(mouseX, mouseY);
			}
			if (mod.isReal()) {
				this.textRenderer.draw(mod.getPrefixedVersion(), x + imageOffset, RIGHT_PANE_Y + 2 + lineSpacing, 0x808080);
			}
			String authors;
			List<String> names = mod.getAuthors();

			if (!names.isEmpty()) {
				if (names.size() > 1) {
					authors = Joiner.on(", ").join(names);
				} else {
					authors = names.get(0);
				}
				DrawingUtil.drawWrappedString(I18n.translate("modmenu.authorPrefix", authors), x + imageOffset, RIGHT_PANE_Y + 2 + lineSpacing * 2, paneWidth - imageOffset - 4, 1, 0x808080);
			}
		}
		super.render(mouseX, mouseY, delta);
		if (this.tooltip != null && !this.tooltip.isEmpty()) {
			this.renderTooltip(this.tooltip, mouseX, mouseY);
		}
	}

	private Text computeModCountText(boolean includeLibs) {
		int[] rootMods = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && !mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));

		if (includeLibs && ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingModsLibraries", rootMods, rootLibs);
		} else {
			return TranslationUtil.translateNumeric("modmenu.showingMods", rootMods);
		}
	}

	private Text computeLibraryCountText() {
		if (ModMenuConfig.SHOW_LIBRARIES.getValue()) {
			int[] rootLibs = formatModCount(ModMenu.ROOT_MODS.values().stream().filter(mod -> !mod.isHidden() && mod.getBadges().contains(Mod.Badge.LIBRARY)).map(Mod::getId).collect(Collectors.toSet()));
			return TranslationUtil.translateNumeric("modmenu.showingLibraries", rootLibs);
		} else {
			return new LiteralText(null);
		}
	}

	private int[] formatModCount(Set<String> set) {
		int visible = modList.getDisplayedCountFor(set);
		int total = set.size();
		if (visible == total) {
			return new int[]{total};
		}
		return new int[]{visible, total};
	}

	public void setTooltip(List<String> tooltip) {
		this.tooltip = tooltip;
	}

	public ModListEntry getSelectedEntry() {
		return selected;
	}

	public void updateSelectedEntry(ModListEntry entry) {
		if (entry != null) {
			this.selected = entry;
		}
	}

	public float getScrollPercent() {
		return scrollPercent;
	}

	public void updateScrollPercent(float scrollPercent) {
		this.scrollPercent = scrollPercent;
	}

	public String getSearchInput() {
		return searchBox.getText();
	}

	private boolean updateFiltersX() {
		if ((filtersWidth + textRenderer.getWidth(computeModCountText(true).getFormattedString()) + 20) >= searchRowWidth && ((filtersWidth + textRenderer.getWidth(computeModCountText(false).getFormattedString()) + 20) >= searchRowWidth || (filtersWidth + textRenderer.getWidth(computeLibraryCountText().getFormattedString()) + 20) >= searchRowWidth)) {
			filtersX = paneWidth / 2 - filtersWidth / 2;
			return !filterOptionsShown;
		} else {
			filtersX = searchRowWidth - filtersWidth + 1;
			return true;
		}
	}

	private static boolean isFabricMod(Path mod) {
		try (JarFile jarFile = new JarFile(mod.toFile())) {
			return jarFile.getEntry("fabric.mod.json") != null;
		} catch (IOException e) {
			return false;
		}
	}

	public Map<String, Boolean> getModHasConfigScreen() {
		return modHasConfigScreen;
	}

	@Override
	public void confirmResult(boolean result, int id) {
		super.confirmResult(result, id);
		if (result && this.selected != null) {
			switch (id) {
			case WEBSITE:
				ScreenUtil.openLink(this, this.selected.mod.getWebsite(), this.selected.mod.getId() + " /website");
				break;
			case ISSUES:
				ScreenUtil.openLink(this, this.selected.mod.getIssueTracker(), this.selected.mod.getId() + "/issues");
				break;
			}
		}

		this.minecraft.openScreen(this);
	}
}
