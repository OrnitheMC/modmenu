package com.terraformersmc.modmenu.gui.widget;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tessellator;
import com.terraformersmc.modmenu.ModMenu;
import com.terraformersmc.modmenu.config.ModMenuConfig;
import com.terraformersmc.modmenu.gui.ModsScreen;
import com.terraformersmc.modmenu.gui.widget.entries.ChildEntry;
import com.terraformersmc.modmenu.gui.widget.entries.IndependentEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ModListEntry;
import com.terraformersmc.modmenu.gui.widget.entries.ParentEntry;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import com.terraformersmc.modmenu.util.mod.ModSearch;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.util.math.MathHelper;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.stream.Collectors;

public class ModListWidget extends EntryListWidget implements AutoCloseable {
	public static final boolean DEBUG = Boolean.getBoolean("modmenu.debug");
	private final ModsScreen parent;
	private final List<ModListEntry> entries = new ArrayList<>();
	private List<Mod> mods = null;
	private final Set<Mod> addedMods = new HashSet<>();
	private String selectedModId = null;
	private boolean scrolling;
	private final FabricIconHandler iconHandler = new FabricIconHandler();
	private ModListEntry selected;

	public ModListWidget(Minecraft client, int width, int height, int y1, int y2, int entryHeight, String searchTerm, ModListWidget list, ModsScreen parent) {
		super(client, width, height, y1, y2, entryHeight);
		this.parent = parent;
		if (list != null) {
			this.mods = list.mods;
		}
		this.filter(searchTerm, false);
		this.scrollAmount = parent.getScrollPercent() * Math.max(0, this.getMaxScroll() - (this.maxY - this.minY - 4));
		this.capScrolling();
	}

	@Override
	public void scroll(int amount) {
		super.scroll(amount);
		int denominator = Math.max(0, this.getMaxScroll() - (this.maxY - this.minY - 4));
		if (denominator <= 0) {
			this.parent.updateScrollPercent(0);
		} else {
			this.parent.updateScrollPercent(getScrollAmount() / Math.max(0, this.getMaxScroll() - (this.maxY - this.minY - 4)));
		}
	}

	public boolean isMouseInList(int mouseX, int mouseY) {
		return mouseY >= this.minY && mouseY <= this.maxY && mouseX >= this.minX && mouseX <= this.maxX;
	}

	protected boolean isFocused() {
		return false; //return parent.getFocused() == this;
	}

	public void select(ModListEntry entry) {
		this.setSelected(entry);
	}

	public void setSelected(ModListEntry entry) {
		selected = entry;
		selectedModId = entry.getMod().getId();
		parent.updateSelectedEntry(entry);
	}

	@Override
	protected boolean isEntrySelected(int index) {
		return selected != null && selected.getMod().getId().equals(this.entries.get(index).getMod().getId());
	}

	public void addEntry(ModListEntry entry) {
		if (addedMods.contains(entry.mod)) {
			return;
		}
		addedMods.add(entry.mod);
		this.entries.add(entry);
		if (entry.getMod().getId().equals(selectedModId)) {
			setSelected(entry);
		}
		return;
	}

	protected boolean removeEntry(ModListEntry entry) {
		addedMods.remove(entry.mod);
		return this.entries.remove(entry);
	}

	protected ModListEntry remove(int index) {
		addedMods.remove(this.entries.get(index).mod);
		return this.entries.remove(index);
	}

	@Override
	public int size() {
		return this.entries.size();
	}

	@Override
	public ModListEntry getEntry(int index) {
		return this.entries.get(index);
	}

	public void clear() {
		this.entries.clear();
	}

	public void reloadFilters() {
		filter(parent.getSearchInput(), true, false);
	}


	public void filter(String searchTerm, boolean refresh) {
		filter(searchTerm, refresh, true);
	}

	private boolean hasVisibleChildMods(Mod parent) {
		List<Mod> children = ModMenu.PARENT_MAP.get(parent);
		boolean hideLibraries = !ModMenuConfig.SHOW_LIBRARIES.getValue();

		return !children.stream().allMatch(child -> child.isHidden() || hideLibraries && child.getBadges().contains(Mod.Badge.LIBRARY));
	}

	private void filter(String searchTerm, boolean refresh, boolean search) {
		this.clear();
		addedMods.clear();
		Collection<Mod> mods = ModMenu.MODS.values().stream().filter(mod -> {
			if (ModMenuConfig.CONFIG_MODE.getValue()) {
				Map<String, Boolean> modHasConfigScreen = parent.getModHasConfigScreen();
				Boolean hasConfig = modHasConfigScreen.get(mod.getId());
				if (!hasConfig) {
					return false;
				}
			}

			return !mod.isHidden();
		}).collect(Collectors.toSet());

		if (DEBUG) {
			mods = new ArrayList<>(mods);
//			mods.addAll(TestModContainer.getTestModContainers());
		}

		if (this.mods == null || refresh) {
			this.mods = new ArrayList<>();
			this.mods.addAll(mods);
			this.mods.sort(ModMenuConfig.SORTING.getValue().getComparator());
		}

		List<Mod> matched = ModSearch.search(parent, searchTerm, this.mods);

		for (Mod mod : matched) {
			String modId = mod.getId();

			//Hide parent lib mods when the config is set to hide
			if (mod.getBadges().contains(Mod.Badge.LIBRARY) && !ModMenuConfig.SHOW_LIBRARIES.getValue()) {
				continue;
			}

			if (!ModMenu.PARENT_MAP.values().contains(mod)) {
				if (ModMenu.PARENT_MAP.keySet().contains(mod) && hasVisibleChildMods(mod)) {
					//Add parent mods when not searching
					List<Mod> children = ModMenu.PARENT_MAP.get(mod);
					children.sort(ModMenuConfig.SORTING.getValue().getComparator());
					ParentEntry parent = new ParentEntry(mod, children, this);
					this.addEntry(parent);
					//Add children if they are meant to be shown
					if (this.parent.showModChildren.contains(modId)) {
						List<Mod> validChildren = ModSearch.search(this.parent, searchTerm, children);
						for (Mod child : validChildren) {
							this.addEntry(new ChildEntry(child, parent, this, validChildren.indexOf(child) == validChildren.size() - 1));
						}
					}
				} else {
					//A mod with no children
					this.addEntry(new IndependentEntry(mod, this));
				}
			}
		}

		if (parent.getSelectedEntry() != null && !this.entries.isEmpty() || this.selected != null && this.selected.getMod() != parent.getSelectedEntry().getMod()) {
			for (ModListEntry entry : this.entries) {
				if (entry.getMod().equals(parent.getSelectedEntry().getMod())) {
					setSelected(entry);
				}
			}
		} else {
			if (this.selected == null && !this.entries.isEmpty() && this.entries.get(0) != null) {
				setSelected(this.entries.get(0));
			}
		}

		if (getScrollAmount() > Math.max(0, this.getMaxScroll() - (this.maxY - this.minY - 4))) {
			scroll(Math.max(0, this.getMaxScroll() - (this.maxY - this.minY - 4)));
		}
	}


	@Override
	protected void renderList(int x, int y, int mouseX, int mouseY) {
		int entryCount = this.size();
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuilder();

		for (int index = 0; index < entryCount; ++index) {
			int entryTop = this.getRowTop(index);
			int entryBottom = this.getRowTop(index) + this.entryHeight;
			if (entryBottom >= this.minY && entryTop <= this.maxY) {
				int entryHeight = this.entryHeight - 4;
				ModListEntry entry = this.entries.get(index);
				int rowWidth = this.getRowWidth();
				int entryLeft;
				if (this.isEntrySelected(index)) {
					entryLeft = getRowLeft() - 2 + entry.getXOffset();
					int selectionRight = this.getRowLeft() + rowWidth + 2;
					float float_2 = this.isFocused() ? 1.0F : 0.5F;
					GlStateManager.disableTexture();
					GlStateManager.color4f(float_2, float_2, float_2, 1.0F);
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION);
					buffer.vertex(entryLeft, entryTop + entryHeight + 2, 0.0F).nextVertex();
					buffer.vertex(selectionRight, entryTop + entryHeight + 2, 0.0F).nextVertex();
					buffer.vertex(selectionRight, entryTop - 2, 0.0F).nextVertex();
					buffer.vertex(entryLeft, entryTop - 2, 0.0F).nextVertex();
					tessellator.end();
					GlStateManager.color4f(0.0F, 0.0F, 0.0F, 1.0F);
					buffer.begin(GL11.GL_QUADS, DefaultVertexFormat.POSITION);
					buffer.vertex(entryLeft + 1, entryTop + entryHeight + 1, 0.0F).nextVertex();
					buffer.vertex(selectionRight - 1, entryTop + entryHeight + 1, 0.0F).nextVertex();
					buffer.vertex(selectionRight - 1, entryTop - 1, 0.0F).nextVertex();
					buffer.vertex(entryLeft + 1, entryTop - 1, 0.0F).nextVertex();
					tessellator.end();
					GlStateManager.enableTexture();
				}

				this.renderEntry(index, this.getRowLeft(), entryTop, entryHeight, mouseX, mouseY);
			}
		}
	}

	protected void updateScrollingState(double double_1, double double_2, int int_1) {
		this.scrolling = int_1 == 0 && double_1 >= (double) this.getScrollbarPosition() && double_1 < (double) (this.getScrollbarPosition() + 6);
	}

	@Override
	public boolean mouseClicked(int double_1, int double_2, int int_1) {
		this.updateScrollingState(double_1, double_2, int_1);
		if (!this.isMouseInList(double_1, double_2)) {
			return false;
		} else {
			ModListEntry entry = this.getEntryAtPos(double_1, double_2);
			if (entry != null) {
				if (entry.mouseClicked(entries.indexOf(entry), double_1, double_2, int_1, double_1, double_2)) {
					this.select(entry);
					return true;
				}
			} else if (int_1 == 0) {
				this.render((int) (double_1 - (double) (this.minX + this.width / 2 - this.getRowWidth() / 2)), (int) (double_2 - (double) this.minY) + (int) this.getScrollAmount() - 4);
				return true;
			}

			return this.scrolling;
		}
	}

	public boolean keyPressed(char chr, int key) {
		if (this.selected != null) {
			int index = this.entries.indexOf(this.selected);
			if (key == Keyboard.KEY_UP) {
				if (--index < 0) {
					index = this.size() - 1;
				}
				this.select(this.entries.get(index));
				return true;
			}
			if (key == Keyboard.KEY_DOWN) {
				if (++index >= this.size()) {
					index = 0;
				}
				this.select(this.entries.get(index));
				return true;
			}
			return this.selected.keyPressed(chr, key);
		}
		return false;
	}

	public final ModListEntry getEntryAtPos(double x, double y) {
		int int_5 = MathHelper.floor(y - (double) this.minY) - this.headerHeight + (int) this.getScrollAmount() - 4;
		int index = int_5 / this.entryHeight;
		return x < (double) this.getScrollbarPosition() && x >= (double) getRowLeft() && x <= (double) (getRowLeft() + getRowWidth()) && index >= 0 && int_5 >= 0 && index < this.size() ? this.entries.get(index) : null;
	}

	@Override
	protected int getScrollbarPosition() {
		return this.width - 6;
	}

	@Override
	public int getRowWidth() {
		return this.width - (Math.max(0, this.getMaxScroll() - (this.maxY - this.minY - 4)) > 0 ? 18 : 12);
	}

	public int getRowLeft() {
		return minX + 6;
	}

	public int getRowTop(int index) {
		return this.minY + 4 - this.getScrollAmount() + index * this.entryHeight + this.headerHeight;
	}

	public int getWidth() {
		return width;
	}

	public int getTop() {
		return this.minY;
	}

	public ModsScreen getParent() {
		return parent;
	}

	@Override
	public int getMaxScroll() {
		return super.getMaxScroll() + 4;
	}

	public int getDisplayedCountFor(Set<String> set) {
		int count = 0;
		for (ModListEntry c : this.entries) {
			if (set.contains(c.getMod().getId())) {
				count++;
			}
		}
		return count;
	}

	@Override
	public void close() {
	}

	public FabricIconHandler getFabricIconHandler() {
		return iconHandler;
	}
}
