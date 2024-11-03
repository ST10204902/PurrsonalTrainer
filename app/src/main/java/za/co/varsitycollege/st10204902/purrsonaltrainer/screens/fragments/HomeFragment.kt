package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments

import android.Manifest
import android.content.ComponentName
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.MonthsAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.RoutineListAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.WorkoutsAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.MonthWorkout
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.workout_activities.StartEmptyWorkoutActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.workout_activities.StartWorkoutActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.NotificationService
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val LOCATION_PERMISSION_REQUEST_CODE = 1001

class HomeFragment : Fragment() {

    private lateinit var routinesRecyclerView: RecyclerView
    private lateinit var monthsAdapter: MonthsAdapter
    private var monthWorkoutList: List<MonthWorkout> = listOf()
    private lateinit var topSection: LinearLayout
//---------------------------------PROBLEM AREA---------------------------------//
private val requiredPermissions = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    // Remove or adjust any invalid permissions
    // Manifest.permission.POST_NOTIFICATIONS, // Include only if targeting Android 13+
)

    fun checkAndRequestPermissions() {
        try {
            val permissionsToRequest = requiredPermissions.filter {
                ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
            }

            if (permissionsToRequest.isNotEmpty()) {
                requestPermissions(
                    permissionsToRequest.toTypedArray(),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            } else {
                Log.d(TAG, "All permissions already granted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking permissions: ${e.message}")
        }
    }



    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted, proceed with functionality
                } else {
                    // Permissions denied, handle accordingly
                    Toast.makeText(requireContext(), "Permissions denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
//---------------------------------PROBLEM AREA---------------------------------//

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Request notification permission on Android 13+
       checkAndRequestPermissions()
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }
        })

        routinesRecyclerView = view.findViewById(R.id.routinesRecyclerView)
        topSection = view.findViewById<LinearLayout>(R.id.topSection)


        applyFloatUpAnimation(topSection)
        applyFloatUpAnimation(routinesRecyclerView)

        routinesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        // Initialize MonthsAdapter with empty list and click listener
        monthsAdapter = MonthsAdapter(monthWorkoutList, { workout ->
            // Handle workout item click
            val bundle = Bundle()
            bundle.putString("WorkoutID", workout.workoutID) // Correct key
            navigateTo(requireContext(), StartEmptyWorkoutActivity::class.java, bundle)
        }, requireContext())
        routinesRecyclerView.adapter = monthsAdapter

        val addButton: AppCompatImageButton = view.findViewById(R.id.add_button)
        addButton.setOnClickListener {
            val intent = Intent(activity, StartWorkoutActivity::class.java)
            startActivity(intent)
        }

        // Load and group workouts by month
        loadMonthWorkouts()

        // Setup current workout
        setupCurrentWorkout(view)
    }

    private fun setupCurrentWorkout(view: View)
    {
        val isWorkoutInProgress = UserManager.getWorkoutInProgress() != null

        if (isWorkoutInProgress)
        {
            val currentWorkout = UserManager.getWorkoutInProgress()
            if (currentWorkout != null)
            {
                val recyclerView = view.findViewById<RecyclerView>(R.id.currentWorkout)
                val adapter = WorkoutsAdapter(listOf(currentWorkout), { workout -> // only adding current workout
                    val bundle = Bundle()
                    bundle.putString("WorkoutID", workout.workoutID)
                    navigateTo(requireContext(), StartEmptyWorkoutActivity::class.java, bundle)
                }, requireContext(), R.layout.item_current_workout)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }
        }
    }

    private fun loadMonthWorkouts() {
        CoroutineScope(Dispatchers.IO).launch {
            val user = UserManager.user
            if (user == null) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            // Group workouts by month
            val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            val grouped = user.userWorkouts.values.groupBy { dateFormat.format(it.date) }
                .map { (month, workouts) ->
                    MonthWorkout(month, workouts.sortedBy { it.date })
                }.sortedByDescending {
                    // To sort months in descending order, parse the month string back to Date
                    SimpleDateFormat("MMMM yyyy", Locale.getDefault()).parse(it.month) ?: Date()
                }

            monthWorkoutList = grouped

            withContext(Dispatchers.Main) {
                monthsAdapter = MonthsAdapter(monthWorkoutList, { workout ->
                    val bundle = Bundle()
                    bundle.putString("WorkoutID", workout.workoutID)
                    navigateTo(requireContext(), StartEmptyWorkoutActivity::class.java, bundle)
                }, requireContext())
                routinesRecyclerView.adapter = monthsAdapter
                monthsAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun applyFloatUpAnimation(view: View?) {
        view?.let {
            val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.float_up)
            it.startAnimation(animation)
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}