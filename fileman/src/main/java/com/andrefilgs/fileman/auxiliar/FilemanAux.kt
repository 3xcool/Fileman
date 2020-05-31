package com.andrefilgs.fileman.auxiliar

import android.content.Context
import android.util.Log
import com.andrefilgs.fileman.enums.FilemanDrivers
import java.io.*

internal class FilemanAux {
  
  companion object{
    
    internal fun getNow():Long{
      return System.currentTimeMillis()
    }
    
    fun getFilenameFullPath(context: Context, driveInt: Int, folderStr: String,fileName: String):String?{
      val fullPath = getFullPath(context, driveInt, folderStr) ?: return null
      return "$fullPath/$fileName"
    }
  
    fun getFullPath(context: Context, driveInt: Int, folderStr: String): String? {
      var driveStringName = ""
      val pathStr: String
      if (driveInt == FilemanDrivers.SandBox.type) {
        driveStringName = context.filesDir.absolutePath
      } else if (driveInt == FilemanDrivers.Internal.type) {
        val path = context.getExternalFilesDirs(null)
        if (path[0] != null) {
          driveStringName = path[0].path
        } else {
          FilemanLogger.d("No emulator")
          return null
        }
      } else if (driveInt == FilemanDrivers.External.type) {
        val path = context.getExternalFilesDirs(null) //Access only by PC, others Apps can't access here
        if (path.size >= 2 && path[1] != null) {
          driveStringName = path[1].path
        } else {
          FilemanLogger.d("No SD card available")
          return null
        }
      }
      pathStr = driveStringName + folderStr
      return pathStr
    }
  
    //Check if Generic Drive Folder exists, otherwise it will be created
    fun createFolder(context: Context, driveStr: Int, folderStr: String): File? {
      val pathStr = getFullPath(context, driveStr, folderStr)
      if (pathStr != null) {
        val fullPath = File(pathStr)
        if (!fullPath.exists()) {
          fullPath.mkdirs()
        }
        return fullPath
      }
      return null
    }
  
    fun createOutputFile(context: Context, drive: Int, folder: String, fileName: String, append: Boolean): OutputStream? {
      val fullPath = createFolder(context, drive, folder)
      if (fullPath != null) {
        val file = File(fullPath, fileName)
        return try {
          if (!file.exists()) {
            file.createNewFile()
            FilemanLogger.d("File created")
          } else {
            FilemanLogger.d("File already exist")
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
    fun checkFolder(context: Context, drive: Int, folderStr: String): Boolean {
      val pathStr = getFullPath(context, drive, folderStr) ?: //Something wrong with the path, maybe there is no SD card
      return false
      val dirPath = File(pathStr)
      dirPath.mkdir()
      return dirPath.exists()
    }
  
  
    fun getFolder(context: Context, drive: Int, folderStr: String): File? {
      if (!checkFolder(context, drive, folderStr)) return null
      val pathStr = getFullPath(context, drive, folderStr)
      return File(pathStr!!)
    }
  
    fun getInputStreamFile(context: Context, drive: Int, folder: String, fileName: String): InputStream? {
      return try {
        val file = File(getFolder(context, drive, folder), fileName)
        FileInputStream(file)
      } catch (ex: IOException) {
        ex.printStackTrace()
        null
      }
    }
  }
  
  
}