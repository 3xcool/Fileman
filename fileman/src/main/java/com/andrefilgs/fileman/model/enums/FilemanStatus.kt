package com.andrefilgs.fileman.model.enums


enum class FilemanStatus(val type:Int) {
  IDLE(-1),
  BLOCKED(10),
  ENQUEUE(20),
  RUNNING(30),
  // WARNING(40),
  SUCCEEDED(100),
  FAILED(200),
  CANCELLED(300),
}