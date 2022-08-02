package emanondev.itemtag.denizen;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DenizenUtils {

    public void runDenizenScript(CommandSender sender, String denizenScript) {
        List<Object> entries = new ArrayList<>();

        // format "<denizen script command>"
        String entry = "run " + denizenScript;

        entries.add(entry);
        InstantQueue queue = new InstantQueue("ITEMTAG-" + UUID.randomUUID());
        NPCTag npc = null;
        if (Depends.citizens != null && Depends.citizens.getNPCSelector().getSelected(sender) != null) {
            npc = new NPCTag(Depends.citizens.getNPCSelector().getSelected(sender));
        }
        List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                new BukkitScriptEntryData(sender instanceof Player ? new PlayerTag((Player) sender) : null, npc));
        queue.addEntries(scriptEntries);

        queue.start();
    }
}
