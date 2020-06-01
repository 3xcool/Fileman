package com.andrefilgs.fileman.auxiliar

import android.util.Log

object FilemanLogger {
  
  private var showlog = true
  
  fun enableLog(){
    showlog = true
  }
  
  fun disableLog(){
    showlog = false
  }
  
  private var LOG_TAG = "Fileman"
  
  fun getFilemanLogTag():String = LOG_TAG
  
  fun setFilemanLogTag(tag:String){
    LOG_TAG = tag
  }
  
  internal fun d(message:String, tag:String?= LOG_TAG){
    if(showlog) Log.d(tag, message)
  }
  
  internal fun dbc(message:String, tag:String?= LOG_TAG){
    if(showlog) Log.d(tag,"Design By Contract -> $message")
  }
  
}