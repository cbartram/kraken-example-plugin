import com.krakenplugins.example.jewelry.JewelryPlugin;
import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RunJewelryPluginTest {
    public static void main(String[] args) throws Exception {
        ExternalPluginManager.loadBuiltin(JewelryPlugin.class);
        RuneLite.main(args);
    }
}
