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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vector3s other = (Vector3s) obj;
        return x == other.x && y == other.y && z == other.z;
    }

    @Override
    public int hashCode() {
        return 31 * (31 * x + y) + z;
    }

}