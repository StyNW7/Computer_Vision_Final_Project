package id.example.wastify.data

data class ApiResponse(
    val features: FloatArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ApiResponse

        if (!features.contentEquals(other.features)) return false

        return true
    }

    override fun hashCode(): Int {
        return features.contentHashCode()
    }
}

