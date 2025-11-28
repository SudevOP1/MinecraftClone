package data_structures;

public class Vector3s {

    public short x;
    public short y;
    public short z;

    public Vector3s(short x, short y, short z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(short x, short y, short z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(Vector3s other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
    }

}