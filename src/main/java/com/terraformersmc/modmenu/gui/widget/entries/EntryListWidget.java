package com.terraformersmc.modmenu.gui.widget.entries;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.vertex.BufferBuilder;

import com.terraformersmc.modmenu.util.GlUtil;
import com.terraformersmc.modmenu.util.ListWidgetHelper;
import com.terraformersmc.modmenu.util.MathUtil;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ListWidget;

public abstract class EntryListWidget extends ListWidget implements ListWidgetHelper {

	protected double scrollAmount;
	protected boolean scrolling;

	public EntryListWidget(Minecraft minecraft, int i, int j, int k, int l, int m) {
		super(minecraft, i, j, k, l, m);
	}

	@Override
	protected void entryClicked(int index, boolean doubleClick) {
	}

	@Override
	protected boolean isEntrySelected(int index) {
		return false;
	}

	@Override
	protected void renderBackground() {
	}

	@Override
	public void render(int mouseX, int mouseY, float tickDelta) {
		int n;
		int n2;
		int n3;
		int n4;
		int n5;
		this.mouseX = mouseX;
		this.mouseY = mouseY;
		this.renderBackground();
		int n6 = this.size();
		int n7 = this.getScrollbarPosition();
		int n8 = n7 + 6;
		if (mouseX > this.minX && mouseX < this.maxX && mouseY > this.minY && mouseY < this.maxY) {
			int n9;
			if (Mouse.isButtonDown(0) && this.isScrolling()) {
				if (this.mouseYStart == -1.0f) {
					n9 = 1;
					if (mouseY >= this.minY && mouseY <= this.maxY) {
						int n10 = this.width / 2 - this.getRowWidth() / 2;
						n5 = this.width / 2 + this.getRowWidth() / 2;
						n4 = mouseY - this.minY - this.headerHeight + (int) this.scrollAmount - 4;
						n3 = n4 / this.entryHeight;
						if (mouseX >= n10 && mouseX <= n5 && n3 >= 0 && n4 >= 0 && n3 < n6) {
							n2 = n3 == this.pos && MathUtil.getTime() - this.time < 250L ? 1 : 0;
							this.entryClicked(n3, n2 != 0);
							this.pos = n3;
							this.time = MathUtil.getTime();
						} else if (mouseX >= n10 && mouseX <= n5 && n4 < 0) {
							this.headerClicked(mouseX - n10, mouseY - this.minY + (int) this.scrollAmount - 4);
							n9 = 0;
						}
						if (mouseX >= n7 && mouseX <= n8) {
							this.scrollSpeedMultiplier = -1.0f;
							n2 = this.getMaxScroll();
							if (n2 < 1) {
								n2 = 1;
							}
							if ((n = (int) ((float) ((this.maxY - this.minY) * (this.maxY - this.minY))
									/ (float) this.getHeight())) < 32) {
								n = 32;
							}
							if (n > this.maxY - this.minY - 8) {
								n = this.maxY - this.minY - 8;
							}
							this.scrollSpeedMultiplier /= (float) (this.maxY - this.minY - n) / (float) n2;
						} else {
							this.scrollSpeedMultiplier = 1.0f;
						}
						this.mouseYStart = n9 != 0 ? (float) mouseY : -2.0f;
					} else {
						this.mouseYStart = -2.0f;
					}
				} else if (this.mouseYStart >= 0.0f) {
					super.scrollAmount = (float) (this.scrollAmount -= ((float) mouseY - this.mouseYStart) * this.scrollSpeedMultiplier);
					this.mouseYStart = mouseY;
				}
			} else {
				while (/*!this.minecraft.options.touchScreen &&*/ Mouse.next()) {
					n9 = Mouse.getEventDWheel();
					if (n9 != 0) {
						if (n9 > 0) {
							n9 = -1;
						} else if (n9 < 0) {
							n9 = 1;
						}
						super.scrollAmount = (float) (this.scrollAmount += (float) (n9 * this.entryHeight / 2));
					}
					this.minecraft.screen.handleMouse();
				}
				this.mouseYStart = -1.0f;
			}
		}
		this.capScrolling();
		GL11.glDisable(2896);
		GL11.glDisable(2912);
		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;
		this.minecraft.textureManager.bind(this.minecraft.textureManager.load("/gui/background.png"));
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		float f = 32.0f;
		bufferBuilder.start();
		bufferBuilder.color(0x202020);
		bufferBuilder.vertex(this.minX, this.maxY, 0.0, (float) this.minX / f,
				(float) (this.maxY + (int) this.scrollAmount) / f);
		bufferBuilder.vertex(this.maxX, this.maxY, 0.0, (float) this.maxX / f,
				(float) (this.maxY + (int) this.scrollAmount) / f);
		bufferBuilder.vertex(this.maxX, this.minY, 0.0, (float) this.maxX / f,
				(float) (this.minY + (int) this.scrollAmount) / f);
		bufferBuilder.vertex(this.minX, this.minY, 0.0, (float) this.minX / f,
				(float) (this.minY + (int) this.scrollAmount) / f);
		bufferBuilder.end();
		n5 = this.minX + (this.width / 2 - this.getRowWidth() / 2 + 2);
		n4 = this.minY + 4 - (int) this.scrollAmount;
		if (this.renderHeader) {
			this.renderHeader(n5, n4, bufferBuilder);
		}
		this.renderList(n5, n4, mouseX, mouseY);
		GL11.glDisable(2929);
		n3 = 4;
		this.renderHoleBackground(0, this.minY, 255, 255);
		this.renderHoleBackground(this.maxY, this.height, 255, 255);
		GL11.glEnable(3042);
		GlUtil.blendFuncSeparate(770, 771, 0, 1);
		GL11.glDisable(3008);
		GL11.glShadeModel(7425);
		GL11.glDisable(3553);
		bufferBuilder.start();
		bufferBuilder.color(0, 0);
		bufferBuilder.vertex(this.minX, this.minY + n3, 0.0, 0.0, 1.0);
		bufferBuilder.vertex(this.maxX, this.minY + n3, 0.0, 1.0, 1.0);
		bufferBuilder.color(0, 255);
		bufferBuilder.vertex(this.maxX, this.minY, 0.0, 1.0, 0.0);
		bufferBuilder.vertex(this.minX, this.minY, 0.0, 0.0, 0.0);
		bufferBuilder.end();
		bufferBuilder.start();
		bufferBuilder.color(0, 255);
		bufferBuilder.vertex(this.minX, this.maxY, 0.0, 0.0, 1.0);
		bufferBuilder.vertex(this.maxX, this.maxY, 0.0, 1.0, 1.0);
		bufferBuilder.color(0, 0);
		bufferBuilder.vertex(this.maxX, this.maxY - n3, 0.0, 1.0, 0.0);
		bufferBuilder.vertex(this.minX, this.maxY - n3, 0.0, 0.0, 0.0);
		bufferBuilder.end();
		n2 = this.getMaxScroll();
		if (n2 > 0) {
			int n11;
			n = (this.maxY - this.minY) * (this.maxY - this.minY) / this.getHeight();
			if (n < 32) {
				n = 32;
			}
			if (n > this.maxY - this.minY - 8) {
				n = this.maxY - this.minY - 8;
			}
			if ((n11 = (int) this.scrollAmount * (this.maxY - this.minY - n) / n2 + this.minY) < this.minY) {
				n11 = this.minY;
			}
			bufferBuilder.start();
			bufferBuilder.color(0, 255);
			bufferBuilder.vertex(n7, this.maxY, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(n8, this.maxY, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(n8, this.minY, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(n7, this.minY, 0.0, 0.0, 0.0);
			bufferBuilder.end();
			bufferBuilder.start();
			bufferBuilder.color(0x808080, 255);
			bufferBuilder.vertex(n7, n11 + n, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(n8, n11 + n, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(n8, n11, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(n7, n11, 0.0, 0.0, 0.0);
			bufferBuilder.end();
			bufferBuilder.start();
			bufferBuilder.color(0xC0C0C0, 255);
			bufferBuilder.vertex(n7, n11 + n - 1, 0.0, 0.0, 1.0);
			bufferBuilder.vertex(n8 - 1, n11 + n - 1, 0.0, 1.0, 1.0);
			bufferBuilder.vertex(n8 - 1, n11, 0.0, 1.0, 0.0);
			bufferBuilder.vertex(n7, n11, 0.0, 0.0, 0.0);
			bufferBuilder.end();
		}
		this.renderDecorations(mouseX, mouseY);
		GL11.glEnable(3553);
		GL11.glShadeModel(7424);
		GL11.glEnable(3008);
		GL11.glDisable(3042);
	}

	protected void renderList(int x, int y, int mouseX, int mouseY) {
		int size = this.size();
		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;
		for (int i = 0; i < size; ++i) {
			int entryY = y + i * this.entryHeight + this.headerHeight;
			int entryHeight = this.entryHeight - 4;
			if (entryY > this.maxY || entryY + entryHeight < this.minY)
				continue;
			if (this.renderSelectionHighlight && this.isEntrySelected(i)) {
				int n4 = this.minX + (this.width / 2 - this.getRowWidth() / 2);
				int n5 = this.minX + (this.width / 2 + this.getRowWidth() / 2);
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				GL11.glDisable(3553);
				bufferBuilder.start();
				bufferBuilder.color(0x808080);
				bufferBuilder.vertex(n4, entryY + entryHeight + 2, 0.0, 0.0, 1.0);
				bufferBuilder.vertex(n5, entryY + entryHeight + 2, 0.0, 1.0, 1.0);
				bufferBuilder.vertex(n5, entryY - 2, 0.0, 1.0, 0.0);
				bufferBuilder.vertex(n4, entryY - 2, 0.0, 0.0, 0.0);
				bufferBuilder.color(0);
				bufferBuilder.vertex(n4 + 1, entryY + entryHeight + 1, 0.0, 0.0, 1.0);
				bufferBuilder.vertex(n5 - 1, entryY + entryHeight + 1, 0.0, 1.0, 1.0);
				bufferBuilder.vertex(n5 - 1, entryY - 1, 0.0, 1.0, 0.0);
				bufferBuilder.vertex(n4 + 1, entryY - 1, 0.0, 0.0, 0.0);
				bufferBuilder.end();
				GL11.glEnable(3553);
			}
			this.renderEntry(i, x, entryY, entryHeight, bufferBuilder);
		}
	}

	private void renderHoleBackground(int top, int bottom, int topAlpha, int bottomAlpha) {
		BufferBuilder bufferBuilder = BufferBuilder.INSTANCE;
		this.minecraft.textureManager.bind(this.minecraft.textureManager.load("/gui/background.png"));
		GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
		float f = 32.0f;
		bufferBuilder.start();
		bufferBuilder.color(0x404040, bottomAlpha);
		bufferBuilder.vertex(this.minX, bottom, 0.0, 0.0, (float) bottom / f);
		bufferBuilder.vertex(this.minX + this.width, bottom, 0.0, (float) this.width / f, (float) bottom / f);
		bufferBuilder.color(0x404040, topAlpha);
		bufferBuilder.vertex(this.minX + this.width, top, 0.0, (float) this.width / f, (float) top / f);
		bufferBuilder.vertex(this.minX, top, 0.0, 0.0, (float) top / f);
		bufferBuilder.end();
	}

	@Override
	protected void renderEntry(int index, int x, int y, int entryHeight, BufferBuilder bufferBuilder) {
		this.getEntry(index).render(index, x, y, this.getRowWidth(), entryHeight, bufferBuilder, mouseX, mouseY, this.getEntryAt(mouseX, mouseY) == index);
	}

	public boolean mouseClicked(int mouseX, int mouseY, int button) {
		int n;
		if (this.isMouseInList(mouseY) && (n = this.getEntryAt(mouseX, mouseY)) >= 0) {
			int n2 = this.minX + (this.width / 2 - this.getRowWidth() / 2 + 2);
			int n3 = this.minY + 4 - this.getScrollAmount() + (n * this.entryHeight + this.headerHeight);
			int n4 = mouseX - n2;
			int n5 = mouseY - n3;
			if (this.getEntry(n).mouseClicked(n, mouseX, mouseY, button, n4, n5)) {
				this.setScrolling(false);
				return true;
			}
		}
		return false;
	}

	public boolean mouseReleased(int mouseX, int mouseY, int button) {
		for (int i = 0; i < this.size(); ++i) {
			int n = this.minX + (this.width / 2 - this.getRowWidth() / 2 + 2);
			int n2 = this.minY + 4 - this.getScrollAmount() + (i * this.entryHeight + this.headerHeight);
			int n3 = mouseX - n;
			int n4 = mouseY - n2;
			this.getEntry(i).mouseReleased(i, mouseX, mouseY, button, n3, n4);
		}
		this.setScrolling(true);
		return false;
	}

	public abstract Entry getEntry(int var1);

	public int getScrollAmount() {
		return (int) this.scrollAmount;
	}

	protected void scroll(int i) {
		this.setScrollAmount(this.scrollAmount + (double)i);
		this.mouseYStart = -2.0F;
	}

	public void setScrollAmount(double amount) {
		if (amount < 0) {
			amount = 0;
		}
		if (amount > getMaxScroll()) {
			amount = getMaxScroll();
		}
		super.scrollAmount = (float) (this.scrollAmount = amount);
	}

	@Override
	public void doCapScrolling() {
		int max = this.getHeight() - (this.maxY - this.minY - 4);
		if (max < 0) {
			max /= 2;
		}
		if (this.scrollAmount < 0.0F) {
			super.scrollAmount = (float) (this.scrollAmount = 0.0F);
		}
		if (this.scrollAmount > max) {
			super.scrollAmount = (float) (this.scrollAmount = max);
		}
	}

	protected int getMaxScroll() {
		return Math.max(0, this.getHeight() - (this.maxY - this.minY - 4));
	}

	public boolean isMouseInList(int mouseY) {
		return mouseY >= this.minY && mouseY <= this.maxY;
	}

	public void setScrolling(boolean scrolling) {
		this.scrolling = scrolling;
	}

	public boolean isScrolling() {
		return this.scrolling;
	}

	protected int getScrollbarPosition() {
		return this.width / 2 + 124;
	}

	public void setX(int x) {
		this.minX = x;
		this.maxX = x + this.width;
	}

	public int getRowWidth() {
		return 220;
	}

	public static interface Entry {

		void render(int var1, int var2, int var3, int var4, int var5, BufferBuilder var6, int var7, int var8, boolean var9);

		boolean mouseClicked(int var1, int var2, int var3, int var4, int var5, int var6);

		void mouseReleased(int var1, int var2, int var3, int var4, int var5, int var6);

	}
}
