package net.purelic.cgm.core.maps.region;

import org.bukkit.util.Vector;

/******************************************************************************
 * From WorldEdit                                                             *
 * Written by sk89q                                                           *
 ******************************************************************************/

public class Vector2D {

    protected final double x;
    protected final double z;

    public Vector2D(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public Vector2D(Vector vector) {
        this.x = vector.getX();
        this.z = vector.getZ();
    }

    public double getX() {
        return this.x;
    }

    public int getBlockX() {
        return (int) Math.round(this.x);
    }

    public Vector2D setX(double x) {
        return new Vector2D(x, this.z);
    }

    public Vector2D setX(int x) {
        return new Vector2D((double) x, this.z);
    }

    public double getZ() {
        return this.z;
    }

    public int getBlockZ() {
        return (int) Math.round(this.z);
    }

    public Vector2D setZ(double z) {
        return new Vector2D(this.x, z);
    }

    public Vector2D setZ(int z) {
        return new Vector2D(this.x, (double) z);
    }

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.z + other.z);
    }

    public Vector2D add(double x, double z) {
        return new Vector2D(this.x + x, this.z + z);
    }

    public Vector2D add(int x, int z) {
        return new Vector2D(this.x + (double) x, this.z + (double) z);
    }

    public Vector2D add(Vector2D... others) {
        double newX = this.x;
        double newZ = this.z;

        for (Vector2D other : others) {
            newX += other.x;
            newZ += other.z;
        }

        return new Vector2D(newX, newZ);
    }

    public Vector2D subtract(Vector vector) {
        return new Vector2D(this.x - vector.getX(), this.z - vector.getZ());
    }

    public Vector2D divide(Vector vector) {
        return new Vector2D(this.x / vector.getX(), this.z / vector.getZ());
    }

    public double length() {
        return Math.sqrt(this.x * this.x + this.z * this.z);
    }

    public double lengthSq() {
        return this.x * this.x + this.z * this.z;
    }

    public double distance(Vector2D other) {
        return Math.sqrt(Math.pow(other.x - this.x, 2.0D) + Math.pow(other.z - this.z, 2.0D));
    }

    public Vector2D round() {
        return new Vector2D(Math.floor(this.x + 0.5D), Math.floor(this.z + 0.5D));
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2D)) {
            return false;
        } else {
            Vector2D other = (Vector2D) obj;
            return other.x == this.x && other.z == this.z;
        }
    }

    public String toString() {
        return "(" + this.x + ", " + this.z + ")";
    }

}
