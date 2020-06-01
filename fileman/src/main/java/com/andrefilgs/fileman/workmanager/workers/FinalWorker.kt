package com.andrefilgs.fileman.workmanager.workers

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.andrefilgs.fileman.auxiliar.*
import com.andrefilgs.fileman.workmanager.FilemanManager


internal  class FinalWorker(appContext: Context,workerParams: WorkerParameters)
  : Worker(appContext, workerParams) {
  
  override fun doWork(): Result {
    return try {
      val initTime = getInitTime()
      val filemanUniqueId = getFilemanUniqueId()
      val fileFullPath = getFileFullPath()
      // val fileContent = getFileContent()
      val fileContent = FilemanManager.getFileContent(filemanUniqueId) ?: ""
      val filemanCommand = getFilemanCommand()
      val deltaTime = System.currentTimeMillis() - initTime
      
      
      
      val outputData: Data = Data.Builder()
        .putIsFinalWorker(true)
        .putFilemanCommand(filemanCommand)
        .putEllapsedTime(deltaTime)
        .putFilemanUniqueID(filemanUniqueId)
        .putFileFullPath(fileFullPath)
        // .putFileContent(fileContent)  //will be caught in File Manager DTO
        .build()
      FilemanManager.putFileContent(filemanUniqueId!!, fileContent)
      Result.success(outputData)
    }catch (e:InterruptedException){
      FilemanLogger.dbc("${::FinalWorker.name} - ${e.message}")
      Result.retry()
    }
  }
  
  override fun onStopped() {
    super.onStopped()
    FilemanLogger.d("${::FinalWorker.name} stopped")
  }
  
  
}