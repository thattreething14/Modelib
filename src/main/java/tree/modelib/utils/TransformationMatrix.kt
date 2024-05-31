package tree.modelib.utils

import org.bukkit.util.Vector
import kotlin.math.*


class TransformationMatrix {
    private var matrix = Array(4) { FloatArray(4) }

    init {
        // Initialize with identity matrix
        resetToIdentityMatrix()
    }

    fun resetToIdentityMatrix() {
        for (i in 0..3) {
            for (j in 0..3) {
                matrix[i][j] = (if ((i == j)) 1 else 0).toFloat()
            }
        }
    }

    fun translate(vector: Vector) {
        translate(vector.x.toFloat(), vector.y.toFloat(), vector.z.toFloat())
    }

    fun translate(x: Float, y: Float, z: Float) {
        val translationMatrix = TransformationMatrix()
        translationMatrix.matrix[0][3] = x
        translationMatrix.matrix[1][3] = y
        translationMatrix.matrix[2][3] = z
        multiplyWith(translationMatrix)
    }

    fun scale(x: Float, y: Float, z: Float) {
        val scaleMatrix = TransformationMatrix()
        scaleMatrix.matrix[0][0] = x
        scaleMatrix.matrix[1][1] = y
        scaleMatrix.matrix[2][2] = z
        multiplyWith(scaleMatrix)
    }

    /**
     * Rotates the matrix by x y z coordinates. Must be in radian!
     */
    fun rotate(x: Float, y: Float, z: Float) {
        rotateZ(z)
        rotateY(y)
        rotateX(x)
    }

    fun rotateX(angleRadians: Float) {
        val cosAngle = cos(angleRadians.toDouble()).toFloat()
        val sinAngle = sin(angleRadians.toDouble()).toFloat()
        val tempMatrix = TransformationMatrix()
        tempMatrix.matrix[1][1] = cosAngle
        tempMatrix.matrix[1][2] = -sinAngle
        tempMatrix.matrix[2][1] = sinAngle
        tempMatrix.matrix[2][2] = cosAngle
        multiplyWith(tempMatrix)
    }

    fun rotateY(angleRadians: Float) {
        val cosAngle = cos(angleRadians.toDouble()).toFloat()
        val sinAngle = sin(angleRadians.toDouble()).toFloat()
        val tempMatrix = TransformationMatrix()
        tempMatrix.matrix[0][0] = cosAngle
        tempMatrix.matrix[0][2] = sinAngle
        tempMatrix.matrix[2][0] = -sinAngle
        tempMatrix.matrix[2][2] = cosAngle
        multiplyWith(tempMatrix)
    }

    fun rotateZ(angleRadians: Float) {
        val cosAngle = cos(angleRadians.toDouble()).toFloat()
        val sinAngle = sin(angleRadians.toDouble()).toFloat()
        val tempMatrix = TransformationMatrix()
        tempMatrix.matrix[0][0] = cosAngle
        tempMatrix.matrix[0][1] = -sinAngle
        tempMatrix.matrix[1][0] = sinAngle
        tempMatrix.matrix[1][1] = cosAngle
        multiplyWith(tempMatrix)
    }
    fun applyTransformation(point: FloatArray): FloatArray {
        val result = FloatArray(4)
        for (i in 0..3) {
            for (j in 0..3) {
                result[i] += matrix[i][j] * point[j]
            }
        }
        return result
    }
    fun copyFrom(other: TransformationMatrix) {
        for (i in 0..3) {
            for (j in 0..3) {
                matrix[i][j] = other.matrix[i][j]
            }
        }
    }

    private fun multiplyWith(other: TransformationMatrix) {
        val result = Array(4) { FloatArray(4) }
        for (i in 0..3) {
            for (j in 0..3) {
                for (k in 0..3) {
                    result[i][j] += matrix[i][k] * other.matrix[k][j]
                }
            }
        }
        this.matrix = result
    }

    val translation: FloatArray
        /**
         * Extracts a xyz position
         *
         * @return [x, y, z]
         */
        get() =// Extract translation components directly from the matrix
            floatArrayOf(matrix[0][3], matrix[1][3], matrix[2][3])

    val rotation: FloatArray
        /**
         * Extracts a rotation in radians
         *
         * @return [x, y, z]
         */
        get() {
            // Assuming the rotation matrix is "pure" (no scaling) and follows XYZ order
            val rotation = FloatArray(3)

            // Yaw (rotation around Y axis)
            rotation[1] = atan2(
                -matrix[2][0].toDouble(),
                sqrt((matrix[0][0] * matrix[0][0] + matrix[1][0] * matrix[1][0]).toDouble())
            )
                .toFloat()

            // As a special case, if cos(yaw) is close to 0, use an alternative calculation
            if (abs(matrix[2][0].toDouble()) < 1e-6 && abs(matrix[2][2].toDouble()) < 1e-6) {
                // Pitch (rotation around X axis)
                rotation[0] = atan2(matrix[1][2].toDouble(), matrix[1][1].toDouble()).toFloat()
                // Roll (rotation around Z axis) is indeterminate: set to 0 or use previous value
                rotation[2] = 0f
            } else {
                // Pitch (rotation around X axis)
                rotation[0] = atan2(matrix[2][1].toDouble(), matrix[2][2].toDouble()).toFloat()
                // Roll (rotation around Z axis)
                rotation[2] = atan2(matrix[1][0].toDouble(), matrix[0][0].toDouble()).toFloat()
            }

            return rotation // Returns rotations in radians
        }
    fun changeBasis(newBasisX: Vector, newBasisY: Vector, newBasisZ: Vector) {
        val basisChangeMatrix = TransformationMatrix()
        // Set the matrix columns to the new basis vectors
        basisChangeMatrix.matrix[0][0] = newBasisX.x.toFloat()
        basisChangeMatrix.matrix[1][0] = newBasisX.y.toFloat()
        basisChangeMatrix.matrix[2][0] = newBasisX.z.toFloat()

        basisChangeMatrix.matrix[0][1] = newBasisY.x.toFloat()
        basisChangeMatrix.matrix[1][1] = newBasisY.y.toFloat()
        basisChangeMatrix.matrix[2][1] = newBasisY.z.toFloat()

        basisChangeMatrix.matrix[0][2] = newBasisZ.x.toFloat()
        basisChangeMatrix.matrix[1][2] = newBasisZ.y.toFloat()
        basisChangeMatrix.matrix[2][2] = newBasisZ.z.toFloat()

        // Since this matrix transforms from the new basis to the original, to change the basis of this matrix,
        // we multiply it with the basisChangeMatrix.
        multiplyWith(basisChangeMatrix)
    }

    fun changeTo180DegYBasis(): TransformationMatrix {
        val newX = Vector(-1, 0, 0) // New X-axis after 180 rotation around Y
        val newY = Vector(0, 1, 0) // Y-axis remains unchanged
        val newZ = Vector(0, 0, -1) // New Z-axis after 180 rotation around Y

        changeBasis(newX, newY, newZ)

        return this
    }

    fun flipX() {
        val flipMatrix = TransformationMatrix()
        flipMatrix.matrix[0][0] = -1f
        multiplyWith(flipMatrix)
    }

    fun flipY() {
        val flipMatrix = TransformationMatrix()
        flipMatrix.matrix[1][1] = -1f
        multiplyWith(flipMatrix)
    }

    fun flipZ() {
        val flipMatrix = TransformationMatrix()
        flipMatrix.matrix[2][2] = -1f
        multiplyWith(flipMatrix)
    }

    companion object {
        fun multiplyMatrices(
            firstMatrix: TransformationMatrix,
            secondMatrix: TransformationMatrix,
            resultMatrix: TransformationMatrix
        ) {
            // Assume resultMatrix is already initialized to the correct dimensions (4x4)
            for (row in 0..3) {
                for (col in 0..3) {
                    resultMatrix.matrix[row][col] = 0f // Reset result matrix cell
                    for (i in 0..3) {
                        resultMatrix.matrix[row][col] += firstMatrix.matrix[row][i] * secondMatrix.matrix[i][col]
                    }
                }
            }
        }
    }
}