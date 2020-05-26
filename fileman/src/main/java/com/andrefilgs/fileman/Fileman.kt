package com.andrefilgs.fileman

import android.content.Context
import android.util.Log
import java.io.*
import java.nio.charset.Charset


class Fileman {
  companion object {
    private val TAG = "Fileman"
    
    val DRIVE_SB = 0 //SandBox
    val DRIVE_SDI = 1 //Internal Device storage
    val DRIVE_SDE = 2 //External Device storage (SD card)
    
    //Extensions
    val JSON_EXTENSION = ".json"
    val VIDEO_EXTENSION = ".mp4"
    val TEMP_EXTENSION = ".tmp"
    val HTML_EXTENSION = ".html"
    val THUMB_EXTENSION = ".png"
    
    
    // ============================== Write ==============================
    
    
    private fun getFullPath(context: Context, driveInt: Int, folderStr: String, showLog: Boolean? = false): String? {
      var driveStringName = ""
      val pathStr: String
      if (driveInt == DRIVE_SB) {
        driveStringName = context.filesDir.absolutePath
      } else if (driveInt == DRIVE_SDI) {
        val path = context.getExternalFilesDirs(null)
        if (path[0] != null) {
          driveStringName = path[0].path
        } else {
          if (showLog!!) Log.d(TAG, "No emulator")
          return null
        }
      } else if (driveInt == DRIVE_SDE) {
        val path = context.getExternalFilesDirs(null) //Access only by PC, others Apps can't access here
        if (path.size >= 2 && path[1] != null) {
          driveStringName = path[1].path
        } else {
          if (showLog!!) Log.d(TAG, "No SD card available")
          return null
        }
      }
      pathStr = driveStringName + folderStr
      return pathStr
    }
    
    //Check if Generic Drive Folder exists, otherwise it will be created
    private fun createFolder(context: Context, driveStr: Int, folderStr: String, showLog: Boolean? = false): File? {
      val pathStr = getFullPath(context, driveStr, folderStr, showLog)
      if (pathStr != null) {
        val fullPath = File(pathStr)
        if (!fullPath.exists()) {
          fullPath.mkdirs()
        }
        return fullPath
      }
      return null
    }
    
    private fun createOutputFile(context: Context, drive: Int, folder: String, fileName: String, append: Boolean, showLog: Boolean? = false): OutputStream? {
      val fullPath = createFolder(context, drive, folder, showLog)
      if (fullPath != null) {
        val file = File(fullPath, fileName)
        return try {
          if (!file.exists()) {
            file.createNewFile()
            if (showLog!!) Log.d(TAG, "File created")
          } else {
            if (showLog!!) Log.d(TAG, "File already exist")
          }
          FileOutputStream(file, append)
        } catch (e: IOException) {
          e.printStackTrace()
          null
        }
        
      }
      return null
    }
    
    
    // ============================== Read ==============================
    
    /**
     * Check if Generic Drive Folder exists
     */
    private fun checkFolder(context: Context, drive: Int, folderStr: String, showLog: Boolean? = false): Boolean {
      val pathStr = getFullPath(context, drive, folderStr, showLog) ?: //Something wrong with the path, maybe there is no SD card
      return false
      val dirPath = File(pathStr)
      dirPath.mkdir()
      return dirPath.exists()
    }
    
    
    private fun getFolder(context: Context, drive: Int, folderStr: String, showLog: Boolean? = false): File? {
      if (!checkFolder(context, drive, folderStr, showLog)) return null
      val pathStr = getFullPath(context, drive, folderStr, showLog)
      return File(pathStr!!)
    }
    
    private fun getInputStreamFile(context: Context, drive: Int, folder: String, fileName: String, showLog: Boolean? = false): InputStream? {
      return try {
        val file = File(getFolder(context, drive, folder, showLog), fileName)
        FileInputStream(file)
      } catch (ex: IOException) {
        ex.printStackTrace()
        null
      }
    }
    
    
    
    /**
     * @param drive Sandbox / Internal / External
     * @param fileName wih extension e.g. ".json"
     */
    fun write(contentString: String, context: Context, drive: Int, folder: String, fileName: String, append: Boolean, showLog: Boolean? = false): Boolean {
      val out = createOutputFile(context, drive, folder, fileName, append, showLog) ?: return false
      
      return try {
        out.write(contentString.toByteArray())
        out.close()
        if (showLog!!) Log.d(TAG, "File $fileName write with success.\nContent is $contentString")
        true
      } catch (e: IOException) {
        e.printStackTrace()
        false
      }
    }
    
    
    /**
     * @param drive Sandbox / Internal / External
     * @param fileName wih extension e.g. ".json"
     */
    fun read(context: Context, drive: Int, folder: String, fileName: String, showLog: Boolean? = false): String? {
      val inputStream: InputStream? = getInputStreamFile(context, drive, folder, fileName, showLog)
      
      var content: String? = null
      try {
        inputStream?.let {
          //      InputStream inputStream = context.getAssets().open("file_name.json");
          val size = inputStream.available()
          val buffer = ByteArray(size)
          inputStream.read(buffer)
          inputStream.close()
          val charset: Charset = Charsets.UTF_8
          content = String(buffer, charset)
          if (showLog!!) Log.d(TAG, "File $fileName read with success.\nFile content is: $content")
        }
      } catch (ex: Exception) {
        ex.printStackTrace()
        return null
      }
      
      return content
    }
    
    fun delete(context: Context, drive: Int, folder: String, fileName: String): Boolean {
      return try {
        val file = File(getFolder(context, drive, folder), fileName)
        file.delete()
        true
      } catch (e: Exception) {
        e.printStackTrace()
        false
      }
    }
    
    
  }
}