package entity;

import java.util.List;

public class InternalNode extends Node{
    private NodeType type;
    private MVPDP sv1;
    private MVPDP sv2;
    private List<Float> M1;
    private List<Float> M2;
    private List<Object> childNodes;

    // Constructor
    public InternalNode(NodeType type, MVPDP sv1, MVPDP sv2, List<Float> M1, List<Float> M2, List<Object> childNodes) {
        this.type = type;
        this.sv1 = sv1;
        this.sv2 = sv2;
        this.M1 = M1;
        this.M2 = M2;
        this.childNodes = childNodes;
    }

    @Override
    public NodeType getType() {
        return NodeType.INTERNAL; // Return the internal node type
    }

    public void setType(NodeType type) {
        this.type = type;
    }

    public MVPDP getSv1() {
        return sv1;
    }

    public void setSv1(MVPDP sv1) {
        this.sv1 = sv1;
    }

    public MVPDP getSv2() {
        return sv2;
    }

    public void setSv2(MVPDP sv2) {
        this.sv2 = sv2;
    }

    public List<Float> getM1() {
        return M1;
    }

    public void setM1(List<Float> M1) {
        this.M1 = M1;
    }

    public List<Float> getM2() {
        return M2;
    }

    public void setM2(List<Float> M2) {
        this.M2 = M2;
    }

    public List<Object> getChildNodes() {
        return childNodes;
    }

    public void setChildNodes(List<Object> childNodes) {
        this.childNodes = childNodes;
    }
}
