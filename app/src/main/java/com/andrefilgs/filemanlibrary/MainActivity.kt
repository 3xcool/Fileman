package com.andrefilgs.filemanlibrary

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.andrefilgs.fileman.Fileman
import com.andrefilgs.fileman.FilemanDrivers
import com.andrefilgs.fileman.auxiliar.FilemanLogger
import com.andrefilgs.fileman.auxiliar.orDefault
import com.andrefilgs.fileman.model.FilemanFeedback
import com.andrefilgs.fileman.model.enums.FilemanCommands
import com.andrefilgs.fileman.model.enums.FilemanStatus
import com.andrefilgs.fileman.workmanager.FilemanWM
import kotlinx.android.synthetic.main.activity_main.*


//This is the simplest example for using Fileman Library (without using third libraries). Don't use it as an example for a good architecture solution.
class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
  
  private val SYNC = "Sync"
  private val ASYNC = "Async"
  private val LAUNCH = "Launch"
  
  private val launchModeOptions = listOf<String>(SYNC, ASYNC, LAUNCH)
  
  private lateinit var filemanWM: FilemanWM
  
  // private var mDrive = FilemanDrivers.SandBox.type   // 0 = store at SandBox
  private var mDrive = FilemanDrivers.Internal.type    // 1 = store at Internal device storage
  // private var mDrive = FilemanDrivers.External.type  // 2 = store at External device storage (SD card)
  
  private var mFolder = "MyFolder"
  private var mFilename = ""
  private var mFileContent = ""
  private var mAppend = false
  private var mTimeout = 5000L  //general
  private var mTimeout2 = 5000L //for multi thread only
  
  // private var mAsync = true
  private var mLaunchModeIndex = launchModeOptions.indexOf("Launch") //use Launch
  
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // FilemanLogger.disableLog() //for production
    FilemanLogger.enableLog() //for development
    FilemanLogger.setFilemanLogTag(FilemanLogger.getFilemanLogTag() + "myTAG") //to override FilemanLogger TAG
    
    // filemanWM = FilemanWM(context = this, viewlifeCycleOwner = this, statusFeedBackLevel =  FilemanStatus.IDLE)       //you can set which level you want to see at filemanFeedback Observer, IDLE is the Default
    // filemanWM = FilemanWM(context = this, viewlifeCycleOwner = this, statusFeedBackLevel =  FilemanStatus.SUCCEEDED)  //only SUCCEEDED, FAILURE or CANCELLED will be triggered
    filemanWM = FilemanWM(context = this, viewlifeCycleOwner = this)
    dismissLoading()
    setClickListeners()
    setSpinner()
    setSeekBars()
    
    filemanWM.filemanFeedback.observe(this, Observer { output ->
      updateText(output)
    })
    
    filemanWM.errorFeedback.observe(this, Observer { output ->
      updateText(output)
    })
  }
  
  private fun setSpinner() {
    val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_expandable_list_item_1, launchModeOptions)
    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    spinner_run_mode.adapter = arrayAdapter
    spinner_run_mode.onItemSelectedListener = this
  }
  
  
  private fun setSeekBars() {
    tv_timeout_value.text = buildTimeoutValuesMessage (seekBar_timeout.progress, seekBar_timeout2.progress )
    seekBar_timeout?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
      }
      
      override fun onStartTrackingTouch(seek: SeekBar) {
      }
      
      override fun onStopTrackingTouch(seek: SeekBar) {
        tv_timeout_value.text = buildTimeoutValuesMessage (seekBar_timeout.progress, seekBar_timeout2.progress )
        mTimeout = convertSeekToTimeout(seekBar_timeout.progress)
      }
    })
  
    seekBar_timeout2?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
      override fun onProgressChanged(seek: SeekBar, progress: Int, fromUser: Boolean) {
      }
    
      override fun onStartTrackingTouch(seek: SeekBar) {
      }
    
      override fun onStopTrackingTouch(seek: SeekBar) {
        tv_timeout_value.text = buildTimeoutValuesMessage (seekBar_timeout.progress, seekBar_timeout2.progress )
        mTimeout2 = convertSeekToTimeout(seekBar_timeout2.progress)
      }
    })
  }
  
  private fun buildTimeoutValuesMessage(timeout1:Int, timeout2:Int):String{
    val status = if( timeout1 > timeout2) "Will not write" else "Will write"
    return "${convertSeekToTimeout(seekBar_timeout.progress)/1000} seconds / ${convertSeekToTimeout(seekBar_timeout2.progress)/1000} seconds.$status"
  }
  
  
  private fun updateText(filemanFeedback: FilemanFeedback) {
    val current = tv_output.text
    
    val workerId = filemanFeedback.workerId
    val filemanId = filemanFeedback.filemanId
    val statusName = filemanFeedback.statusName
    val message = filemanFeedback.message
    val progressMessage = filemanFeedback.progressMessage
    val isFinalWorker = filemanFeedback.isFinalWorker
    
    tv_output.text = "$current\n===============\nWorkerId: ${filemanFeedback.workerId}\nFilemanID: ${filemanFeedback.filemanId}\nState: ${filemanFeedback.statusName}\nMessage: ${filemanFeedback.message}\nProgress: ${filemanFeedback.progressMessage}\nIs Final: ${filemanFeedback.isFinalWorker}"
    
    if (filemanFeedback.status.orDefault(-1) > FilemanStatus.SUCCEEDED.type) {
      dismissLoading()
    } else if (filemanFeedback.isFinalWorker.orDefault() && filemanFeedback.status.orDefault(-1) >= FilemanStatus.SUCCEEDED.type) {
      dismissLoading()
    }
  }
  
  private fun setOutputMessage(message: String?) {
    tv_output.text = message
  }
  
  
  private fun showLoading() {
    progressBar.visibility = View.VISIBLE
  }
  
  private fun dismissLoading() {
    progressBar.visibility = View.GONE
  }
  
  private fun setClickListeners() {
    btn_clear_output.setOnClickListener {
      setOutputMessage("")
      dismissLoading()
      filemanWM.cancelAllWorks()
    }
    
    btn_delete_file.setOnClickListener {
      beginCommand()
      runFilemanCommand(FilemanCommands.DELETE)
    }
    
    btn_write.setOnClickListener {
      beginCommand()
      runFilemanCommand(FilemanCommands.WRITE)
    }
    
    btn_write_large.setOnClickListener {
      beginCommand()
      
      //One test
      // for(i in 0..30){
      //   runFilemanCommand(FilemanCommands.WRITE)
      // }
      
      //Second test
      mFileContent = buildLargeFileContent()
      runFilemanCommand(FilemanCommands.WRITE)
    }
    
    btn_read.setOnClickListener {
      beginCommand()
      runFilemanCommand(FilemanCommands.READ)
    }
    
    btn_multi_thread_write.setOnClickListener {
      beginCommand()
      if(mLaunchModeIndex == launchModeOptions.indexOf(SYNC) ){
        setOutputMessage("Command mode must be $ASYNC or $LAUNCH")
        dismissLoading()
        return@setOnClickListener
      }
      Fileman.getFilenameFullPath(this, mDrive, mFolder, mFilename)?.let {
        runFilemanCommand(FilemanCommands.WRITE)
        filemanWM.lockFileWithTimeout(it, mTimeout) //assuming the first write takes longer
        mFileContent = "\n***************\nAnother Write:\n$mFileContent\n***************"
        runFilemanCommand(FilemanCommands.WRITE, mTimeout2)  //mTimeout2 < mTimeout will not write and mTimeout2 > mTimeout will write
      }
    }
    
    btn_lock_file.setOnClickListener {
      collectInput()
      Fileman.getFilenameFullPath(this, mDrive, mFolder, mFilename)?.let {
        if(mTimeout == 0L){
          filemanWM.lockFile(it)
          setOutputMessage("File $mFilename is locked")
        }else{
          filemanWM.lockFileWithTimeout(it, mTimeout)
          setOutputMessage("File $mFilename is locked with timeout $mTimeout")
        }
        
      }
    }
    
    btn_unlock_file.setOnClickListener {
      collectInput()
      Fileman.getFilenameFullPath(this, mDrive, mFolder, mFilename)?.let {
        filemanWM.unlockFile(it)
        setOutputMessage("File $mFilename is unlocked")
      }
    }
  }
  
  private fun beginCommand() {
    showLoading()
    collectInput()
  }
  
  //Use DataBinding
  private fun collectInput() {
    mFilename = et_filename.text.toString()
    mFileContent = et_file_content.text.toString()
    mAppend = checkbox_append.isChecked
    mLaunchModeIndex = spinner_run_mode.selectedItemPosition
    mTimeout = convertSeekToTimeout(seekBar_timeout.progress)
  }
  
  private fun runFilemanCommand(filemanCommand: FilemanCommands, timeout:Long?=mTimeout) {
    when (filemanCommand) {
      FilemanCommands.WRITE  -> write(timeout)
      FilemanCommands.READ   -> read()
      FilemanCommands.DELETE -> delete()
    }
  }
  
  private fun write(timeout: Long?) {
    when (mLaunchModeIndex) {
      launchModeOptions.indexOf(SYNC)   -> {
        val res = Fileman.write(fileContent = mFileContent, context = this, drive = mDrive, folder = mFolder, filename = mFilename, append = mAppend)
        setOutputMessage("Write command success -> $res")
        dismissLoading()
      }
      launchModeOptions.indexOf(ASYNC)  -> {
        val res = filemanWM.writeAsync(fileContent = mFileContent, context = this, drive = mDrive, folder = mFolder, filename = mFilename, append = mAppend, withTimeout = true, timeout = timeout?: mTimeout)
        setOutputMessage("Status: ${res.statusName}\nMessage: ${res.message}\nID: ${res.workerId}") //This is not the final result, just to check if request is fine
        if (res.status == null || res.status!! > FilemanStatus.SUCCEEDED.type) dismissLoading()     //Dismiss Loading in case of Failure or Cancelled
      }
      launchModeOptions.indexOf(LAUNCH) -> {
        val res = filemanWM.writeLaunch(fileContent = mFileContent, context = this, drive = mDrive, folder = mFolder, filename = mFilename, append = mAppend, withTimeout = true, timeout = timeout ?: mTimeout)
        setOutputMessage("Observe final worker ID: ${res.workerId}")
      }
    }
  }
  
  private fun read() {
    when (mLaunchModeIndex) {
      launchModeOptions.indexOf(SYNC)   -> {
        val fileContent = Fileman.read(context = this, drive = mDrive, folder = mFolder, filename = mFilename)
        setOutputMessage("$mFilename content is:\n$fileContent")
        dismissLoading()
      }
      launchModeOptions.indexOf(ASYNC)  -> {
        val res = filemanWM.readAsync(context = this, drive = mDrive, folder = mFolder, filename = mFilename, withTimeout = true, timeout = mTimeout)
        setOutputMessage("Status: ${res.statusName}\nMessage: ${res.message}\nID: ${res.workerId}") //This is not the final result, just to check if request is fine
        if (res.status == null || res.status!! > FilemanStatus.SUCCEEDED.type) dismissLoading() //dismiss Loading in case of Failure or Cancelled
      }
      launchModeOptions.indexOf(LAUNCH) -> {
        val res = filemanWM.readLaunch(context = this, drive = mDrive, folder = mFolder, filename = mFilename, withTimeout = true, timeout = mTimeout)
        setOutputMessage("Observe final worker ID: ${res.workerId}")
      }
    }
  }
  
  private fun delete() {
    when (mLaunchModeIndex) {
      launchModeOptions.indexOf(SYNC)   -> {
        val res = Fileman.delete(context = this, drive = mDrive, folder = mFolder, filename = mFilename)
        setOutputMessage("Delete command success -> $res")
        dismissLoading()
      }
      launchModeOptions.indexOf(ASYNC)  -> {
        val res = filemanWM.deleteAsync(context = this, drive = mDrive, folder = mFolder, filename = mFilename, withTimeout = true, timeout = mTimeout)
        setOutputMessage("Status: ${res.statusName}\nMessage: ${res.message}\nID: ${res.workerId}") //This is not the final result, just to check if request is fine
        if (res.status == null || res.status!! > FilemanStatus.SUCCEEDED.type) dismissLoading()  //dismiss Loading in case of Failure or Cancelled
      }
      launchModeOptions.indexOf(LAUNCH) -> {
        val res = filemanWM.deleteLaunch(context = this, drive = mDrive, folder = mFolder, filename = mFilename, withTimeout = true, timeout = mTimeout)
        setOutputMessage("Observe final worker ID: ${res.workerId}")
      }
    }
  }
  
  
  private fun buildLargeFileContent(): String {
    val sbDummyLargeText = StringBuffer()
    for (i in 0..10000) {
      sbDummyLargeText.append("Lorem ipsum $i\n")
    }
    return sbDummyLargeText.toString()
  }
  
  override fun onNothingSelected(p0: AdapterView<*>?) {
  }
  
  override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, p3: Long) {
  }
  
  
  private fun convertSeekToTimeout(progress:Int):Long{
    return if(progress == 0) Long.MAX_VALUE else progress * 1000L
  }
}
