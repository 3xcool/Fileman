package com.andrefilgs.fileman.workmanager.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.andrefilgs.fileman.*
import com.andrefilgs.fileman.auxiliar.*
import com.andrefilgs.fileman.auxiliar.FilemanAux
import com.andrefilgs.fileman.auxiliar.FilemanInternalConstants
import com.andrefilgs.fileman.enums.FilemanCommands
import com.andrefilgs.fileman.enums.FilemanDrivers
import com.andrefilgs.fileman.model.FilemanFeedback
import com.andrefilgs.fileman.workmanager.FilemanManager
import kotlinx.coroutines.*
import java.lang.Exception

class CoroutineFilemanWorker(val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
  
  private fun validateInputData(filenameFullPath: String?, content: String?, drive: Int?, folder: String?, filename: String?): Boolean {
    return (filenameFullPath != null && content != null && drive != null && drive <= FilemanDrivers.values().size && folder != null && filename != null)
  }
  
  
  override suspend fun doWork(): Result = coroutineScope {
    val initTime = getInitTime()
    val filemanCommand = getFilemanCommand()
    val filemanUniqueId = getFilemanUniqueId()
    val fileContent = getFileContent() ?: ""
    val drive = getDrive()
    val folder = getFolder() ?: FilemanInternalConstants.DEFAULT_FILEMAN_FOLDER
    val fileName = getFilename() ?: FilemanInternalConstants.DEFAULT_FILEMAN_FILE
    val append = getAppend()
    val withTimeout = getWithTimeout()
    val timeout = if (withTimeout) getTimeout() else Long.MAX_VALUE
    val filenameFullPath = FilemanAux.getFilenameFullPath(context, drive, folder, fileName)
    
    //by default is Dispatchers.Default
    withContext(Dispatchers.IO) {
      if (!validateInputData(filenameFullPath, fileContent, drive, folder, fileName) || filenameFullPath == null) {
        FilemanLogger.dbc("${::CoroutineFilemanWorker.name} - Invalid input data")
        return@withContext Result.failure()
      }
      
      try {
        val waitFileReadyJob = async {
          val timeoutResult = withTimeoutOrNull(timeout) {
            while (!FilemanManager.isFileAvailable(filenameFullPath)) {
              FilemanLogger.d("$filenameFullPath is not available")
              setProgressAsync(Data.Builder().putProgressMessage("$filenameFullPath is blocked. Fileman is waiting...").build())
              delay(FilemanInternalConstants.TIMEOUT_STEP)
            }
            "Ok" //if timeout triggers, this will get cancelled before it produces this result and will return null
          }
          FilemanLogger.d("Timeout result is $timeoutResult")
        }
        
        waitFileReadyJob.join()
        
        if (!FilemanManager.isFileAvailable(filenameFullPath)) {
          FilemanLogger.dbc("$filenameFullPath not available after $timeout reached")
          val outputData = Data.Builder().putOutputFeedback("$filenameFullPath not available after $timeout reached", filemanCommand, filemanUniqueId ?: "").build()
          return@withContext Result.failure(outputData)
        }
        
        var commandResult: Pair<Boolean, String?>? = Pair(false, null)
        val writingJob = async {
          commandResult = withTimeoutOrNull(timeout) {
            FilemanLogger.d("$filemanCommand at file $fileName the following:\n$fileContent")
            FilemanManager.lockFile(filenameFullPath)
            val res = when (filemanCommand) {
              FilemanCommands.WRITE.name  -> Pair(Fileman.write(fileContent, context, drive, folder, fileName, append), fileContent)
              FilemanCommands.DELETE.name -> Pair(Fileman.delete(context, drive, folder, fileName), null)
              FilemanCommands.READ.name   -> {
                val fileContentRead = Fileman.read(context, drive, folder, fileName)
                Pair(fileContentRead != null, fileContentRead)
              }
              else                        -> Pair(false, null)
            }
            FilemanManager.unlockFile(filenameFullPath)
            res
          }
          FilemanLogger.d("$filemanCommand result is $commandResult")
        }
        writingJob.join()
        
        val deltaTime = System.currentTimeMillis() - initTime
        val outputMessage = FilemanFeedback.buildOutputMessage(commandResult?.first.orDefault(false), filemanCommand, filenameFullPath, fileName, commandResult?.second, deltaTime)
        filemanUniqueId?.let { FilemanManager.outputMessage[it] = outputMessage }  //this will be caught in FinalWorker
        val outputData = Data.Builder().putOutputFeedback(outputMessage, filemanCommand, filemanUniqueId).build()
        FilemanLogger.d(outputMessage)
        
        if (!commandResult?.first.orDefault(false)) {
          return@withContext Result.failure(outputData)
        }
        
        Result.success(outputData)
      } catch (e: Exception) {
        FilemanLogger.dbc("ERROR at Command: $filemanCommand\nFile: \"$filenameFullPath\"\n due to ${e.message}")
        val outputData = Data.Builder().putOutputFeedback("ERROR at Command: $filemanCommand\nFile: \"$filenameFullPath\"\n due to ${e.message}", filemanCommand, filemanUniqueId).build()
        Result.failure(outputData)
      }
    }
  }
  
  
}