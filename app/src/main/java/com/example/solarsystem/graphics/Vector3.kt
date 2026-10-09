package com.example.solarsystem.graphics

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class Vector3(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f
) {
    operator fun plus(other: Vector3): Vector3 = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3): Vector3 = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float): Vector3 = Vector3(x * scalar, y * scalar, z * scalar)
    operator fun div(scalar: Float): Vector3 = Vector3(x / scalar, y / scalar, z / scalar)

    fun length(): Float = sqrt(x * x + y * y + z * z)

    fun normalized(): Vector3 {
        val len = length()
        return if (len > 0.00001f) Vector3(x / len, y / len, z / len) else Vector3(0f, 0f, 0f)
    }

    fun dot(other: Vector3): Float = x * other.x + y * other.y + z * other.z

    fun cross(other: Vector3): Vector3 = Vector3(
        y * other.z - z * other.y,
        z * other.x - x * other.z,
        x * other.y - y * other.x
    )

    fun copyFrom(other: Vector3) {
        x = other.x
        y = other.y
        z = other.z
    }
}

class Matrix4 {
    val m = FloatArray(16)

    init {
        identity()
    }

    fun identity() {
        for (i in 0 until 16) m[i] = 0f
        m[0] = 1f; m[5] = 1f; m[10] = 1f; m[15] = 1f
    }

    fun multiply(b: Matrix4): Matrix4 {
        val result = Matrix4()
        for (row in 0..3) {
            for (col in 0..3) {
                var sum = 0f
                for (k in 0..3) {
                    sum += this.m[row * 4 + k] * b.m[k * 4 + col]
                }
                result.m[row * 4 + col] = sum
            }
        }
        return result
    }

    fun transform(v: Vector3): Vector3 {
        val rx = m[0] * v.x + m[1] * v.y + m[2] * v.z + m[3]
        val ry = m[4] * v.x + m[5] * v.y + m[6] * v.z + m[7]
        val rz = m[8] * v.x + m[9] * v.y + m[10] * v.z + m[11]
        val rw = m[12] * v.x + m[13] * v.y + m[14] * v.z + m[15]
        return if (rw != 0f && rw != 1f) {
            Vector3(rx / rw, ry / rw, rz / rw)
        } else {
            Vector3(rx, ry, rz)
        }
    }

    companion object {
        fun lookAt(eye: Vector3, target: Vector3, up: Vector3): Matrix4 {
            val f = (target - eye).normalized()
            val s = f.cross(up).normalized()
            val u = s.cross(f)

            val mat = Matrix4()
            mat.m[0] = s.x;  mat.m[1] = s.y;  mat.m[2] = s.z;  mat.m[3] = -s.dot(eye)
            mat.m[4] = u.x;  mat.m[5] = u.y;  mat.m[6] = u.z;  mat.m[7] = -u.dot(eye)
            mat.m[8] = -f.x; mat.m[9] = -f.y; mat.m[10] = -f.z; mat.m[11] = f.dot(eye)
            mat.m[12] = 0f;  mat.m[13] = 0f;  mat.m[14] = 0f;  mat.m[15] = 1f
            return mat
        }

        fun perspective(fovYDeg: Float, aspect: Float, near: Float, far: Float): Matrix4 {
            val rad = Math.toRadians(fovYDeg.toDouble()).toFloat()
            val tanHalfFovy = kotlin.math.tan(rad / 2f)

            val mat = Matrix4()
            for (i in 0 until 16) mat.m[i] = 0f
            mat.m[0] = 1f / (aspect * tanHalfFovy)
            mat.m[5] = 1f / tanHalfFovy
            mat.m[10] = -(far + near) / (far - near)
            mat.m[11] = -(2f * far * near) / (far - near)
            mat.m[14] = -1f
            return mat
        }

        fun rotationX(rad: Float): Matrix4 {
            val mat = Matrix4()
            val c = cos(rad)
            val s = sin(rad)
            mat.m[5] = c;  mat.m[6] = -s
            mat.m[9] = s;  mat.m[10] = c
            return mat
        }

        fun rotationY(rad: Float): Matrix4 {
            val mat = Matrix4()
            val c = cos(rad)
            val s = sin(rad)
            mat.m[0] = c;   mat.m[2] = s
            mat.m[8] = -s;  mat.m[10] = c
            return mat
        }

        fun rotationZ(rad: Float): Matrix4 {
            val mat = Matrix4()
            val c = cos(rad)
            val s = sin(rad)
            mat.m[0] = c;  mat.m[1] = -s
            mat.m[4] = s;  mat.m[5] = c
            return mat
        }

        fun translation(x: Float, y: Float, z: Float): Matrix4 {
            val mat = Matrix4()
            mat.m[3] = x
            mat.m[7] = y
            mat.m[11] = z
            return mat
        }

        fun alignToDirection(dir: Vector3): Matrix4 {
            val d = dir.normalized()
            val up = if (kotlin.math.abs(d.y) < 0.99f) Vector3(0f, 1f, 0f) else Vector3(1f, 0f, 0f)
            val right = d.cross(up).normalized()
            val trueUp = right.cross(d).normalized()

            val mat = Matrix4()
            mat.m[0] = right.x;  mat.m[1] = trueUp.x;  mat.m[2] = d.x;  mat.m[3] = 0f
            mat.m[4] = right.y;  mat.m[5] = trueUp.y;  mat.m[6] = d.y;  mat.m[7] = 0f
            mat.m[8] = right.z;  mat.m[9] = trueUp.z;  mat.m[10] = d.z; mat.m[11] = 0f
            mat.m[12] = 0f;      mat.m[13] = 0f;       mat.m[14] = 0f;  mat.m[15] = 1f
            return mat
        }
    }
}
