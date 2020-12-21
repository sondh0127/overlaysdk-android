package com.example.overlay_sdk

import android.app.AlertDialog
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException


class MainActivity : AppCompatActivity() {

    class InteractiveManager {
        val overlays = null

        fun initializeApp(
            baseURL: String,
            webView: WebView,
            controlHeight: String,
            fullscreen: Boolean
        ) {

        }
    }

    class Dice(private val numSides: Int) {
        var sides = numSides

        fun roll(): Int {
            return (1..numSides).random()
        }
    }


    private val client = OkHttpClient()
    private val moshi = Moshi.Builder().build()

    private val gistJsonAdapter = moshi.adapter(Gist::class.java)


    fun handleOnCreated() {
        val request = Request.Builder()
            .url("https://api.github.com/gists/c2a7c39532239ff261be")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val gist = gistJsonAdapter.fromJson(response.body!!.source())

            for ((key, value) in gist!!.files!!) {
                println(key)
                println(value.content)
            }
        }
    }


    @JsonClass(generateAdapter = true)
    data class Gist(var files: Map<String, GistFile>?)

    @JsonClass(generateAdapter = true)
    data class GistFile(var content: String?)


    class AndroidJSInterface(private val context: Context) {
        var mContext = context
        @JavascriptInterface
        fun showToast(toastMessage: String) {
            println("Help button clicked")
            val toast = Toast.makeText(context, toastMessage, Toast.LENGTH_LONG)

            toast.show()
        }

        @JavascriptInterface
        fun openAndroidDialog() {
            val myDialog = AlertDialog.Builder(context)
            myDialog.setTitle("DANGER!");
            myDialog.setMessage("You can do what you want!");
            myDialog.setPositiveButton("ON", null);
            myDialog.show();
        }
    }

    private fun loadJs(webView: WebView) {
        webView.loadUrl(
            """javascript:(function f() {
                var btnInput = document.getElementById('input-web');
                btnInput.setAttribute('value', "valuevalue");
              })()"""
        )
    }

    private lateinit var webView: WebView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val SDK_INT = Build.VERSION.SDK_INT
        if (SDK_INT > 8) {
            val policy = StrictMode.ThreadPolicy.Builder()
                .permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val rollButton: Button = findViewById(R.id.button)


            rollButton.setOnClickListener {
//                handleOnCreated()
                roolDice()
            }

            val videoView: VideoView = findViewById(R.id.videoView)


            val mediaController = MediaController(this)
            mediaController.setAnchorView(videoView)

            val onlineUri = Uri.parse(
                "https://r8---sn-42u-i5ols.googlevideo.com/videoplayback?expire=1608487103&ei=XzzfX56KHoSds8IPofCcyAw&ip=59.153.249.59&id=o-AJAgiw0R1ZxuwUny49uGl0qjJBxaIrN_cBsIDOYENP5S&itag=136&aitags=133%2C134%2C135%2C136%2C137%2C160%2C242%2C243%2C244%2C247%2C248%2C271%2C278%2C313&source=youtube&requiressl=yes&vprv=1&mime=video%2Fmp4&ns=xUbVhdIxCNUx21w_yS7MnKEF&gir=yes&clen=13687727&dur=236.249&lmt=1608039955795444&fvip=3&keepalive=yes&c=WEB&txp=5532432&n=CGR--l6GKRvPi8V0F&sparams=expire%2Cei%2Cip%2Cid%2Caitags%2Csource%2Crequiressl%2Cvprv%2Cmime%2Cns%2Cgir%2Cclen%2Cdur%2Clmt&sig=AOq0QJ8wRgIhALH9-bCzqpI7lAfPm5FlxP_aCTWbXXok9y4wlVgCElozAiEA8IM5e41blJyxJnGR274UUR0UlfcRXu5lOdxXpchtrkg%3D&title=SOOBIN%2B-%2BTH%C3%81NG%2BN%C4%82M%2B(Official%2BMusic%2BVideo)&cms_redirect=yes&mh=iO&mip=113.22.140.68&mm=31&mn=sn-42u-i5ols&ms=au&mt=1608474284&mv=m&mvi=8&pcm2cms=yes&pl=24&lsparams=mh,mip,mm,mn,ms,mv,mvi,pcm2cms,pl&lsig=AG3C_xAwRAIgaPbzKCKiwXcPRrhkA7o0A6kkIkpWGUpAj778caTWqKACIC2ZeIK6LLF8Ih6HyrtitW_p-vVtGqobUYZMzHwDvtYA"
            )
            val offlineUri = Uri.parse("android.resource://$packageName/${R.raw.videoplayback}")

            videoView.setMediaController(mediaController)
            videoView.setVideoURI(offlineUri)
            videoView.requestFocus()
            videoView.start()

            webView = findViewById(R.id.webView)

            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView, url: String) {
                    loadJs(view)
                }
            }

            webView.settings.javaScriptEnabled = true

            webView.addJavascriptInterface(AndroidJSInterface(this), "Android")


            webView.loadUrl("file:///android_res/raw/index2.html")
        }
    }




    private fun roolDice() {
        val toast = Toast.makeText(this, "Roll dices", Toast.LENGTH_LONG)

        toast.show()
        val dice = Dice(6)
        val diceRoll = dice.roll()
        val diceImage: ImageView = findViewById(R.id.imageView)

        val drawableResource = when (diceRoll) {
            1 -> R.drawable.dice_1
            2 -> R.drawable.dice_2
            3 -> R.drawable.dice_3
            4 -> R.drawable.dice_4
            5 -> R.drawable.dice_5
            else -> R.drawable.dice_6
        }

        diceImage.setImageResource(drawableResource)
        diceImage.contentDescription = diceRoll.toString()

    }
}