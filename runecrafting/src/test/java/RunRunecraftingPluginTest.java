import com.krakenplugins.autorunecrafting.AutoRunecraftingPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunRunecraftingPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(AutoRunecraftingPlugin.class);
        RuneLite.main(args);
    }
}
