package com.podmev.cashsplitter.fragment

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.podmev.cashsplitter.R
import com.podmev.cashsplitter.databinding.FragmentAboutBinding
import com.podmev.cashsplitter.utils.getProjectVersionCode
import com.podmev.cashsplitter.utils.getProjectVersionName

//TODO If Ania make icons and design, i need to put her on page
//TODO add FAQ
class AboutFragment : Fragment() {
    companion object {
        private const val logTag = "AboutFragment"
    }

    private var _binding: FragmentAboutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        try {
            _binding = FragmentAboutBinding.inflate(inflater, container, false)
            binding.textViewVersion.text = getVersionText()
            binding.textViewDevelopedBy.text = String.format(
                resources.getString(R.string.textView_developed_by_text),
                resources.getString(R.string.developer_name)
            )
            //next line is needed for clickable link in textView
            binding.textViewReviewAndComments.movementMethod = LinkMovementMethod.getInstance()
            return binding.root
        } catch (e: Throwable) {
            Log.e(logTag, "onCreateView failed: ${e.javaClass}, ${e.message}")
            e.printStackTrace()
            return inflater.inflate(R.layout.fragment_main, container, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getVersionText(): String =
        String.format(
            resources.getString(R.string.textView_version),
            getProjectVersionName(requireContext()),
            getProjectVersionCode(requireContext()).toString()
        )

}