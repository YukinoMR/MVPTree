package entity;

import java.util.List;

public class LeafNode extends Node{
    private NodeType type;
    private MVPDP sv1;
    private MVPDP sv2;
    private List<MVPDP> points;
    private List<Float> d1;
    private List<Float> d2;
    private int nbPoints;

    // Constructor
    public LeafNode(NodeType type, MVPDP sv1, MVPDP sv2, List<MVPDP> points, List<Float> d1, List<Float> d2, int nbPoints) {
        this.type = type;
        this.sv1 = sv1;
        this.sv2 = sv2;
        this.points = points;
        this.d1 = d1;
        this.d2 = d2;
        this.nbPoints = nbPoints;
    }

    // Getters and setters
    @Override
    public NodeType getType() {
        return NodeType.LEAF; // Return the leaf node type
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

    public List<MVPDP> getPoints() {
        return points;
    }

    public void setPoints(List<MVPDP> points) {
        this.points = points;
    }

    public List<Float> getD1() {
        return d1;
    }

    public void setD1(List<Float> d1) {
        this.d1 = d1;
    }

    public List<Float> getD2() {
        return d2;
    }

    public void setD2(List<Float> d2) {
        this.d2 = d2;
    }

    public int getNbPoints() {
        return nbPoints;
    }

    public void setNbPoints(int nbPoints) {
        this.nbPoints = nbPoints;
    }
}
