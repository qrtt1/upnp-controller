package qaretito.githbu.upnp;

import java.util.HashMap;
import java.util.Map;

public class AppMain {

    public static void main(String[] args) throws Exception {
        final UPnPController controller = new ConcreteUPnPController();
        controller.joinUPnPNetwork();
        controller.notifySearch();

        // wait for upnp service
        Thread.sleep(3000);
        
        Map<String, String> arguments = new HashMap<String, String>();
        arguments.put("NewTargetValue", "1");
        controller.invoke("SwitchPower", "SetTarget", arguments);
        controller.leaveUPnPNetwork();
        
    }
}
