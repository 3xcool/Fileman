package com.andrefilgs.fileman.workmanager

import android.content.Context
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.andrefilgs.fileman.Fileman
import com.andrefilgs.fileman.auxiliar.FilemanConstants
import com.andrefilgs.fileman.auxiliar.FilemanLogger
import com.andrefilgs.fileman.model.FilemanFeedback
import com.andrefilgs.fileman.model.enums.FilemanCommands
import com.andrefilgs.fileman.model.enums.FilemanStatus
import com.andrefilgs.fileman.workmanager.workers.WorkRequestFactory
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*


/**
 * @param viewlifeCycleOwner may be null because you may not want to observe WorkManager LiveData
 */
class FilemanWM(context: Context, private val viewlifeCycleOwner: LifecycleOwner?=null, private val statusFeedBackLevel:FilemanStatus?=FilemanStatus.IDLE) : BaseViewModel() {
  
  private var workManager: WorkManager = WorkManager.getInstance(context)
  
  private val _filemanWorkFeedback = MutableLiveData<WorkInfo>()
  
  val errorFeedback: MutableLiveData<FilemanFeedback> = MutableLiveData()
  
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
    viewlifeCycleOwner?.let { workManager.getWorkInfoByIdLiveData(id).observe(viewlifeCycleOwner, observeWorkById) }
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun writeLaunch(fileContent: String, context: Context, drive: Int, folder: String, filename: String, append: Boolean, withTimeout:Boolean?=true, timeout:Long? = FilemanConstants.TIMEOUT): FilemanFeedback {
    return runCommandLaunch(FilemanCommands.WRITE, fileContent, context, drive, folder, filename, append, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun readLaunch(context: Context, drive: Int, folder: String, filename: String, withTimeout:Boolean?=true, timeout:Long? = FilemanConstants.TIMEOUT): FilemanFeedback {
    return runCommandLaunch(FilemanCommands.READ, null, context, drive, folder, filename, null, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun deleteLaunch(context: Context, drive: Int, folder: String, filename: String, withTimeout:Boolean?=true, timeout:Long? = FilemanConstants.TIMEOUT): FilemanFeedback {
    return runCommandLaunch(FilemanCommands.DELETE, null, context, drive, folder, filename, null, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  private fun runCommandLaunch(filemanCommand: FilemanCommands, fileContent: String?, context: Context, drive: Int, folder: String, filename: String, append: Boolean?, withTimeout:Boolean?=true, timeout:Long? = FilemanConstants.TIMEOUT): FilemanFeedback {
    val filenameFullPath = Fileman.getFilenameFullPath(context, drive, folder, filename)
      ?: return FilemanFeedback(FilemanStatus.FAILED.type, FilemanStatus.FAILED.name, "Fileman FullPath error. Probably driver not found", filemanCommand.name)
    var output = FilemanFeedback()
    viewModelScope.launch {
      output = try {
        val now = Fileman.getNow()
        val filemanUniqueId = WorkRequestFactory.generateFilemanUniqueId(filenameFullPath, now)
        val coroutineWorkerRequest = WorkRequestFactory.buildFilemanWorkerRequest(filemanCommand.name, now, filemanUniqueId, filemanUniqueId, fileContent, drive, folder, filename, append, withTimeout!!, timeout!!)
        val finalWorkerRequest = WorkRequestFactory.buildFinalWorker(now, filemanCommand.name, filemanUniqueId, filenameFullPath, fileContent)
        observeWorkerById(coroutineWorkerRequest.id)
        observeWorkerById(finalWorkerRequest.id)
        workManager.beginUniqueWork(filemanUniqueId, ExistingWorkPolicy.REPLACE, coroutineWorkerRequest).then(finalWorkerRequest).enqueue()
        FilemanFeedback(FilemanStatus.ENQUEUE.type,FilemanStatus.ENQUEUE.name, "Running $filemanCommand", filemanCommand.name, filemanUniqueId, finalWorkerRequest.id,true,  filenameFullPath, fileContent)
      } catch (e: Exception) {
        FilemanLogger.dbc("${::runCommandLaunch.name} - ${e.message}")
        val filemanFeedback = FilemanFeedback(FilemanStatus.FAILED.type, FilemanStatus.FAILED.name, e.message, filemanCommand.name, null, null, null, filenameFullPath, fileContent)
        errorFeedback.value = filemanFeedback
        filemanFeedback
      }
    }
    return output
  }
  
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun writeAsync(fileContent: String, context: Context, drive: Int, folder: String, filename: String, append: Boolean, withTimeout:Boolean?=true, timeout:Long? = FilemanConstants.TIMEOUT): FilemanFeedback {
    return runCommandAsync(FilemanCommands.WRITE, fileContent, context, drive, folder, filename, append, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun readAsync(context: Context, drive: Int, folder: String, filename: String, withTimeout:Boolean?=true, timeout:Long? = FilemanConstants.TIMEOUT): FilemanFeedback {
    return runCommandAsync(FilemanCommands.READ, null, context, drive, folder, filename, null, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  fun deleteAsync(context: Context, drive: Int, folder: String, filename: String, withTimeout:Boolean?=true, timeout:Long? = FilemanConstants.TIMEOUT): FilemanFeedback {
    return runCommandAsync(FilemanCommands.DELETE, null, context, drive, folder, filename, null, withTimeout, timeout)
  }
  
  /**
   * @param drive Sandbox / Internal / External
   * @param filename wih extension e.g. ".json"
   */
  private fun runCommandAsync(filemanCommand: FilemanCommands,fileContent: String?, context: Context, drive: Int, folder: String, filename: String, append: Boolean?, withTimeout:Boolean?=true, timeout:Long? = FilemanConstants.TIMEOUT): FilemanFeedback = runBlocking {
    
    val res = viewModelScope.async {
      return@async try {
        val filenameFullPath = Fileman.getFullPath(context, drive, folder)
        val now = Fileman.getNow()
        val filemanUniqueId = WorkRequestFactory.generateFilemanUniqueId(filenameFullPath!!, now)
        val coroutineWorkerRequest = WorkRequestFactory.buildFilemanWorkerRequest(filemanCommand.name, now, filemanUniqueId, filemanUniqueId, fileContent, drive, folder, filename, append, withTimeout!!, timeout!!) //using the uniqueId as Tag
        val finalWorkerRequest = WorkRequestFactory.buildFinalWorker(now,filemanCommand.name, filemanUniqueId, filenameFullPath, fileContent)
        observeWorkerById(coroutineWorkerRequest.id)
        observeWorkerById(finalWorkerRequest.id)
        workManager.beginUniqueWork(filemanUniqueId, ExistingWorkPolicy.REPLACE, coroutineWorkerRequest).then(finalWorkerRequest).enqueue()
        FilemanFeedback(FilemanStatus.ENQUEUE.type, FilemanStatus.ENQUEUE.name, "Running ${::runCommandAsync}", filemanCommand.name, filemanUniqueId,  finalWorkerRequest.id,true, filenameFullPath, fileContent)
      } catch (e: Exception) {
        FilemanLogger.dbc("${::runCommandAsync.name} - ${e.message}")
        FilemanFeedback(FilemanStatus.FAILED.type, FilemanStatus.FAILED.name, e.message, filemanCommand.name, null, null, null, null, fileContent)
      }
    }.await()
    res
  }
  
  
  
  fun cancelWorkById(workerId:UUID) {
    workManager.cancelWorkById(workerId)
  }
  
  fun pruneWork() {
    workManager.pruneWork()
  }
  
  fun cancelAllWorks() {
    workManager.cancelAllWork()
  }
  
  
  fun lockFile(filenameFullPath: String){
    FilemanManager.filesManager[filenameFullPath] = false
  }
  
  fun unlockFile(filenameFullPath: String){
    FilemanManager.filesManager[filenameFullPath] = true
  }
  
  fun lockFileWithTimeout(filenameFullPath: String, timeout: Long? = FilemanConstants.TIMEOUT){
    viewModelScope.launch {
      lockFile(filenameFullPath)
      delay(timeout!!)
      unlockFile(filenameFullPath)
    }
  }
}