package qaretito.githbu.upnp.ssdp;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import junit.framework.TestCase;

public class ServiceDiscoveryPacketTest extends TestCase {

    public void testParse() throws Exception {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = 
            new BufferedReader(new InputStreamReader(
                ServiceDiscoveryPacketTest.class.getResourceAsStream("/packet_notify.txt")));
        String line = reader.readLine();
        while(line !=null){
            builder.append(line).append("\n");
            line = reader.readLine();
        }
        reader.close();
        ServiceDiscoveryPacket packet = new ServiceDiscoveryPacket(builder.toString());
        //
        // NOTIFY * HTTP/1.1
        assertTrue(packet.isNotify());
        // Host: 239.255.255.250:1900
        assertEquals("239.255.255.250:1900", packet.getHeader("host"));
        // Cache-Control: max-age=1800
        assertEquals("max-age=1800", packet.getHeader("cache-control"));
        // Location: http://192.168.0.125:60968/RootDevice0x8447f88.xml
        assertEquals("http://192.168.0.125:60968/RootDevice0x8447f88.xml",
                packet.getHeader("loCation"));
        // Server: Linux/2.6.33-ARCH UPnP/1.0 GUPnP/0.13.3
        assertEquals("Linux/2.6.33-ARCH UPnP/1.0 GUPnP/0.13.3", packet.getHeader("server"));
        // NTS: ssdp:alive
        assertEquals("ssdp:alive", packet.getHeader("NTS"));
        // NT: urn:schemas-upnp-org:service:Dimming:1
        assertEquals("urn:schemas-upnp-org:service:Dimming:1", packet.getHeader("nt"));
        // USN:
        // uuid:92ca7ddb-e9ea-4ef5-9ca2-c26b1e3b4337::urn:schemas-upnp-org:service:Dimming:1
        assertEquals(
                "uuid:92ca7ddb-e9ea-4ef5-9ca2-c26b1e3b4337::urn:schemas-upnp-org:service:Dimming:1", 
                packet.getHeader("usn"));
    }

}
