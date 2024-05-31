package tree.modelib.utils

import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipFile {
    fun zip(directory: File, targetZipPath: String?): Boolean {
        if (!directory.exists()) {
            Logger.warn("Failed to zip directory ${directory.path} because it does not exist!")
            return false
        }

        try {
            ZipUtility.zip(directory, targetZipPath)
            return true
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        }
    }

    object ZipUtility {
        /**
         * A constants for buffer size used to read/write data
         */
        private const val BUFFER_SIZE = 8192

        /**
         * Compresses a list of files to a destination zip file
         *
         * @param file        File to zip
         * @param destZipFile The path of the destination zip file
         * @throws FileNotFoundException
         * @throws IOException
         */
        @Throws(IOException::class)
        fun zip(file: File, destZipFile: String?) {
            ZipOutputStream(FileOutputStream(destZipFile.toString())).use { zos ->
                if (file.isDirectory) {
                    for (file1 in file.listFiles()!!) {
                        if (file1.isDirectory) zipDirectory(file1, file1.name, zos)
                        else zipFile(file1, zos)
                    }
                } else {
                    zipFile(file, zos)
                }
            }
        }

        /**
         * Adds a directory to the current zip output stream
         *
         * @param folder       the directory to be  added
         * @param parentFolder the path of parent directory
         * @param zos          the current zip output stream
         * @throws FileNotFoundException
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun zipDirectory(
            folder: File, parentFolder: String,
            zos: ZipOutputStream
        ) {
            for (file in folder.listFiles()!!) {
                if (file.isDirectory) {
                    zipDirectory(file, parentFolder + "/" + file.name, zos)
                    continue
                }
                zos.putNextEntry(ZipEntry(parentFolder + "/" + file.name))
                BufferedInputStream(FileInputStream(file)).use { bis ->
                    var bytesRead: Long = 0
                    val bytesIn = ByteArray(BUFFER_SIZE)
                    var read: Int
                    while (bis.read(bytesIn).also { read = it } != -1) {
                        zos.write(bytesIn, 0, read)
                        bytesRead += read.toLong()
                    }
                }
                zos.closeEntry()
            }
        }

        /**
         * Adds a file to the current zip output stream
         *
         * @param file the file to be added
         * @param zos  the current zip output stream
         * @throws FileNotFoundException
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun zipFile(file: File, zos: ZipOutputStream) {
            zos.putNextEntry(ZipEntry(file.name))
            BufferedInputStream(FileInputStream(file)).use { bis ->
                var bytesRead: Long = 0
                val bytesIn = ByteArray(BUFFER_SIZE)
                var read: Int
                while (bis.read(bytesIn).also { read = it } != -1) {
                    zos.write(bytesIn, 0, read)
                    bytesRead += read.toLong()
                }
            }
            zos.closeEntry()
        }
    }
}
