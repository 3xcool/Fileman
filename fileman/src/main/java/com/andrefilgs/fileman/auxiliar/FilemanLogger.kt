package com.andrefilgs.fileman.auxiliar

import android.util.Log

object FilemanLogger {
  
  var showlog = true //todo 1000 set false
  
  private var LOG_TAG = "Fileman"
  
  fun getFilemanLogTag():String = LOG_TAG
  
  fun setFilemanLogTag(tag:String){
    LOG_TAG = tag
  }
  
  fun d(message:String, tag:String?= LOG_TAG){
    if(showlog) Log.d(tag, message)
  }
  
  fun dbc(message:String, tag:String?= LOG_TAG){
    if(showlog) Log.d(tag,"Design By Contract -> $message")
  }
  
}