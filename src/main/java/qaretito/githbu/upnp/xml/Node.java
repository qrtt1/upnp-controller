package qaretito.githbu.upnp.xml;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.xml.sax.Attributes;

public class Node implements INode {

    private Node parent;
    private List<INode> children = new ArrayList<INode>();
    private String name;
    private Attributes attributes;
    private String text;

    public Node(Node parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Node getParent() {
        return this.parent;
    }

    public void addChild(Node currentNode) {
        this.children.add(currentNode);
    }
    
    private int getParentCount() {
        int count = 0;
        INode current = this;
        while (current.getParent() != null) {
            count++;
            current = current.getParent();
        }
        return count;
    }

    @Override
    public String toString() {
        final int parentCount = getParentCount();
        StringBuilder indent = new StringBuilder();
        for (int i = 0; i < parentCount; i++) {
            indent.append('\t');
        }
        
        final String content = 
            (text == null || "".equals(text)) ? "(empty)" : text;
        
        return String.format("\n%s%s(%s){%s}%s", 
                indent, name, parentCount + 1, content,  children);
    }

    @Override
    public List<INode> getChildren() {
        return children;
    }

    @Override
    public List<INode> find(String name) {
        Queue<INode> queue = new LinkedList<INode>();
        queue.add(this);
        List<INode> result = new ArrayList<INode>();
        while (!queue.isEmpty()) {
            INode node = queue.poll();
            if (node == null) {
                break;
            }
            if (node.getName().equals(name)) {
                result.add(node);
            }
            if (!node.getChildren().isEmpty()) {
                queue.addAll(node.getChildren());
            }
        }
        
        return result;
    }

}
