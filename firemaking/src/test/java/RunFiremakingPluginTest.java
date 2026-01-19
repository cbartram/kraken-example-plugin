import com.krakenplugins.example.firemaking.FiremakingPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunFiremakingPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(FiremakingPlugin.class);
        RuneLite.main(args);
    }
}
