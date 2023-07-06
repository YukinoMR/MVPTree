package entity;

public enum MVPDataWidth
{
    BYTEARRAY(1),
    UINT16ARRAY(2),
    UINT32ARRAY(4),
    UINT64ARRAY (8);

    public final int code;
    MVPDataWidth(int code) {
        this.code = code;
    }
};

