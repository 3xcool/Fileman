package com.andrefilgs.fileman.workmanager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andrefilgs.fileman.auxiliar.FilemanLogger
import kotlinx.coroutines.*
import kotlin.reflect.KSuspendFunction1

open class BaseViewModel : ViewModel() {


  private val _baseErrorMessage = MutableLiveData<String>()
  val baseErrorMessage: LiveData<String> = _baseErrorMessage

  private val _baseMessage = MutableLiveData<String>()
  val baseMessage: LiveData<String> = _baseMessage

  private var _baseLoading = MutableLiveData<Boolean>(false)
  var baseLoading: LiveData<Boolean> = _baseLoading

  val baseParentJob: Job = SupervisorJob()  //when you cancel the children you don't kill this job


  val coroutineExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
    FilemanLogger.d( "Coroutine exception: ${throwable.message}")

    _baseErrorMessage.postValue("ERROR: ${throwable.message}")
    _baseLoading.postValue(false)

    GlobalScope.launch { println("Caught ${throwable.printStackTrace()}") }
  }

  
  val baseCoroutineScope = CoroutineScope(Dispatchers.Main + baseParentJob + coroutineExceptionHandler)

  suspend fun baseDelaySomeTime(delayTime: Long) {
    delay(delayTime)
  }
  
  suspend fun baseShowFeedbackMessage(message: String?) {
    _baseMessage.value = message
    message?.let { FilemanLogger.d(message) }
  }

  suspend fun baseStartSomeCommand(message: String? = null, logAsTitle: Boolean? = true) {
    FilemanLogger.d("Show loading")
    message?.let { baseShowFeedbackMessage(it) }
    _baseLoading.value = true
  }
  
  suspend fun baseFinishSomeCommand(message: String? = null) {
    FilemanLogger.d("Dismiss loading")
    message?.let { baseShowFeedbackMessage(it) }
    baseDismissLoading()
  }
  
  fun baseShowLoading(){
    _baseLoading.value = true
  }
  
  fun baseDismissLoading(){
    _baseLoading.value = false
  }
  
  
  
  /**
   * wrap method in coroutine
   */
  fun baseCoroMessage(text:String, someMethod: KSuspendFunction1<String?, Unit>){
    baseCoroutineScope.launch {
      someMethod(text)
    }
  }


  fun stop() {
    baseParentJob.cancelChildren()
  }
}