package com.andrefilgs.filemanlibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.andrefilgs.fileman.Fileman
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class MainActivity : AppCompatActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext = Dispatchers.Main

    //    private var mDrive = Fileman.DRIVE_SB   // 0 = store at SandBox
    private var mDrive = Fileman.DRIVE_SDI    // 1 = store at Internal device storage
//    private var mDrive = Fileman.DRIVE_SDE  // 2 = store at External device storage (SD card)

    private var mFolder = "MyFolder"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setClickListeners()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    @ExperimentalCoroutinesApi
    private fun setClickListeners() {

        //This way blocks the UI
//    btn_sample_write.setOnClickListener {
//      val res = writePerson(createDummyPerson())
//      tv_sample.text = if (res) "File write with success" else "File have not been written"
//    }

//    btn_sample_read.setOnClickListener {
//      tv_sample.text = readPerson() ?: "File does not exist"
//    }


        //With Coroutine
        btn_sample_write.setOnClickListener {
            //            writeAsyncPerson(createDummyPerson()) //without return

            //with return and coroutine
            GlobalScope.launch {
                val res = withContext(Dispatchers.IO) {
                    writePerson(createDummyPerson())
                }
                Dispatchers.Main{
                    tv_sample.text = if (res) "File written with success" else "File have not been written"
                }
            }
        }



        btn_sample_read.setOnClickListener {
            GlobalScope.launch {
                val res = withContext(Dispatchers.IO) { readPerson() }
                withContext(context = Dispatchers.Main) {
                    tv_sample.text = res ?: "File does not exist"
                }
            }
            tv_sample.text = "reading..."
        }
    }


    private fun createDummyPerson(): Person {
        return Person("John", "Doe")
    }

    //using simple lib methods
    private fun writePerson(person: Person): Boolean {
        val jsonString = Gson().toJson(person, Person::class.java)
        return Fileman.write(jsonString, this, mDrive, mFolder, person.name + Fileman.JSON_EXTENSION, false, true)
    }

    private fun readPerson(): String? {
        Thread.sleep(3000) //just to show that UI is not blocked
        return Fileman.read(this, mDrive, mFolder, "John" + Fileman.JSON_EXTENSION)
    }


    /**
     * Without result
     */
    private fun writeAsyncPerson(person: Person) {
        val jsonString = Gson().toJson(person, Person::class.java)
        Fileman.writeAsync(jsonString, this, mDrive, mFolder, person.name + Fileman.JSON_EXTENSION, false, true)
    }



}
