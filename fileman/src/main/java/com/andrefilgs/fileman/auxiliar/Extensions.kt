package com.andrefilgs.fileman.auxiliar

import androidx.work.Data
import androidx.work.ListenableWorker
import androidx.work.WorkInfo

//Getters
fun ListenableWorker.getInitTime() = this.inputData.getLong(FilemanInternalConstants.WORK_KEY_INIT_TIME, -1)
fun ListenableWorker.getFilemanCommand() = this.inputData.getString(FilemanInternalConstants.WORK_KEY_FILEMAN_COMMAND)
fun ListenableWorker.getFilemanUniqueId() = this.inputData.getString(FilemanInternalConstants.WORK_KEY_FILEMAN_UNIQUE_ID)
fun ListenableWorker.getFileFullPath() = this.inputData.getString(FilemanInternalConstants.WORK_KEY_FILE_FULL_PATH)
fun ListenableWorker.getFileContent() = this.inputData.getString(FilemanInternalConstants.WORK_KEY_FILE_CONTENT)
fun ListenableWorker.getDrive() = this.inputData.getInt(FilemanInternalConstants.WORK_KEY_DRIVE,-1)
fun ListenableWorker.getFolder() = this.inputData.getString(FilemanInternalConstants.WORK_KEY_FOLDER)
fun ListenableWorker.getFilename() = this.inputData.getString(FilemanInternalConstants.WORK_KEY_FILENAME)
fun ListenableWorker.getAppend() = this.inputData.getBoolean(FilemanInternalConstants.WORK_KEY_APPEND, true)
fun ListenableWorker.getWithTimeout() = this.inputData.getBoolean(FilemanInternalConstants.WORK_KEY_WITH_TIMEOUT, true)
fun ListenableWorker.getTimeout() = this.inputData.getLong(FilemanInternalConstants.WORK_KEY_TIMEOUT, FilemanInternalConstants.TIMEOUT)






//Getters
fun WorkInfo.getOutput(): String? = this.outputData.getString(FilemanInternalConstants.WORK_KEY_OUTPUT)
fun WorkInfo.getFilemanId(): String? = this.outputData.getString(FilemanInternalConstants.WORK_KEY_FILEMAN_UNIQUE_ID)
fun WorkInfo.getProgressValue(): Int? = this.progress.getInt(FilemanInternalConstants.WORK_KEY_PROGRESS, 0)
fun WorkInfo.getProgressMessage(): String? = this.progress.getString(FilemanInternalConstants.WORK_KEY_PROGRESS_MESSAGE)
fun WorkInfo.isFinalWork(): Boolean? = this.outputData.getBoolean(FilemanInternalConstants.WORK_KEY_IS_FINAL_WORKER, false)
fun WorkInfo.getInitTime(): Long? = this.outputData.getLong(FilemanInternalConstants.WORK_KEY_INIT_TIME,-1)
fun WorkInfo.getEllapsedTime(): Long? = this.outputData.getLong(FilemanInternalConstants.WORK_KEY_ELLAPSED_TIME,-1)
fun WorkInfo.getFilename(): String? = this.outputData.getString(FilemanInternalConstants.WORK_KEY_FILENAME)
fun WorkInfo.getFileContent(): String? = this.outputData.getString(FilemanInternalConstants.WORK_KEY_FILE_CONTENT)
fun WorkInfo.getDrive(): String? = this.outputData.getString(FilemanInternalConstants.WORK_KEY_DRIVE)
fun WorkInfo.getFolder(): String? = this.outputData.getString(FilemanInternalConstants.WORK_KEY_FOLDER)
fun WorkInfo.getFullPath(): String? = this.outputData.getString(FilemanInternalConstants.WORK_KEY_FILE_FULL_PATH)
fun WorkInfo.getFilemanCommand(): String? = this.outputData.getString(FilemanInternalConstants.WORK_KEY_FILEMAN_COMMAND)
fun WorkInfo.getWithTimeout(): Boolean? = this.outputData.getBoolean(FilemanInternalConstants.WORK_KEY_WITH_TIMEOUT, true)
fun WorkInfo.getTimeout(): Long? = this.outputData.getLong(FilemanInternalConstants.WORK_KEY_TIMEOUT, FilemanInternalConstants.TIMEOUT)

//Setters
fun Data.Builder.putOutputFeedback(outputMessage: String, filemanCommand:String?, filemanId:String?) = this.putOutput(outputMessage).putFilemanCommand(filemanCommand).putFilemanUniqueID(filemanId)

fun Data.Builder.putOutput(value: String) = this.putString(FilemanInternalConstants.WORK_KEY_OUTPUT, value)
fun Data.Builder.putProgressMessage(value: String) = this.putString(FilemanInternalConstants.WORK_KEY_PROGRESS_MESSAGE, value)
fun Data.Builder.putProgressValue(value: Int) = this.putInt(FilemanInternalConstants.WORK_KEY_PROGRESS, value)
fun Data.Builder.putFilemanCommand(value: String?) = this.putString(FilemanInternalConstants.WORK_KEY_FILEMAN_COMMAND, value)
fun Data.Builder.putFileFullPath(value: String) = this.putString(FilemanInternalConstants.WORK_KEY_FILE_FULL_PATH, value)
fun Data.Builder.putInitTime(value: Long) = this.putLong(FilemanInternalConstants.WORK_KEY_INIT_TIME, value)
fun Data.Builder.putFilemanUniqueID(value: String?) = this.putString(FilemanInternalConstants.WORK_KEY_FILEMAN_UNIQUE_ID, value)
fun Data.Builder.putFileContent(value: String?) = this.putString(FilemanInternalConstants.WORK_KEY_FILE_CONTENT, value)
fun Data.Builder.putDrive(value: Int) = this.putInt(FilemanInternalConstants.WORK_KEY_DRIVE, value)
fun Data.Builder.putFolder(value: String) = this.putString(FilemanInternalConstants.WORK_KEY_FOLDER, value)
fun Data.Builder.putFilename(value: String) = this.putString(FilemanInternalConstants.WORK_KEY_FILENAME, value)
fun Data.Builder.putAppend(value: Boolean?) = this.putBoolean(FilemanInternalConstants.WORK_KEY_APPEND, value ?: true)
fun Data.Builder.putWithTimeout(withTimeout: Boolean) = this.putBoolean(FilemanInternalConstants.WORK_KEY_WITH_TIMEOUT, withTimeout)
fun Data.Builder.putTimeout(timeout: Long) = this.putLong(FilemanInternalConstants.WORK_KEY_TIMEOUT, timeout)






fun Boolean?.orDefault(default: Boolean = false): Boolean {
  if (this == null)
    return default
  return this
}

fun Int?.orDefault(default: Int = 0): Int {
  if (this == null)
    return 0
  return this
}

