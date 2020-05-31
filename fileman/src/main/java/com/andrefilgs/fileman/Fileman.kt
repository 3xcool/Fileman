package com.andrefilgs.fileman

import android.content.Context
import com.andrefilgs.fileman.auxiliar.FilemanAux
import com.andrefilgs.fileman.auxiliar.FilemanLogger
import com.andrefilgs.fileman.workmanager.FilemanManager
import java.io.*
import java.nio.charset.Charset


class Fileman {
  companion object {

    
    //region ============================================================
    /**
     * @param drive Sandbox / Internal / External
     * @param fileName wih extension e.g. ".json"
     */
    fun write(contentString: String, context: Context, drive: Int, folder: String, fileName: String, append: Boolean): Boolean {
      FilemanManager.lockFile(fileName)
      val out = FilemanAux.createOutputFile(context, drive, folder, fileName, append) ?: return false
      
      return try {
        out.write(contentString.toByteArray())
        out.close()
        FilemanLogger.d( "File $fileName write with success.\nContent is $contentString")
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
    fun read(context: Context, drive: Int, folder: String, fileName: String): String? {
      FilemanManager.lockFile(fileName)
      val inputStream: InputStream? = FilemanAux.getInputStreamFile(context, drive, folder, fileName)
      
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
          FilemanLogger.d( "File $fileName read with success.\nFile content is: $content")
        }
      } catch (ex: Exception) {
        ex.printStackTrace()
        return null
      }
      
      return content
    }
    
    fun delete(context: Context, drive: Int, folder: String, fileName: String): Boolean {
      return try {
        val file = File(FilemanAux.getFolder(context, drive, folder), fileName)
        file.delete()
        true
      } catch (e: Exception) {
        e.printStackTrace()
        false
      }
    }
    //endregion
    
  }
}