package tree.modelib.math

import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import kotlin.math.*
// might use in future for better animations
@Deprecated("Might be used in the future for animations")
class Quaternion(private var w: Double, var vec: Vector) {

    constructor(w: Double, x: Double, y: Double, z: Double) : this(w, Vector(x, y, z))
    fun toRotationMatrix(): Array<FloatArray> {
        val x = vec.x
        val y = vec.y
        val z = vec.z

        val xx = x * x
        val xy = x * y
        val xz = x * z
        val xw = x * w

        val yy = y * y
        val yz = y * z
        val yw = y * w

        val zz = z * z
        val zw = z * w

        val matrix = Array(4) { FloatArray(4) }

        matrix[0][0] = (1 - 2 * (yy + zz)).toFloat()
        matrix[0][1] = (2 * (xy - zw)).toFloat()
        matrix[0][2] = (2 * (xz + yw)).toFloat()
        matrix[0][3] = 0f

        matrix[1][0] = (2 * (xy + zw)).toFloat()
        matrix[1][1] = (1 - 2 * (xx + zz)).toFloat()
        matrix[1][2] = (2 * (yz - xw)).toFloat()
        matrix[1][3] = 0f

        matrix[2][0] = (2 * (xz - yw)).toFloat()
        matrix[2][1] = (2 * (yz + xw)).toFloat()
        matrix[2][2] = (1 - 2 * (xx + yy)).toFloat()
        matrix[2][3] = 0f

        matrix[3][0] = 0f
        matrix[3][1] = 0f
        matrix[3][2] = 0f
        matrix[3][3] = 1f

        return matrix
    }
    companion object {

        fun toQuaternion(e: EulerAngle): Quaternion {
            val c1x = cos(e.x * 0.5)
            val c2y = cos(e.y * -0.5)
            val c3z = cos(e.z * 0.5)
            val s1x = sin(e.x * 0.5)
            val s2y = sin(e.y * -0.5)
            val s3z = sin(e.z * 0.5)
            val x = s1x * c2y * c3z + c1x * s2y * s3z
            val y = c1x * s2y * c3z - s1x * c2y * s3z
            val z = c1x * c2y * s3z + s1x * s2y * c3z
            val w = c1x * c2y * c3z - s1x * s2y * s3z
            return Quaternion(w, x, y, z)
        }
        fun toEuler(q: Quaternion): EulerAngle {
            val xx = q.vec.x * 2
            val yy = q.vec.y * 2
            val zz = q.vec.z * 2
            val xy = q.vec.x * yy
            val xz = q.vec.x * zz
            val yz = q.vec.y * zz
            val wx = q.w * xx
            val wy = q.w * yy
            val wz = q.w * zz
            val m11 = 1 - (yy + zz)
            val m12 = xy - wz
            val m13 = xz + wy
            val m23 = yz - wx
            val m33 = 1 - (xx + yy)
            val ey = asin(clamp(m13, -1.0, 1.0))
            val ex = if (abs(m13) < 0.99999) atan2(-m23, m33) else atan2(m23, m33)
            val ez = if (abs(m13) < 0.99999) atan2(-m12, m11) else 0.0
            return EulerAngle(ex, -ey, ez)
        }

        fun multiply(a: Quaternion, b: Quaternion): Quaternion {
            val x = a.vec.x * b.w + a.w * b.vec.x + a.vec.y * b.vec.z - a.vec.z * b.vec.y
            val y = a.vec.y * b.w + a.w * b.vec.y + a.vec.z * b.vec.x - a.vec.x * b.vec.z
            val z = a.vec.z * b.w + a.w * b.vec.z + a.vec.x * b.vec.y - a.vec.y * b.vec.x
            val w = a.w * b.w - a.vec.x * b.vec.x - a.vec.y * b.vec.y - a.vec.z * b.vec.z
            return Quaternion(w, x, y, z)
        }

        fun multiply(a: Quaternion, b: Double): Quaternion {
            return Quaternion(a.w * b, a.vec.x * b, a.vec.y * b, a.vec.z * b)
        }

        fun dot(a: Quaternion, b: Quaternion): Double {
            return a.w * b.w + a.vec.dot(b.vec)
        }

        fun add(a: Quaternion, b: Quaternion): Quaternion {
            return Quaternion(a.w + b.w, a.vec.add(b.vec))
        }

        fun subtract(a: Quaternion, b: Quaternion): Quaternion {
            return Quaternion(a.w - b.w, a.vec.subtract(b.vec))
        }

        fun combine(origin: EulerAngle, delta: EulerAngle): EulerAngle {
            return toEuler(multiply(toQuaternion(origin), toQuaternion(delta)))
        }

        fun slerp(a: EulerAngle, b: EulerAngle, t: Double): EulerAngle {
            var qA = toQuaternion(a)
            var qB = toQuaternion(b)
            var dot = dot(qA, qB)
            if (dot < 0) {
                qB = multiply(qB, -1.0)
                dot = -dot
            }
            if (dot > 0.9995) {
                var result = subtract(qB, qA)
                result = multiply(result, t)
                result = add(qA, result)
                return toEuler(result)
            } else {
                val theta_0 = acos(dot)
                val theta = theta_0 * t
                val sin_theta = sin(theta)
                val sin_theta_0 = sin(theta_0)
                val sA = cos(theta) - dot * sin_theta / sin_theta_0
                val sB = sin_theta / sin_theta_0
                val rQA = multiply(qA, sA)
                val rQB = multiply(qB, sB)
                return toEuler(add(rQA, rQB))
            }
        }
        fun conjugate(q: Quaternion): Quaternion {
            return Quaternion(q.w, -q.vec.x, -q.vec.y, -q.vec.z)
        }
        private fun clamp(value: Double, min: Double, max: Double): Double {
            return min(max(value, min), max)
        }
    }
    fun multiply(b: Double) {
        w *= b
        vec.multiply(b)
    }

    fun normalize() {
        val norm = sqrt(w * w + vec.x * vec.x + vec.y * vec.y + vec.z * vec.z)
        w /= norm
        vec.multiply(1.0 / norm)
    }

    fun toFormula(): String {
        return "[$w+${vec.x}i+${vec.y}j+${vec.z}k]"
    }
}
