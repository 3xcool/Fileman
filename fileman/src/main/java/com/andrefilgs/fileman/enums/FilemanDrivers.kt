package com.andrefilgs.fileman.enums

enum class FilemanDrivers (val type:Int){
  SandBox(0),  //Where app is installed
  Internal(1), //Internal Device storage
  External(2)  //External Device storage (SD card)
}