package qaretito.githbu.upnp.xml;

import java.util.List;

import org.xml.sax.Attributes;

public interface INode {

    public abstract String getName();

    public abstract Attributes getAttributes();

    public abstract String getText();

    public abstract INode getParent();
    
    public abstract List<INode> getChildren();

    public abstract List<INode> find(String string);

}