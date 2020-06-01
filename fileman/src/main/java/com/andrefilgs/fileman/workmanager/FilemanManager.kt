package com.andrefilgs.fileman.workmanager

internal class FilemanManager {
  
  companion object{
    var outputMessage = mutableMapOf<String,String>() //for Final Worker. Using filemanUniqueId as key and outputMessage as value
    
    var filesManager = mutableMapOf<String,Boolean>()
    
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
  
  
    //Fileman DTO due to "Data cannot occupy more than 10240KB when serialized [android-workmanager]"
    var dataTransferObject = mutableMapOf<String,String?>()
    val sufixFileContent = "FileContent"
    val sufixOutputMessage = "OutputMessage"
    
    
    internal fun getFileContent(filemanUniqueId:String?):String?{
      if(filemanUniqueId ==null) return null
      return dataTransferObject[filemanUniqueId + sufixFileContent]
    }
    
    internal fun putFileContent(filemanUniqueId:String,  fileContent: String?){
      dataTransferObject[filemanUniqueId + sufixFileContent] = fileContent
    }
  
  
    internal fun getOutputMessage(filemanUniqueId:String?):String?{
      if(filemanUniqueId ==null) return null
      return dataTransferObject[filemanUniqueId + sufixOutputMessage]
    }
  
    internal fun putOutputMessage(filemanUniqueId:String,  outputMessage: String?){
      dataTransferObject[filemanUniqueId + sufixOutputMessage] = outputMessage
    }
  
  }
  
}