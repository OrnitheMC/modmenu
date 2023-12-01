package com.terraformersmc.modmenu.util;

import org.lwjgl.opengl.ContextCapabilities;
import org.lwjgl.opengl.EXTBlendFuncSeparate;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GLContext;

public class GlUtil {

	private static boolean separateBlend;
	public static boolean useSeparateBlendExt;

	public static void init() {
		ContextCapabilities contextCapabilities = GLContext.getCapabilities();

		separateBlend = contextCapabilities.OpenGL14 || contextCapabilities.GL_EXT_blend_func_separate;
		useSeparateBlendExt = contextCapabilities.GL_EXT_blend_func_separate && !contextCapabilities.OpenGL14;
	}

	public static void blendFuncSeparate(int sfactorRGB, int dfactorRGB, int sfactorAlpha, int dfactorAlpha) {
		if (separateBlend) {
			if (useSeparateBlendExt) {
				EXTBlendFuncSeparate.glBlendFuncSeparateEXT(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
			} else {
				GL14.glBlendFuncSeparate(sfactorRGB, dfactorRGB, sfactorAlpha, dfactorAlpha);
			}
		} else {
			GL11.glBlendFunc(sfactorRGB, dfactorRGB);
		}
	}
}
