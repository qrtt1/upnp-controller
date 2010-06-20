package qaretito.githbu.upnp.ssdp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import qaretito.githbu.upnp.UPnPException;

public class ServiceDiscoveryPacket {

    public static final String TYPE_NOTIFY = "NOTIFY * HTTP/1.1";
    public static final String TYPE_MSEARCH = "M-SEARCH * HTTP/1.1";
    public static final String TYPE_RESPONSE = "HTTP/1.1 200 OK";
    
    private static Pattern HTTP_HOST = 
        Pattern.compile("^(https?://[^/]+?)/.*$", Pattern.DOTALL);
    
    private static final Set<String> START_LINES = new HashSet<String>();

    static {
        START_LINES.add(TYPE_RESPONSE);
        START_LINES.add(TYPE_NOTIFY);
        START_LINES.add(TYPE_MSEARCH);
    }

    private static Log logger = LogFactory.getLog(ServiceDiscoveryPacket.class);
    private String startLine;
    private Map<String, String> headers = new HashMap<String, String>();

    public ServiceDiscoveryPacket(String packet) throws UPnPException {
        try {
            List<String> lines = new ArrayList<String>(Arrays.asList(packet
                    .split("[\r\n]+")));
            parseStartLine(lines);
            parseHeaders(lines);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new UPnPException(e);
        }
    }

    private void parseHeaders(List<String> lines) {
        for (String line : lines) {
            try {
                int sep = line.indexOf(":");
                if (sep == -1) {
                    continue;
                }
                headers.put(line.substring(0, sep).trim().toUpperCase(), line
                        .substring(sep + 1).trim());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

    }

    private void parseStartLine(List<String> lines) throws UPnPException {
        startLine = lines.remove(0).trim();
        if (!START_LINES.contains(startLine.toUpperCase())) {
            throw new UPnPException("Invalid start line: " + startLine);
        }
    }

    public String getHeader(String key) {
        if (key == null) {
            return null;
        }
        return headers.get(key.trim().toUpperCase());
    }

    public boolean isNotify() {
        return TYPE_NOTIFY.equals(startLine);
    }

    public boolean isMSearch() {
        return TYPE_MSEARCH.equals(startLine);
    }

    public boolean isResponsed() {
        return TYPE_RESPONSE.equals(startLine);
    }

    public String getIdentify() {
        return getHeader("st");
    }
    
    public String getLocation(){
        return getHeader("location");
    }
    
    @Override
    public String toString() {
        return String.format("%s :: %s", startLine, headers);
    }

    public String getHttpServer() {
        String httpServer = getLocation();
        Matcher m = HTTP_HOST.matcher(httpServer);
        if(m.matches()){
            httpServer = m.group(1);
            logger.info("http host: " + httpServer);
        }
        return httpServer;
    }
}
