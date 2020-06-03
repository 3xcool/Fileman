# Fileman
This is a utility API for File Management.

If you don't need to use **WorkManager + Coroutine** for file CRUD, check FilemanLite repository first.

I did this Library because I had a problem with multi threading writing/reading which I needed to guarantee the log chronology and to keep the job working even when the app is not running anymore.


# Dependency

In build.graddle (Project)
```kotlin
  allprojects {
    repositories {
        google()
        jcenter()
        maven { url 'https://jitpack.io' }
    }
  }
```

    
In build.graddle (app)
```kotlin
dependencies {
implementation 'com.github.3xcool:fileman:$LATEST_VERSION'
}
```

# Drivers

Use FilemanDrivers enum class:
1) **SandBox:** Where the app is installed (can't be accessed by other apps)
2) **Internal:** Device storage (can be accessed by other apps)
3) **External:** SD Card if available

# Simple CRUD

Don't call these functions on the Main Thread to avoid UI freeze.

```kotlin
Fileman.write(fileContent: String, context: Context, drive: Int, folder: String, filename: String, append: Boolean)
```

```kotlin
Fileman.read(context: Context, drive: Int, folder: String, filename: String)
```

```kotlin
Fileman.delete(context: Context, drive: Int, folder: String, filename: String)
```

# WorkManager (advanced)

For WorkManager use FilemanWM class instead.

### Create an object

```kotlin
filemanWM = FilemanWM(context, viewLifecycleOwner)
```

### Call Launch or Async CRUD functions 
Obs: Launch and Async are Coroutine nomenclatures, see Kotlin Coroutine to see the difference.
```kotlin
val res = filemanWM.writeLaunch("filecontent", context, drive, folder, filename, append = true, withTimeout = true, timeout = 10000L)
//return a FilemanFeedback Class.
```

### Return
Get the return from filemanFeedback and errorFeedback LiveData.

```kotlin
   filemanWM.filemanFeedback.observe(this, Observer { output ->
      updateText(output)
    })
    
    filemanWM.errorFeedback.observe(this, Observer { output ->
      updateText(output)
    })
```

### Some WorkManager utility fun

cancelWorkById(), cancelAllWorks() and pruneWork()
