package com.andrefilgs.fileman.auxiliar

internal object FilemanInternalConstants {
  
  internal val TIMEOUT = 30000L
  internal val TIMEOUT_STEP = 1000L
  internal val TIMEOUT_MAX_STEPS =( TIMEOUT / TIMEOUT_STEP).toInt()
  
  var LOG_TAG = "Fileman"
  
  const val DEFAULT_FILEMAN_FOLDER = "Fileman_Folder"
  const val DEFAULT_FILEMAN_FILE = "Fileman_File"
  
  const val WORK_TAG_WRITE = "WORK_TAG_WRITE"
  const val WORK_TAG_FINAL = "WORK_TAG_FINAL"
  
  const val WORK_KEY_FILEMAN_UNIQUE_ID = "WORK_KEY_FILEMAN_UNIQUE_ID" //Will be FileFullPath + Timestamp
  const val WORK_KEY_FILEMAN_COMMAND = "WORK_KEY_FILEMAN_COMMAND"
  const val WORK_KEY_IS_FINAL_WORKER = "WORK_KEY_IS_FINAL_WORKER"
  const val WORK_KEY_OUTPUT = "WORK_KEY_OUTPUT"
  
  const val WORK_KEY_PROGRESS = "WORK_KEY_PROGRESS"
  const val WORK_KEY_PROGRESS_MESSAGE = "WORK_KEY_PROGRESS_MESSAGE"
  const val WORK_KEY_INIT_TIME = "WORK_KEY_INIT_TIME"
  const val WORK_KEY_ELLAPSED_TIME = "WORK_KEY_ELLAPSED_TIME"
  const val WORK_KEY_FILENAME = "WORK_KEY_FILENAME"
  const val WORK_KEY_FILE_CONTENT = "WORK_KEY_FILE_CONTENT"
  const val WORK_KEY_DRIVE = "WORK_KEY_DRIVE"
  const val WORK_KEY_FOLDER = "WORK_KEY_FOLDER"
  const val WORK_KEY_APPEND = "WORK_KEY_APPEND"
  const val WORK_KEY_WITH_TIMEOUT = "WORK_KEY_WITH_TIMEOUT"
  const val WORK_KEY_TIMEOUT = "WORK_KEY_TIMEOUT"
  const val WORK_KEY_FILE_FULL_PATH = "WORK_KEY_FILE_FULL_PATH"
  
  
}
