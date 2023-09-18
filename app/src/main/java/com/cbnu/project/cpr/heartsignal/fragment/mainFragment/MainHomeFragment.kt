package com.cbnu.project.cpr.heartsignal.fragment.mainFragment

import android.graphics.SurfaceTexture
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import com.cbnu.project.cpr.heartsignal.R
import com.cbnu.project.cpr.heartsignal.databinding.FragmentMainHomeBinding

class MainHomeFragment : Fragment() {

    private lateinit var homeTextureView: TextureView
    private var _binding: FragmentMainHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var mediaPlayer: MediaPlayer


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMainHomeBinding.inflate(inflater, container, false)
        val view = binding.root

        homeTextureView = binding.homeVideoTextureView
        initVideoPlayer()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initVideoPlayer() {
        // 동영상 파일의 경로 또는 URL을 설정
        val videoUri = Uri.parse("android.resource://${requireContext().packageName}/raw/homevideo")

        // MediaController를 사용하여 컨트롤러 추가 (선택 사항)
        val mediaController = MediaController(requireContext())
        mediaController.setAnchorView(homeTextureView)

        // TextureView에 SurfaceTextureListener 설정
        homeTextureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                startVideoPlayback(videoUri, surface)
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                mediaPlayer.release()
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    private fun startVideoPlayback(videoUri: Uri, surface: SurfaceTexture) {
        mediaPlayer = MediaPlayer()
        mediaPlayer.setSurface(Surface(surface))
        mediaPlayer.setDataSource(requireContext(), videoUri)
        mediaPlayer.prepare()
        mediaPlayer.start()
        mediaPlayer.setOnCompletionListener { mp ->
            // 동영상 재생이 끝나면 필요한 작업 수행
            mp.release()
            homeTextureView.visibility = View.GONE
        }
    }


}