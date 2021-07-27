package net.purelic.cgm.core.maps.region;

import org.bukkit.util.Vector;

/******************************************************************************
 * From WorldEdit                                                             *
 * Written by sk89q                                                           *
 ******************************************************************************/

public class Vector2D {

    public static final Vector2D ZERO = new Vector2D(0, 0);
    public static final Vector2D UNIT_X = new Vector2D(1, 0);
    public static final Vector2D UNIT_Z = new Vector2D(0, 1);
    public static final Vector2D ONE = new Vector2D(1, 1);
    protected final double x;
    protected final double z;

    public Vector2D(double x, double z) {
        this.x = x;
        this.z = z;
    }

    public Vector2D(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public Vector2D(float x, float z) {
        this.x = x;
        this.z = z;
    }

    public Vector2D(Vector vector) {
        this.x = vector.getX();
        this.z = vector.getZ();
    }

    public Vector2D(Vector2D other) {
        this.x = other.x;
        this.z = other.z;
    }

    public Vector2D() {
        this.x = 0.0D;
        this.z = 0.0D;
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

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.z - other.z);
    }

    public Vector2D subtract(double x, double z) {
        return new Vector2D(this.x - x, this.z - z);
    }

    public Vector2D subtract(int x, int z) {
        return new Vector2D(this.x - (double) x, this.z - (double) z);
    }

    public Vector2D subtract(Vector2D... others) {
        double newX = this.x;
        double newZ = this.z;
        Vector2D[] var6 = others;
        int var7 = others.length;

        for (int var8 = 0; var8 < var7; ++var8) {
            Vector2D other = var6[var8];
            newX -= other.x;
            newZ -= other.z;
        }

        return new Vector2D(newX, newZ);
    }

    public Vector2D multiply(Vector2D other) {
        return new Vector2D(this.x * other.x, this.z * other.z);
    }

    public Vector2D multiply(double x, double z) {
        return new Vector2D(this.x * x, this.z * z);
    }

    public Vector2D multiply(int x, int z) {
        return new Vector2D(this.x * (double) x, this.z * (double) z);
    }

    public Vector2D multiply(Vector2D... others) {
        double newX = this.x;
        double newZ = this.z;
        Vector2D[] var6 = others;
        int var7 = others.length;

        for (int var8 = 0; var8 < var7; ++var8) {
            Vector2D other = var6[var8];
            newX *= other.x;
            newZ *= other.z;
        }

        return new Vector2D(newX, newZ);
    }

    public Vector2D multiply(double n) {
        return new Vector2D(this.x * n, this.z * n);
    }

    public Vector2D multiply(float n) {
        return new Vector2D(this.x * (double) n, this.z * (double) n);
    }

    public Vector2D multiply(int n) {
        return new Vector2D(this.x * (double) n, this.z * (double) n);
    }

    public Vector2D divide(Vector vector) {
        return new Vector2D(this.x / vector.getX(), this.z / vector.getZ());
    }

    public Vector2D divide(Vector2D other) {
        return new Vector2D(this.x / other.x, this.z / other.z);
    }

    public Vector2D divide(double x, double z) {
        return new Vector2D(this.x / x, this.z / z);
    }

    public Vector2D divide(int x, int z) {
        return new Vector2D(this.x / (double) x, this.z / (double) z);
    }

    public Vector2D divide(int n) {
        return new Vector2D(this.x / (double) n, this.z / (double) n);
    }

    public Vector2D divide(double n) {
        return new Vector2D(this.x / n, this.z / n);
    }

    public Vector2D divide(float n) {
        return new Vector2D(this.x / (double) n, this.z / (double) n);
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

    public double distanceSq(Vector2D other) {
        return Math.pow(other.x - this.x, 2.0D) + Math.pow(other.z - this.z, 2.0D);
    }

    public Vector2D normalize() {
        return this.divide(this.length());
    }

    public double dot(Vector2D other) {
        return this.x * other.x + this.z * other.z;
    }

    public boolean containedWithin(Vector2D min, Vector2D max) {
        return this.x >= min.x && this.x <= max.x && this.z >= min.z && this.z <= max.z;
    }

    public boolean containedWithinBlock(Vector2D min, Vector2D max) {
        return this.getBlockX() >= min.getBlockX() && this.getBlockX() <= max.getBlockX() && this.getBlockZ() >= min.getBlockZ() && this.getBlockZ() <= max.getBlockZ();
    }

    public Vector2D floor() {
        return new Vector2D(Math.floor(this.x), Math.floor(this.z));
    }

    public Vector2D ceil() {
        return new Vector2D(Math.ceil(this.x), Math.ceil(this.z));
    }

    public Vector2D round() {
        return new Vector2D(Math.floor(this.x + 0.5D), Math.floor(this.z + 0.5D));
    }

    public Vector2D positive() {
        return new Vector2D(Math.abs(this.x), Math.abs(this.z));
    }

    public Vector2D transform2D(double angle, double aboutX, double aboutZ, double translateX, double translateZ) {
        angle = Math.toRadians(angle);
        double x = this.x - aboutX;
        double z = this.z - aboutZ;
        double x2 = x * Math.cos(angle) - z * Math.sin(angle);
        double z2 = x * Math.sin(angle) + z * Math.cos(angle);
        return new Vector2D(x2 + aboutX + translateX, z2 + aboutZ + translateZ);
    }

    public boolean isCollinearWith(Vector2D other) {
        if (this.x == 0.0D && this.z == 0.0D) {
            return true;
        } else {
            double otherX = other.x;
            double otherZ = other.z;
            if (otherX == 0.0D && otherZ == 0.0D) {
                return true;
            } else if (this.x == 0.0D != (otherX == 0.0D)) {
                return false;
            } else if (this.z == 0.0D != (otherZ == 0.0D)) {
                return false;
            } else {
                double quotientX = otherX / this.x;
                if (!Double.isNaN(quotientX)) {
                    return other.equals(this.multiply(quotientX));
                } else {
                    double quotientZ = otherZ / this.z;
                    if (!Double.isNaN(quotientZ)) {
                        return other.equals(this.multiply(quotientZ));
                    } else {
                        throw new RuntimeException("This should not happen");
                    }
                }
            }
        }
    }

    public Vector toVector() {
        return new Vector(this.x, 0.0D, this.z);
    }

    public Vector toVector(double y) {
        return new Vector(this.x, y, this.z);
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Vector2D)) {
            return false;
        } else {
            Vector2D other = (Vector2D) obj;
            return other.x == this.x && other.z == this.z;
        }
    }

    public int hashCode() {
        return (new Double(this.x)).hashCode() >> 13 ^ (new Double(this.z)).hashCode();
    }

    public String toString() {
        return "(" + this.x + ", " + this.z + ")";
    }

    public static Vector2D getMinimum(Vector2D v1, Vector2D v2) {
        return new Vector2D(Math.min(v1.x, v2.x), Math.min(v1.z, v2.z));
    }

    public static Vector2D getMaximum(Vector2D v1, Vector2D v2) {
        return new Vector2D(Math.max(v1.x, v2.x), Math.max(v1.z, v2.z));
    }

}
