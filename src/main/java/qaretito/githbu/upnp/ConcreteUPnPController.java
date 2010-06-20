package qaretito.githbu.upnp;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import qaretito.githbu.upnp.ssdp.ServiceDiscoveryPacket;

public class ConcreteUPnPController implements UPnPController {

    private static Log logger = LogFactory.getLog(ConcreteUPnPController.class);
    private UPnPManager manager = new UPnPManager();
    private AbstractUPnPDiscoverer discoverer;

    private static class Discoverer extends AbstractUPnPDiscoverer {
        private UPnPManager manager;
        public Discoverer(UPnPManager manager) throws IOException {
            super(UPnPContrants.UPNP_MULTICAST_ADDRESS,
                    UPnPContrants.UPNP_MULTICAST_PORT);
            this.manager = manager;
        }

        @Override
        protected void onDiscoveryEvent(String packetData) throws Exception {
            ServiceDiscoveryPacket packet = new ServiceDiscoveryPacket(packetData);
            if (packet.isResponsed()) {
                manager.createService(packet);
            }
            if (packet.isNotify()) {
                // ssdp:byebye
            }
        }

    }

    @Override
    public void joinUPnPNetwork() throws Exception {
        if (discoverer == null) {
            discoverer = new Discoverer(manager);
            discoverer.start();
        }
    }

    @Override
    public void leaveUPnPNetwork() throws Exception {
        if (discoverer != null) {
            try {
                discoverer.stop();
            } catch (Exception ignored) {
            } finally {
                discoverer = null;
            }
        }
    }

    @Override
    public void notifySearch() throws Exception {
        if (discoverer != null) {
            discoverer.issueMSearch(true);
        }
    }

    @Override
    public Object invoke(String service, String action, Map<String, String> args) {
        logger.info(String.format("%s@%s%s", action, service, args));
        return manager.invoke(service, action, args);
    }

}
