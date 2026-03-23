package data_structures;

public class Vector2s {

    public short x;
    public short y;

    public Vector2s(short x, short y) {
        this.x = x;
        this.y = y;
    }

    public Vector2s(int x, int y) {
        this((short) x, (short) y);
    }

    public void set(Vector2s other) {
        this.x = other.x;
        this.y = other.y;
    }

    public void set(short x, short y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Vector2s other = (Vector2s) obj;
        return x == other.x && y == other.y;
    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

}