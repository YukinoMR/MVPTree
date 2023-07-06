package entity;

import java.util.List;

public class MVPDP {
    private String id;
    private Object data;
    private List<Float> path;
    private int dataLen;
    private MVPDataType type;

    // Constructor
    public MVPDP() {
        this.id = null;
        this.data = null;
        this.path = null;
        this.dataLen = 0;
        this.type = null;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public List<Float> getPath() {
        return path;
    }

    public void setPath(List<Float> path) {
        this.path = path;
    }

    public void setPath(int index, Float val){
        this.path.set(index, val);
    }

    public int getDataLen() {
        return dataLen;
    }

    public void setDataLen(int dataLen) {
        this.dataLen = dataLen;
    }

    public MVPDataType getType() {
        return type;
    }

    public void setType(MVPDataType type) {
        this.type = type;
    }
}
