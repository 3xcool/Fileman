package com.andrefilgs.fileman.workmanager.workers

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.andrefilgs.fileman.auxiliar.*
import com.andrefilgs.fileman.auxiliar.FilemanInternalConstants
import com.andrefilgs.fileman.workmanager.FilemanManager


class FinalWorker(appContext: Context,workerParams: WorkerParameters)
  : Worker(appContext, workerParams) {
  
  override fun doWork(): Result {
    return try {
      val initTime = getInitTime()
      val filemanUniqueId = getFilemanUniqueId()
      val fullPath = getFileFullPath()
      val fileContent = getFileContent()
      val filemanCommand = getFilemanCommand()
      val deltaTime = System.currentTimeMillis() - initTime
      
      
      
      val outputData: Data = Data.Builder()
        .putBoolean(FilemanInternalConstants.WORK_KEY_IS_FINAL_WORKER, true)
        .putString(FilemanInternalConstants.WORK_KEY_OUTPUT, FilemanManager.outputMessage[filemanUniqueId] ?: "") //getting outputmessage if exists
        .putString(FilemanInternalConstants.WORK_KEY_FILEMAN_COMMAND, filemanCommand)
        .putLong(FilemanInternalConstants.WORK_KEY_ELLAPSED_TIME, deltaTime)
        .putString(FilemanInternalConstants.WORK_KEY_FILEMAN_UNIQUE_ID, filemanUniqueId)
        .putString(FilemanInternalConstants.WORK_KEY_FILE_FULL_PATH, fullPath)
        .putString(FilemanInternalConstants.WORK_KEY_FILE_CONTENT, fileContent)
        .build()
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