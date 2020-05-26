package com.andrefilgs.filemanlibrary

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.andrefilgs.fileman.Fileman
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

//https://medium.com/@elizarov/the-reason-to-avoid-globalscope-835337445abc
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
            GlobalScope.launch {
                //        val res = async {  coroWritePerson(createDummyPerson())}.await()
                val res = withContext(Dispatchers.IO) {
                    writePerson(createDummyPerson())
                }
                Dispatchers.Main {
                    tv_sample.text =
                        if (res) "File written with success" else "File have not been written"
                }
            }
        }


        btn_sample_read.setOnClickListener {

            launch(Dispatchers.Default){
                val res = withContext(Dispatchers.IO) { readPerson() }
                withContext(context = Dispatchers.Main){
                    tv_sample.text = res ?: "File does not exist"
                }
            }
            tv_sample.text = "reading..."


//            GlobalScope.launch {
//                //        val res = async {  coroReadPerson()}.await()
//                val res = withContext(Dispatchers.IO) { readPerson() }
//                withContext(context = Dispatchers.Main){
//                    tv_sample.text = res ?: "File does not exist"
//                }
//            }
//            tv_sample.text = "reading..."

        }


        //Simple Blocking Coroutine example
        btn_sample_sync_coro.setOnClickListener {

            launch(Dispatchers.IO){
                val response = checkValue(2)
                withContext(Dispatchers.Main){
                    tv_sample.text = response
                }
            }
            tv_sample.text = "reading Sync test..."
        }

    }

    private fun checkValue(value: Int): String {
        return "$value is greater than 2?\nResult = ${runBlocking {
            syncCoroutine(value)
        }}"
    }

    suspend fun syncCoroutine(sampleText: Int): Boolean = coroutineScope {
        Thread.sleep(3000) //just to show the power of coroutine
        sampleText > 2
    }



    private fun createDummyPerson(): Person {
        return Person("John", "Doe")
    }

    private fun writePerson(person: Person): Boolean {
        val jsonString = Gson().toJson(person, Person::class.java)
        return Fileman.write(
            jsonString,
            this,
            mDrive,
            mFolder,
            person.name + Fileman.JSON_EXTENSION,
            false,
            true
        )
    }

    private fun readPerson(): String? {
        Thread.sleep(3000) //just to show the power of coroutine
        return Fileman.read(this, mDrive, mFolder, "John" + Fileman.JSON_EXTENSION)
    }
}