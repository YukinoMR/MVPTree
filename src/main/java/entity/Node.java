package entity;

public abstract class Node {
    // Common fields or methods shared by LeafNode and InternalNode
    // ...

    // Methods to be implemented by subclasses
    public abstract NodeType getType();
}



/*
//how to use it
Node node;

// Create a leaf node
LeafNode leafNode = new LeafNode();
leafNode.setType(NodeType.LEAF);
// Set leafNode specific fields

node = leafNode; // Assign leafNode to the base class reference

// Create an internal node
InternalNode internalNode = new InternalNode();
internalNode.setType(NodeType.INTERNAL);
// Set internalNode specific fields

node = internalNode; // Assign internalNode to the base class reference

// Determine the node type
NodeType type = node.getType();

 */