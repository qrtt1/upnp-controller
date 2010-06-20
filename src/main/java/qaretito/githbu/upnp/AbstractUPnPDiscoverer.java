package qaretito.githbu.upnp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class AbstractUPnPDiscoverer {

    private static Log logger = LogFactory.getLog(AbstractUPnPDiscoverer.class);
    //
    private MulticastSocket socket;
    private InetAddress group;
    //
    private Thread worker;
    private boolean stop = false;
    //
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private byte[] resetTemplate = new byte[1024];
    private DatagramPacket packet = new DatagramPacket(
            buffer.array(), buffer.capacity());
    //
    private static byte[] MSEARCH_ROOT_DEVICE_COMMAND = (
            "M-SEARCH * HTTP/1.1\r\n"
            + "HOST: 239.255.255.250:1900\r\n" 
            + "MAN: \"upnp:rootdevice\"\r\n"
            + "MX: 3\r\n" 
            + "ST: ssdp:all\r\n\r\n"
            ).getBytes();
    
    private static byte[] MSEARCH_ALL_COMMAND = (
            "M-SEARCH * HTTP/1.1\r\n"
            + "HOST: 239.255.255.250:1900\r\n" 
            + "MAN: \"ssdp:discover\"\r\n"
            + "MX: 3\r\n" 
            + "ST: ssdp:all\r\n\r\n"
            ).getBytes();

    public AbstractUPnPDiscoverer(String address, int port) throws IOException {
        group = InetAddress.getByName(address);
        socket = new MulticastSocket(port);
    }

    protected abstract void onDiscoveryEvent(String packet) throws Exception;

    /**
     * join the UPnP SSDP network
     */
    public void start() {
        if (worker != null) {
            logger.warn("The multicast worker is running.");
            return;
        }

        worker = new Thread() {
            public void run() {
                joinNetwork();
                boolean isBreak = false;
                while (!stop) {
                    try {
                        socket.receive(packet);
                        try {
                            onDiscoveryEvent(new String(packet.getData()).trim());
                        } catch (Exception e) {
                            logger.error(e.getMessage());
                        } finally {
                            buffer.clear();
                            buffer.put(resetTemplate);
                            buffer.clear();
                        }
                    } catch (IOException e) {
                        isBreak = true;
                        break;
                    }
                    Thread.yield();
                }

                if (isBreak) {
                    AbstractUPnPDiscoverer.this.stop();
                }
            };
        };
        worker.start();
    }

    protected void joinNetwork() {
        try {
            socket.joinGroup(group);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    protected void leaveNetwork() {
        try {
            socket.leaveGroup(group);
        } catch (Exception ignored) {
        }
        try {
            socket.close();
        } catch (Exception ignored) {
        } finally {
            socket = null;
        }
    }

    /**
     * leave the UPnP SSDP network
     */
    public void stop() {
        stop = true;
        try {
            leaveNetwork();
            worker.interrupt();
        } catch (Exception ignored) {
        } finally {
            worker = null;
        }
    }

    /**
     * request the UPnP Device/Service in the network to response itself
     * @throws IOException
     */
    public void issueMSearch(boolean rootDeviceOnly) throws IOException {
        final byte[] command = rootDeviceOnly ? MSEARCH_ROOT_DEVICE_COMMAND : MSEARCH_ALL_COMMAND;
        socket.send(
                new DatagramPacket(command, command.length,
                group, UPnPContrants.UPNP_MULTICAST_PORT));
    }

}
