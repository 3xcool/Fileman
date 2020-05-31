package com.andrefilgs.fileman.workmanager

import android.content.Context
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.andrefilgs.fileman.auxiliar.FilemanAux
import com.andrefilgs.fileman.auxiliar.FilemanInternalConstants
import com.andrefilgs.fileman.auxiliar.FilemanLogger
import com.andrefilgs.fileman.enums.FilemanCommands
import com.andrefilgs.fileman.model.FilemanFeedback
import com.andrefilgs.fileman.enums.FilemanStatus
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


class FilemanWM(context: Context, private val viewlifeCycleOwner: LifecycleOwner, private val statusFeedBackLevel:FilemanStatus?=FilemanStatus.IDLE) : BaseViewModel() {
  
  private var workManager: WorkManager = WorkManager.getInstance(context)
  
  private val _filemanWorkFeedback = MutableLiveData<WorkInfo>()
  
  
  /**
   * User will observe this LiveData
   */
  val filemanFeedback: LiveData<FilemanFeedback> = Transformations.switchMap(_filemanWorkFeedback) { filemanWorkFeedback ->
    if(FilemanFeedback.convertWorkStateToFilemanStatus(filemanWorkFeedback).type >= statusFeedBackLevel!!.type){
      MutableLiveData<FilemanFeedback>(FilemanFeedback.buildFromWorkInfo(filemanWorkFeedback))
    }else{
      null
    }
  }
  

  
  private val observeWorkById = Observer<WorkInfo> { workInfo ->
    if (workInfo == null) return@Observer
    _filemanWorkFeedback.value = workInfo
  }
  
  private fun observeWorkerById(id: UUID) {
    workManager.getWorkInfoByIdLiveData(id).observe(viewlifeCycleOwner, observeWorkById)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun writeLaunch(fileContent: String, context: Context, drive: Int, folder: String, filename: String, append: Boolean, withTimeout:Boolean?=true, timeout:Long? = FilemanInternalConstants.TIMEOUT): FilemanFeedback {
    return runCommandLaunch(FilemanCommands.WRITE, fileContent, context, drive, folder, filename, append, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun readLaunch(context: Context, drive: Int, folder: String, filename: String, withTimeout:Boolean?=true, timeout:Long? = FilemanInternalConstants.TIMEOUT): FilemanFeedback {
    return runCommandLaunch(FilemanCommands.READ, null, context, drive, folder, filename, null, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun deleteLaunch(context: Context, drive: Int, folder: String, filename: String, withTimeout:Boolean?=true, timeout:Long? = FilemanInternalConstants.TIMEOUT): FilemanFeedback {
    return runCommandLaunch(FilemanCommands.DELETE, null, context, drive, folder, filename, null, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  private fun runCommandLaunch(filemanCommand: FilemanCommands, fileContent: String?, context: Context, drive: Int, folder: String, filename: String, append: Boolean?, withTimeout:Boolean?=true, timeout:Long? = FilemanInternalConstants.TIMEOUT): FilemanFeedback {
    val filenameFullPath = FilemanAux.getFilenameFullPath(context, drive, folder, filename)
      ?: return FilemanFeedback(FilemanStatus.FAILED.type, FilemanStatus.FAILED.name, "Fileman FullPath error. Probably driver not found", filemanCommand.name)
    var output = FilemanFeedback()
    viewModelScope.launch {
      output = try {
        val now = FilemanAux.getNow()
        val filemanUniqueId = WorkRequestFactory.generateFilemanUniqueId(filenameFullPath, now)
        val writeWorkerRequest = WorkRequestFactory.buildFilemanWorkerRequest(filemanCommand.name, now, filemanUniqueId, filemanUniqueId, fileContent, drive, folder, filename, append, withTimeout!!, timeout!!)
        val finalWorkerRequest = WorkRequestFactory.buildFinalWorker(now, filemanCommand.name, filemanUniqueId, filenameFullPath, fileContent)
        observeWorkerById(writeWorkerRequest.id)
        observeWorkerById(finalWorkerRequest.id)
        workManager.beginWith(writeWorkerRequest).then(finalWorkerRequest).enqueue()
        FilemanFeedback(FilemanStatus.ENQUEUE.type,FilemanStatus.ENQUEUE.name, "Running ${::writeLaunch}", filemanCommand.name, filemanUniqueId, finalWorkerRequest.id,true,  filenameFullPath, fileContent)
      } catch (e: Exception) {
        FilemanLogger.dbc("${::writeLaunch.name} - ${e.message}")
        FilemanFeedback(FilemanStatus.FAILED.type, FilemanStatus.FAILED.name, e.message, filemanCommand.name, null, null, null, filenameFullPath, fileContent)
      }
    }
    return output
  }
  

  
  
  // /**
  //  * @param drive Sandbox / Internal / External
  //  * @param fileName wih extension e.g. ".json"
  //  */
  // fun writeAsync(fileContent: String, context: Context, drive: Int, folder: String, fileName: String, append: Boolean, withTimeout:Boolean?=true, timeout:Long? = FilemanInternalConstants.TIMEOUT): FilemanFeedback = runBlocking {
  //   val res = viewModelScope.async {
  //     return@async try {
  //       val filenameFullPath = FilemanAux.getFullPath(context, drive, folder)
  //       val now = FilemanAux.getNow()
  //       val filemanUniqueId = WorkRequestFactory.generateFilemanUniqueId(filenameFullPath!!, now)
  //       val writeWorkerRequest = WorkRequestFactory.buildFilemanWorkerRequest(FilemanCommands.WRITE.name, now, filemanUniqueId, filemanUniqueId, fileContent, drive, folder, fileName, append, withTimeout!!, timeout!!) //using the uniqueId as Tag
  //       val finalWorkerRequest = WorkRequestFactory.buildFinalWorker(now,FilemanCommands.WRITE.name, filemanUniqueId, filenameFullPath, fileContent)
  //       observeWorkerById(writeWorkerRequest.id)
  //       observeWorkerById(finalWorkerRequest.id)
  //       workManager.beginWith(writeWorkerRequest).then(finalWorkerRequest).enqueue()
  //       FilemanFeedback(FilemanStatus.ENQUEUE.type, FilemanStatus.ENQUEUE.name, "Running ${::writeAsync}", FilemanCommands.WRITE.name, filemanUniqueId,  finalWorkerRequest.id,true, filenameFullPath, fileContent)
  //     } catch (e: Exception) {
  //       FilemanLogger.dbc("${::writeAsync.name} - ${e.message}")
  //       FilemanFeedback(FilemanStatus.FAILED.type, FilemanStatus.FAILED.name, e.message, FilemanCommands.WRITE.name, null, null, null, null, fileContent)
  //     }
  //   }.await()
  //   res
  // }
  
  
  fun cancelAllWorks() {
    workManager.cancelAllWork()
  }
  
}