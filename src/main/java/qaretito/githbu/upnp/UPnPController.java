package qaretito.githbu.upnp;

import java.util.Map;

public interface UPnPController {

    public void joinUPnPNetwork() throws Exception;

    public void leaveUPnPNetwork() throws Exception;

    public void notifySearch() throws Exception;
    
    public Object invoke(String service, String action, Map<String, String> args);

}
