package qaretito.githbu.upnp;

@SuppressWarnings("serial")
public class UPnPException extends Exception {

    public UPnPException(Exception e) {
        super(e);
    }

    public UPnPException(String message) {
        super(message);
    }

    public UPnPException(String message, Exception e) {
        super(message, e);
    }

}
