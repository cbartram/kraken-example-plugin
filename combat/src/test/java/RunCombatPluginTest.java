import com.krakenplugins.example.combat.CombatPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunCombatPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(CombatPlugin.class);
        RuneLite.main(args);
    }
}
