package dev.darkmoon;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class DarkmoonClient implements ClientModInitializer {
    private boolean fish, fly, xray, creative;

    @Override public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, access) -> dispatcher.register(literal("darkmoon")
            .executes(ctx -> { status(); return 1; })
            .then(literal("fish").executes(ctx -> { fish = !fish; message("Авторыбалка: " + state(fish) + " (в разработке)"); return 1; }))
            .then(literal("fly").executes(ctx -> { fly = !fly; message("Полёт: " + state(fly) + " (зависит от разрешений сервера)"); return 1; }))
            .then(literal("xray").executes(ctx -> { xray = !xray; message("X-Ray: " + state(xray) + " (в разработке)"); return 1; }))
            .then(literal("creative").executes(ctx -> { creative = !creative; message("Creative: " + state(creative) + " (серверные права не обходятся)"); return 1; }))));
    }
    private void status() { message("Настройки: /darkmoon fish | fly | xray | creative"); }
    private static String state(boolean b) { return b ? "ВКЛ" : "ВЫКЛ"; }
    private static void message(String s) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) client.player.displayClientMessage(Component.literal(s), false);
    }
}
