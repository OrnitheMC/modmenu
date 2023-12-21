package com.terraformersmc.modmenu.util;

import java.util.List;

public interface TextRendererHelper {

	default String trim(String text, int width) {
		throw new UnsupportedOperationException();
	}

	default String trim(String text, int width, boolean inverse) {
		throw new UnsupportedOperationException();
	}

	default List<String> split(String text, int width) {
		throw new UnsupportedOperationException();
	}

	default String insertLineBreaks(String text, int width) {
		throw new UnsupportedOperationException();
	}

	default int indexAtWidth(String text, int width) {
		throw new UnsupportedOperationException();
	}

	public static boolean isColor(char chr) {
		return chr >= '0' && chr <= '9' || chr >= 'a' && chr <= 'f' || chr >= 'A' && chr <= 'F';
	}

	public static boolean isFormatting(char chr) {
		return chr >= 'k' && chr <= 'o' || chr >= 'K' && chr <= 'O' || chr == 'r' || chr == 'R';
	}

	public static String isolateFormatting(String text) {
		String string = "";
		int n = -1;
		int n2 = text.length();
		while ((n = text.indexOf(167, n + 1)) != -1) {
			if (n >= n2 - 1)
				continue;
			char c = text.charAt(n + 1);
			if (TextRendererHelper.isColor(c)) {
				string = "\u00a7" + c;
				continue;
			}
			if (!TextRendererHelper.isFormatting(c))
				continue;
			string = string + "\u00a7" + c;
		}
		return string;
	}
}
