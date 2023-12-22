package com.terraformersmc.modmenu.mixin;

import java.util.Arrays;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.terraformersmc.modmenu.util.TextRendererHelper;

import net.minecraft.SharedConstants;
import net.minecraft.client.render.TextRenderer;

@Mixin(TextRenderer.class)
public class MixinTextRenderer implements TextRendererHelper {

	@Shadow private int[] characterWidths;

	@Override
	public int getWidth(char chr) {
		if (chr == '\u00a7') {
			return -1;
		}
		int index = SharedConstants.VALID_CHAT_CHARACTERS.indexOf(chr);
		if (index >= 0) {
			return this.characterWidths[index + 32];
		}
		return 0;
	}

	@Override
	public String trim(String text, int width) {
		return this.trim(text, width, false);
	}

	@Override
	public String trim(String text, int width, boolean inverse) {
		StringBuilder sb = new StringBuilder();
		int n = 0;
		int n2 = inverse ? text.length() - 1 : 0;
		int n3 = inverse ? -1 : 1;
		boolean bl = false;
		boolean bl2 = false;
		for (int i = n2; i >= 0 && i < text.length() && n < width; i += n3) {
			char c = text.charAt(i);
			int n4 = this.getWidth(c);
			if (bl) {
				bl = false;
				if (c == 'l' || c == 'L') {
					bl2 = true;
				} else if (c == 'r' || c == 'R') {
					bl2 = false;
				}
			} else if (n4 < 0) {
				bl = true;
			} else {
				n += n4;
				if (bl2) {
					++n;
				}
			}
			if (n > width)
				break;
			if (inverse) {
				sb.insert(0, c);
				continue;
			}
			sb.append(c);
		}
		return sb.toString();
	}

	@Override
	public List<String> split(String text, int width) {
		return Arrays.asList(this.insertLineBreaks(text, width).split("\n"));
	}

	@Override
	public String insertLineBreaks(String text, int width) {
		int n = this.indexAtWidth(text, width);
		if (text.length() <= n) {
			return text;
		}
		String string = text.substring(0, n);
		String string2 = TextRendererHelper.isolateFormatting(string) + text.substring(n + (text.charAt(n) == ' ' ? 1 : 0));
		return string + "\n" + this.insertLineBreaks(string2, width);
	}

	@Override
	public int indexAtWidth(String text, int width) {
		int n;
		int n2 = text.length();
		int n3 = 0;
		int n4 = -1;
		boolean bl = false;
		for (n = 0; n < n2; ++n) {
			char c = text.charAt(n);
			switch (c) {
			case '\u00a7': {
				char c2;
				if (n == n2)
					break;
				if ((c2 = text.charAt(++n)) == 'l' || c2 == 'L') {
					bl = true;
					break;
				}
				if (c2 != 'r' && c2 != 'R')
					break;
				bl = false;
				break;
			}
			case ' ': {
				n4 = n;
			}
			default: {
				n3 += this.getWidth(c);
				if (!bl)
					break;
				++n3;
			}
			}
			if (c == '\n') {
				n4 = ++n;
				break;
			}
			if (n3 > width)
				break;
		}
		if (n != n2 && n4 != -1 && n4 < n) {
			return n4;
		}
		return n;
	}
}
