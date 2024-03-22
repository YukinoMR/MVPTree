package entity;

import java.util.List;

public class MVPDP {
    private String id;
    private Object data;
    private List<Float> path;
    private int maxPath;
    private int dataLen;
    private MVPDataType type;

    // Constructor
    public MVPDP(int id, Object data,MVPDataType type) {
        this.id = String.valueOf(id);
        this.data = data;
        this.path = null;
        this.dataLen = 0;
        this.type = type;
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
        this.maxPath = path.size();
    }

    public void setPath(int index, Float val){
        if(index > path.size())
            this.path.add(index - 1, 0.0f);
        this.path.add(index, val);
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
