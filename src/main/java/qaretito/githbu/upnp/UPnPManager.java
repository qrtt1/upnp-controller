package qaretito.githbu.upnp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXParseException;

import qaretito.githbu.upnp.ssdp.HttpUtils;
import qaretito.githbu.upnp.ssdp.ServiceDiscoveryPacket;
import qaretito.githbu.upnp.xml.INode;
import qaretito.githbu.upnp.xml.XmlTree;

public class UPnPManager {
    
    private static Log logger = LogFactory.getLog(UPnPManager.class);
    private Map<String, UPnPModel> models = new HashMap<String, UPnPModel>();

    static abstract class UPnPModel {
        private INode tree;
        private String identify;
        
        public UPnPModel(INode node) {
            this.tree = node;
        }

        @Override
        public boolean equals(Object other) {
            if (other == null) {
                return false;
            }
            if (other instanceof UPnPModel) {
                UPnPModel obj = (UPnPModel) other;
                if (obj.identify == null) {
                    return false;
                }
                return obj.identify.equals(identify);
            }
            return super.equals(other);
        }

        @Override
        public int hashCode() {
            if (identify != null) {
                return identify.hashCode();
            }
            return super.hashCode();
        }

        public INode getTree() {
            return tree;
        }

        public void setTree(INode tree) {
            this.tree = tree;
        }

        public String getIdentify() {
            return identify;
        }

        public void setIdentify(String identify) {
            this.identify = identify;
        }
        
    }

    static class Service extends UPnPModel {
        private INode serviceSpecXml;
        private String controlUrl;
        public Service(INode node, String httpServer) throws UPnPException {
            super(node);
            setIdentify(node.find("serviceType").get(0).getText());
            String data = null;
            try {
                
                String scpdUrl = node.find("SCPDURL").get(0).getText();
                if (!scpdUrl.startsWith("http")) {
                    scpdUrl = httpServer + "/" + scpdUrl;
                }
                logger.info("SCPDURL: " + scpdUrl);
                data = HttpUtils.getData(scpdUrl);
                serviceSpecXml = XmlTree.makeTree(data);
                controlUrl = httpServer + ""
                        + node.find("controlURL").get(0).getText();
            } catch (SAXParseException e) {
                logger.error(e.getMessage());
            } catch (Exception e) {
                throw new UPnPException(e);
            }
        }
        
        @Override
        public String toString() {
            return "" + serviceSpecXml;
        }
        
        protected String requestBody(String serviceType, String service, String arguments) {
            return "<?xml version=\"1.0\"?>"
                    + "<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\" "
                    + "s:encodingStyle=\"http://schemas.xmlsoap.org/soap/encoding/\">"
                    + "<s:Body><u:%{Service} xmlns:u=\"%{ServiceType}\">%{Arguments}</u:%{Service}></s:Body></s:Envelope>"
                    .replaceAll("%\\{ServiceType\\}", serviceType)
                    .replaceAll("%\\{Arguments\\}", arguments)
                    .replaceAll("%\\{Service\\}", service);
        }

        public Map<String, String> prepareInvokeData(String action, Map<String, String> args) throws UPnPException {
            INode invoker = null;
            for (INode actionNode : serviceSpecXml.find("action")) {
                try {
                    final String actionName = actionNode.find("name").get(0).getText();
                    if(action.equals(actionName)){
                        invoker = actionNode;
                    }
                } catch (Exception ignored) {
                }
            }
            
            if(invoker == null){
                throw new UPnPException(String.format(
                        "cannot find action[%s] in service[%s]", 
                        action, getIdentify()));
            }
            
            StringBuilder arguments = new StringBuilder();
            for (Entry<String, String> entry : args.entrySet()) {
                arguments.append(
                        String.format("<%s>%s</%s>", 
                                entry.getKey(),
                                entry.getValue(), 
                                entry.getKey()));
            }
            
            Map<String, String> requestData = new HashMap<String, String>();
            requestData.put("SOAPACTION", "\"" + getIdentify() + "#" + action
                    + "\"");
            requestData.put("CONTENT-TYPE", "text/xml; charset=\"utf-8\"");
            requestData.put("body", requestBody(getIdentify(), action, arguments.toString()));

            return requestData;
        }

        public String getControlUrl() {
            return controlUrl;
        }
    }

    public void createService(ServiceDiscoveryPacket packet) throws UPnPException {
        if (models.containsKey(packet.getIdentify())) {
            logger.info("service[" + packet.getIdentify() + "] is created.");
            return;
        }
        if(packet.isMSearch()){
            logger.warn("cannot create service from M-Search packet");
            return ;
        }
        if (packet.isNotify()) {
            logger.warn("not implemented yet.");
        }
        if(packet.isResponsed()){
            try {
                logger.info("create service for: " + packet.getIdentify());
                buildService(packet.getHttpServer(), HttpUtils.getData(packet.getLocation()));
            } catch (Exception e) {
                throw new UPnPException("failed to build services from location: " 
                        + packet.getLocation(), e);
            }
        }
        
    }
    
    protected void buildService(String httpServer, String rootDeviceDescription)
            throws Exception {

        INode root = XmlTree.makeTree(rootDeviceDescription);
        for (INode deviceNode : root.find("device")) {
            for (INode serviceNode : deviceNode.find("service")) {
                try {
                    Service service = new Service(serviceNode, httpServer);
                    if (!models.containsValue(service)) {
                        models.put(service.getIdentify(), service);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

    }

    public Object invoke(String service, String action, Map<String, String> args) {
        for (String key : models.keySet()) {
            if (key.contains(service)) {
                if (models.get(key) instanceof Service) {
                    Service serviceInstance = (Service) models.get(key);
                    logger.info("found: " + serviceInstance.getIdentify());
                    try {
                        Map<String, String> request = serviceInstance.prepareInvokeData(action, args);
                        final String body = request.remove("body");
                        Object result = HttpUtils.post(serviceInstance.getControlUrl(), request, body);
                        logger.info("result:" + result);
                        return result;
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        }
        logger.info("no invoked action.");
        return null;
    }

}
