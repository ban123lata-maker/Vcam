package com.example.videocamerasource

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Rational
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.ui.PlayerView
import com.example.videocamerasource.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var lastUri: Uri? = null

    private val pickVideo = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            lastUri = uri
            getSharedPreferences("app", MODE_PRIVATE).edit()
                .putString("last_uri", uri.toString()).apply()
            PlayerManager.getOrCreate(this)
            PlayerManager.setVideo(uri)
            binding.playerView.player = PlayerManager.player
            binding.tvStatus.text = "Dang phat lap: $uri"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val player = PlayerManager.getOrCreate(this)
        binding.playerView.player = player

        val saved = getSharedPreferences("app", MODE_PRIVATE)
            .getString("last_uri", null)
        if (saved != null) {
            try {
                val uri = Uri.parse(saved)
                lastUri = uri
                PlayerManager.setVideo(uri)
                binding.tvStatus.text = "Khoi phuc video gan nhat"
            } catch (_: Exception) {}
        }

        binding.btnPick.setOnClickListener {
            pickVideo.launch(arrayOf("video/mp4", "video/*"))
        }
        binding.btnPlayPause.setOnClickListener {
            val p = PlayerManager.player ?: return@setOnClickListener
            if (p.isPlaying) p.pause() else p.play()
        }
        binding.btnMute.setOnClickListener {
            val p = PlayerManager.player ?: return@setOnClickListener
            p.volume = if (p.volume > 0f) 0f else 1f
        }
        binding.btnFloat.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val it = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(it)
                Toast.makeText(this, "Hay cap quyen Hien thi tren ung dung khac roi bam lai", Toast.LENGTH_LONG).show()
            } else {
                startService(Intent(this, FloatingService::class.java))
                Toast.makeText(this, "Da bat cua so noi, nhan Home de dung app khac", Toast.LENGTH_SHORT).show()
            }
        }
        binding.btnPip.setOnClickListener { enterPip() }
    }

    private fun enterPip() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
                val params = PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
                enterPictureInPictureMode(params)
            } else {
                Toast.makeText(this, "May khong ho tro PiP", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Tu dong PiP khi nhan Home neu dang phat
        if (PlayerManager.player?.isPlaying == true) {
            try { enterPip() } catch (_: Exception) {}
        }
    }

    override fun onStop() {
        super.onStop()
        (binding.playerView as PlayerView).player = null
    }

    override fun onRestart() {
        super.onRestart()
        binding.playerView.player = PlayerManager.player
    }
}
