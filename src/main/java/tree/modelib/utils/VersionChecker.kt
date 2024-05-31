package tree.modelib.utils
import org.bukkit.Bukkit


object VersionChecker {
    /**
     * Compares a Minecraft version with the current version on the server. Returns true if the version on the server is older.
     *
     * @param majorVersion Target major version to compare (i.e. 1.>>>17<<<.0)
     * @param minorVersion Target minor version to compare (i.e. 1.17.>>>0<<<)
     * @return Whether the version is under the value to be compared
     */
    fun serverVersionOlderThan(majorVersion: Int, minorVersion: Int): Boolean {
        val splitVersion = Bukkit.getBukkitVersion().split("[.]".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

        val actualMajorVersion =
            splitVersion[1].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toInt()

        var actualMinorVersion = 0
        if (splitVersion.size > 2) actualMinorVersion =
            splitVersion[2].split("-".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()[0].toInt()

        if (actualMajorVersion < majorVersion) return true

        if (splitVersion.size > 2) return actualMajorVersion == majorVersion && actualMinorVersion < minorVersion

        return false
    }
}