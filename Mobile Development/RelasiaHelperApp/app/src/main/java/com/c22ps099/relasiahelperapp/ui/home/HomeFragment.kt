package com.c22ps099.relasiahelperapp.ui.home

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.c22ps099.relasiahelperapp.R
import com.c22ps099.relasiahelperapp.adapter.LoadingStateAdapter
import com.c22ps099.relasiahelperapp.adapter.MissionListAdapter
import com.c22ps099.relasiahelperapp.data.MissionRepository
import com.c22ps099.relasiahelperapp.databinding.FragmentHomeBinding
import com.c22ps099.relasiahelperapp.network.ApiConfig
import com.c22ps099.relasiahelperapp.ui.MissionFactory
import com.c22ps099.relasiahelperapp.ui.login.LoginFragment
import com.c22ps099.relasiahelperapp.ui.profile.ProfileFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var binding: FragmentHomeBinding? = null
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel = ViewModelProvider(
            this, MissionFactory(
                MissionRepository(
                    ApiConfig.getApiService()
                )
            )
        )[HomeViewModel::class.java]

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) homeViewModel.checkVolunteer(auth.currentUser?.uid.toString())
        homeViewModel.isSuccess.observe(viewLifecycleOwner) { success ->
            if (!success) {
                showProfileDialog()
            }
        }
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        val firebaseUser = auth.currentUser

        if (firebaseUser == null) {
            val navigateAction = HomeFragmentDirections
                .actionHomeFragmentToLoginFragment()
            findNavController().navigate(navigateAction)

            val mLoginFragment = LoginFragment()
            val mFragmentManager = parentFragmentManager
            mFragmentManager.beginTransaction().apply {
                replace(
                    R.id.nav_host_fragment,
                    mLoginFragment,
                    LoginFragment::class.java.simpleName
                )
                setReorderingAllowed(true)
                commit()
            }
        }

        val adapter = MissionListAdapter()
        binding?.apply {
            svHome.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
                android.widget.SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    if (query != null) {
                        binding?.rvMissions?.scrollToPosition(0)
//                        homeViewModel.searchMission(query)
                        svHome.clearFocus()
                    }
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }

            })
            btnNotification.setOnClickListener {
                val navigateAction = HomeFragmentDirections
                    .actionHomeFragmentToProfileFragment()
                findNavController().navigate(navigateAction)

                val mProfileFragment = ProfileFragment()
                val mFragmentManager = parentFragmentManager
                mFragmentManager.beginTransaction().apply {
                    replace(
                        R.id.nav_host_fragment,
                        mProfileFragment,
                        ProfileFragment::class.java.simpleName
                    )
                    addToBackStack(null)
                    setReorderingAllowed(true)
                    commit()
                }
            }
            rvMissions.setHasFixedSize(true)
            rvMissions.itemAnimator = null
            rvMissions.layoutManager = LinearLayoutManager(requireContext())
            rvMissions.adapter = adapter.withLoadStateFooter(
                footer = LoadingStateAdapter {
                    adapter.retry()
                }
            )

            homeViewModel.missions.observe(viewLifecycleOwner) {
                adapter.submitData(lifecycle, it)
            }

            adapter.addLoadStateListener { loadState ->
                binding.apply {
                    progressBar.isVisible = loadState.source.refresh is LoadState.Loading
                    rvMissions.isVisible = loadState.source.refresh is LoadState.NotLoading
                    buttonRetry.isVisible = loadState.source.refresh is LoadState.Error
                    textViewError.isVisible = loadState.source.refresh is LoadState.Error

                    if (loadState.source.refresh is LoadState.NotLoading &&
                        loadState.append.endOfPaginationReached &&
                        adapter.itemCount < 1
                    ) {
                        rvMissions.isVisible = false
                        textViewEmpty.isVisible = true
                    } else {
                        textViewEmpty.isVisible = false
                    }
                }
            }
        }
    }

    private fun showProfileDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_fill_profile)
        dialog.show()
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        val btnProfile = dialog.findViewById<Button>(R.id.btn_profile)
        btnProfile.setOnClickListener {
            dialog.dismiss()
            val navigateAction = HomeFragmentDirections
                .actionHomeFragmentToProfileFragment()
            findNavController().navigate(navigateAction)

            val mProfileFragment = ProfileFragment()
            val mFragmentManager = parentFragmentManager
            mFragmentManager.beginTransaction().apply {
                replace(
                    R.id.nav_host_fragment,
                    mProfileFragment,
                    ProfileFragment::class.java.simpleName
                )
                addToBackStack(null)
                setReorderingAllowed(true)
                commit()
            }
            dialog.hide()
        }
    }
}