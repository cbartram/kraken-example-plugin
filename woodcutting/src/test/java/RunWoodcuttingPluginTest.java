import com.krakenplugins.example.woodcutting.WoodcuttingPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunWoodcuttingPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(WoodcuttingPlugin.class);
        RuneLite.main(args);
    }
}
