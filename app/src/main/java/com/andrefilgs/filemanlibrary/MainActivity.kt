package com.andrefilgs.filemanlibrary

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.andrefilgs.fileman.enums.FilemanDrivers
import com.andrefilgs.fileman.Fileman
import com.andrefilgs.fileman.auxiliar.orDefault
import com.andrefilgs.fileman.enums.FilemanCommands
import com.andrefilgs.fileman.enums.FilemanStatus
import com.andrefilgs.fileman.model.FilemanFeedback
import com.andrefilgs.fileman.workmanager.FilemanWM
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {
  
  private lateinit var filemanWM: FilemanWM
  
  override val coroutineContext: CoroutineContext = Dispatchers.Main
  
  // private var mDrive = FilemanDrivers.SandBox.type   // 0 = store at SandBox
  private var mDrive = FilemanDrivers.Internal.type    // 1 = store at Internal device storage
  // private var mDrive = FilemanDrivers.External.type  // 2 = store at External device storage (SD card)
  
  private var mFolder = "MyFolder"
  private var mFilename = ""
  private var mFileContent = ""
  private var mAppend = false
  
  
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    // filemanWM = FilemanWM(context = this, viewlifeCycleOwner = this, statusFeedBackLevel =  FilemanStatus.IDLE)  //you can set which level to show, IDLE is the Default
    filemanWM = FilemanWM(context = this, viewlifeCycleOwner = this)
    dismissLoading()
    setClickListeners()
    
    filemanWM.filemanFeedback.observe(this, Observer { output ->
      updateText(output)
    })
  }
  
  
  private fun updateText(filemanFeedback: FilemanFeedback) {
    val current = tv_output.text
    
    tv_output.text = "$current\n===============\nID: ${filemanFeedback.filemanId}\nState: ${filemanFeedback.statusName}\nMessage: ${filemanFeedback.message}\nProgress: ${filemanFeedback.progressMessage}\nIs Final: ${filemanFeedback.isFinalWorker}"
    
    if (filemanFeedback.status.orDefault(-1) > FilemanStatus.SUCCEEDED.type) {
      dismissLoading()
    } else if (filemanFeedback.isFinalWorker.orDefault() && filemanFeedback.status.orDefault(-1) >= FilemanStatus.SUCCEEDED.type) {
      dismissLoading()
    }
  }
  
  override fun onDestroy() {
    super.onDestroy()
  }
  
  
  private fun showLoading() {
    progressBar.visibility = View.VISIBLE
  }
  
  private fun dismissLoading() {
    progressBar.visibility = View.GONE
  }
  
  private fun setClickListeners() {
    btn_clear_output.setOnClickListener {
      tv_output.text = ""
      dismissLoading()
      filemanWM.cancelAllWorks()
    }
    
    btn_delete_file.setOnClickListener {
      showLoading()
      collectInput()
      val res = runFilemanCommandAsync(FilemanCommands.DELETE)
      tv_output.text = "Observe final worker ID: ${res?.workerId}"
    }
    
    btn_write.setOnClickListener {
      showLoading()
      collectInput()
      // writePerson(createDummyPerson())
      val res = runFilemanCommandAsync(FilemanCommands.WRITE)
      tv_output.text = "Observe final worker ID: ${res?.workerId}"
    }
  
    btn_read.setOnClickListener {
      showLoading()
      collectInput()
      val res = runFilemanCommandAsync(FilemanCommands.READ)
      tv_output.text = "Observe final worker ID: ${res?.workerId}"
    }
  
    
    
    
    // btn_read.setOnClickListener {
    //   showLoading()
    //   launch(Dispatchers.Default) {
    //     val res = withContext(Dispatchers.IO) { readPerson("Andre") }
    //     withContext(context = Dispatchers.Main) {
    //       tv_output.text = res ?: "File does not exist"
    //       dismissLoading()
    //     }
    //   }
    //   tv_output.text = "reading..."
    // }
    
    
  }
  
  
  private fun collectInput() {
    mFilename = et_filename.text.toString()
    mFileContent = et_file_content.text.toString()
    mAppend = checkBox.isChecked
  }
  
  private fun runFilemanCommandAsync(command: FilemanCommands): FilemanFeedback? {
    val timeout = 5000L
    return when (command) {
      FilemanCommands.WRITE  -> filemanWM.writeLaunch(fileContent = mFileContent, context = this, drive = mDrive, folder = mFolder, filename = mFilename, append = mAppend, withTimeout = true, timeout = timeout)
      FilemanCommands.READ   -> filemanWM.readLaunch(context = this, drive = mDrive, folder = mFolder, filename = mFilename, withTimeout = true, timeout = timeout)
      FilemanCommands.DELETE -> filemanWM.deleteLaunch(context = this, drive = mDrive, folder = mFolder, filename = mFilename, withTimeout = true, timeout = timeout)
      else                   -> null
    }
    
  }
  
  
  private fun createDummyPerson(): Person {
    return Person("John", "Doe")
  }
  
  private fun createPerson(name: String, surname: String): Person {
    return Person(name, surname)
  }
  
  /**
   * Synchronously
   */
  private fun writePerson(person: Person): Boolean {
    val personJson = Gson().toJson(person, Person::class.java)
    return Fileman.write(personJson, this, mDrive, mFolder, person.name, append = false)
  }
  
  private fun readPerson(personName: String? = "John"): String? {
    Thread.sleep(2000) //just to show the power of coroutine
    return Fileman.read(this, mDrive, mFolder, personName!!)
  }
  
  
  /**
   * Asynchronously with WorkManager and Coroutine
   */
  private fun writePersonWM(person: Person): FilemanFeedback {
    val personJson = Gson().toJson(person, Person::class.java)
    return filemanWM.writeLaunch(personJson, this, mDrive, mFolder, person.name, append = false, withTimeout = true, timeout = 5000L)
  }
  
  
}
