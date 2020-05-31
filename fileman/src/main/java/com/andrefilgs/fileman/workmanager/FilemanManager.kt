package com.andrefilgs.fileman.workmanager

import android.content.Context

class FilemanManager {
  
  companion object{
    var gContext : Context?=null
    
    var outputMessage = mutableMapOf<String,String>() //for Final Worker. Using filemanUniqueId as key and outputMessage as value
    
    
    var filesManager = mutableMapOf<String,Boolean>()
    // var fileIsAvailable = false //simulating a file writing strategy
    
    fun isFileAvailable(filenameFullPath:String):Boolean{
      return filesManager[filenameFullPath] ?: true
    }
    
    fun lockFile(filenameFullPath: String){
      filesManager[filenameFullPath] = false
    }
  
    fun unlockFile(filenameFullPath: String){
      filesManager[filenameFullPath] = true
    }
    
    fun removeFileFromFilesManager(fileName: String){
      filesManager.remove(fileName)
    }
    
    fun clearFilesManager(){
      filesManager.clear()
    }
  }
  
}