package com.andrefilgs.fileman.model

import android.os.Parcelable
import androidx.work.WorkInfo
import com.andrefilgs.fileman.auxiliar.*
import com.andrefilgs.fileman.enums.FilemanCommands
import com.andrefilgs.fileman.enums.FilemanStatus
import kotlinx.android.parcel.Parcelize
import java.util.*

@Parcelize
data class FilemanFeedback(
  
  val status: Int?=null ,
  
  val statusName: String?=null ,
  
  val message: String?=null ,
  
  val commandType: String?=null ,
  
  //filenameFullPath + timestamp
  val filemanId: String?=null ,
  
  //provided by WorkManager
  val workerId: UUID?=null ,
  
  //provided by WorkManager
  val isFinalWorker: Boolean?=null,
  
  val filenameFullPath: String?=null,
  
  val fileContent: String?=null,
  
  val progress: Int?=null,
  val progressMessage: String?=null
) :Parcelable{
  
  companion object{
    
    fun buildFromWorkInfo(workInfo: WorkInfo):FilemanFeedback{
      val status = convertWorkStateToFilemanStatus(workInfo)
      return FilemanFeedback(
        status = status.type,
        statusName = status.name,
        message = workInfo.getOutput(),
        commandType = workInfo.getFilemanCommand(),
        filemanId = workInfo.getFilemanId(),
        isFinalWorker = workInfo.isFinalWork(),
        workerId = workInfo.id,
        filenameFullPath = workInfo.getFullPath(),
        fileContent = workInfo.getFileContent(),
        progress = workInfo.getProgressValue(),
        progressMessage = workInfo.getProgressMessage()
      )
    }
  
  
    
    fun convertWorkStateToFilemanStatus(workInfo: WorkInfo):FilemanStatus{
      return when (workInfo.state){
        WorkInfo.State.SUCCEEDED -> FilemanStatus.SUCCEEDED
        WorkInfo.State.CANCELLED -> FilemanStatus.CANCELLED
        WorkInfo.State.FAILED -> FilemanStatus.FAILED
        WorkInfo.State.BLOCKED -> FilemanStatus.BLOCKED
        WorkInfo.State.ENQUEUED -> FilemanStatus.ENQUEUE
        WorkInfo.State.RUNNING -> FilemanStatus.RUNNING
        else -> FilemanStatus.IDLE
      }
    }
    
    fun buildOutputMessage(status:Boolean?, filemanCommand:String?, filenameFullPath: String?, filename:String, fileContent:String?, ellapsed:Long?):String{
      return when(filemanCommand){
        FilemanCommands.WRITE.name -> if (status.orDefault(false)) "File \"$filename\" has been written with content: \"$fileContent\" after $ellapsed milliseconds." else "ERROR: File \"$filenameFullPath\" has not been written after $ellapsed milliseconds."
        FilemanCommands.DELETE.name -> if (status.orDefault(false)) "File \"$filename\" has been deleted after $ellapsed milliseconds." else "ERROR: File \"$filenameFullPath\" has not been deleted after $ellapsed milliseconds."
        FilemanCommands.READ.name -> if (status.orDefault(false)) "File \"$filename\" has been read with content: \"$fileContent\" after $ellapsed milliseconds." else "ERROR: File \"$filenameFullPath\" has not been read after $ellapsed milliseconds."
        else -> ""
      }
    }
  }

}