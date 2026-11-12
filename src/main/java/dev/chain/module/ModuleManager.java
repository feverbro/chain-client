package dev.chain.module;

import dev.chain.module.impl.combat.AnchorAura;
import dev.chain.module.impl.combat.AutoTotem;
import dev.chain.module.impl.combat.CrystalAura;
import dev.chain.module.impl.combat.KillAura;
import dev.chain.module.impl.combat.TriggerBot;
import dev.chain.module.impl.misc.AutoRespawn;
import dev.chain.module.impl.misc.ClickGui;
import dev.chain.module.impl.movement.Flight;
import dev.chain.module.impl.movement.Speed;
import dev.chain.module.impl.movement.Sprint;
import dev.chain.module.impl.player.NoFall;
import dev.chain.module.impl.player.Scaffold;
import dev.chain.module.impl.render.ESP;
import dev.chain.module.impl.render.Fullbright;
import dev.chain.module.impl.render.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
    private final List<Module> modules = new ArrayList<>();
    public final Hud hud = new Hud();
    public final ClickGui clickGui = new ClickGui();

    public ModuleManager() {
        // Combat
        modules.add(new KillAura());
        modules.add(new TriggerBot());
        modules.add(new CrystalAura());
        modules.add(new AnchorAura());
        modules.add(new AutoTotem());
        // Movement
        modules.add(new Sprint());
        modules.add(new Flight());
        modules.add(new Speed());
        // Player
        modules.add(new NoFall());
        modules.add(new Scaffold());
        // Render
        modules.add(new ESP());
        modules.add(new Fullbright());
        modules.add(hud);
        // Misc
        modules.add(new AutoRespawn());
        modules.add(clickGui);

        hud.setEnabled(true);
    }

    public List<Module> all() {
        return modules;
    }

    public List<Module> byCategory(Category category) {
        List<Module> result = new ArrayList<>();
        for (Module m : modules) {
            if (m.category == category && !m.hidden) {
                result.add(m);
            }
        }
        return result;
    }

    /** Вызывается каждый клиентский тик. */
    public void tick(MinecraftClient mc) {
        long window = mc.getWindow().getHandle();
        boolean inWorld = mc.player != null && mc.world != null;

        for (Module m : modules) {
            // Горячие клавиши работают только без открытого экрана (чат, меню, ClickGUI)
            if (m.keyCode > 0) {
                boolean down = InputUtil.isKeyPressed(window, m.keyCode);
                if (down && !m.keyDown && mc.currentScreen == null && inWorld) {
                    m.toggle();
                }
                m.keyDown = down;
            }
            if (inWorld && m.isEnabled()) {
                m.onTick();
            }
        }
    }
}
