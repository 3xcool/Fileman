package com.andrefilgs.fileman

import android.content.Context
import com.andrefilgs.fileman.auxiliar.FilemanLogger
import com.andrefilgs.fileman.workmanager.FilemanManager
import java.io.*
import java.nio.charset.Charset

/**
 * In 99% of the cases you will only need this class for File Management (CRUD).
 * Because of this I created another repository called FilemanLite, check it out.
 *
 * If you want to use Workmanager + Coroutines for File Management, please check FilemanWM class
 */
class Fileman {
  companion object {

    
    //region ======================= MAIN FILEMAN FUN =====================================
    /**
     * @param drive Sandbox / Internal / External
     * @param filename wih extension e.g. ".json"
     */
    fun write(fileContent: String, context: Context, drive: Int, folder: String, filename: String, append: Boolean): Boolean {
      FilemanManager.lockFile(filename)
      val out = createOutputFile(context, drive, folder, filename, append) ?: return false
      
      return try {
        out.write(fileContent.toByteArray())
        out.close()
        FilemanLogger.d( "File $filename write with success.\nContent is $fileContent")
        true
      } catch (e: IOException) {
        e.printStackTrace()
        false
      }
    }
    
    
    /**
     * @param drive Sandbox / Internal / External
     * @param filename wih extension e.g. ".json"
     */
    fun read(context: Context, drive: Int, folder: String, filename: String): String? {
      FilemanManager.lockFile(filename)
      val inputStream: InputStream? = getInputStreamFile(context, drive, folder, filename)
      
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
          FilemanLogger.d( "File $filename read with success.\nFile content is: $content")
        }
      } catch (ex: Exception) {
        ex.printStackTrace()
        return null
      }
      
      return content
    }
    
    fun delete(context: Context, drive: Int, folder: String, filename: String): Boolean {
      return try {
        val file = File(getFolder(context, drive, folder), filename)
        file.delete()
        true
      } catch (e: Exception) {
        e.printStackTrace()
        false
      }
    }
    //endregion
  
  
  
    //region =================== Auxiliar Funs ===================
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
  
    fun getInputStreamFile(context: Context, drive: Int, folder: String, filename: String): InputStream? {
      return try {
        val file = File(getFolder(context, drive, folder), filename)
        FileInputStream(file)
      } catch (ex: IOException) {
        ex.printStackTrace()
        null
      }
    }
    //endregion
    
  }
}

enum class FilemanDrivers (val type:Int){
  SandBox(0),  //Where the app is installed
  Internal(1), //Internal Device storage
  External(2)  //External Device storage (SD card)
}