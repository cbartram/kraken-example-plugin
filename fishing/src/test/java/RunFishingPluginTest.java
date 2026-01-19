import com.krakenplugins.example.fishing.FishingPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunFishingPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(FishingPlugin.class);
        RuneLite.main(args);
    }
}
