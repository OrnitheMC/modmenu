package com.terraformersmc.modmenu.event;

import com.terraformersmc.modmenu.gui.ModsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.options.KeyBinding;
//import net.ornithemc.osl.keybinds.api.KeyBindingEvents;
import net.ornithemc.osl.lifecycle.api.MinecraftEvents;
import org.lwjgl.input.Keyboard;

public class ModMenuEventHandler {
	private static KeyBinding MENU_KEY_BIND;

	public static void register() {
		/*KeyBindingEvents.REGISTER_KEYBINDS.register(registry -> MENU_KEY_BIND = registry.register(
				"key.modmenu.open_menu",
				Keyboard.KEY_NONE
		));*/
		MinecraftEvents.TICK_END.register(ModMenuEventHandler::onClientEndTick);
	}

	private static void onClientEndTick(Minecraft client) {
		/*while (MENU_KEY_BIND.consumeClick()) {
			client.openScreen(new ModsScreen(client.screen));
		}*/
	}

	public static void shiftButtons(ButtonWidget button, boolean shiftUp, int spacing) {
		if (shiftUp) {
			button.y -= spacing / 2;
		} else {
			button.y += spacing / 2;
		}
	}
}
