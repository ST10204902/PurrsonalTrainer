package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.workout_activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.CreateRoutineExercisesAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.OnSetsUpdatedListener
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.WorkoutExercisesAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityStartEmptyWorkoutBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserRoutine
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserWorkout
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.HomeActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments.ChooseCategoryFragment
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.ExerciseAddedListener
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.NotificationService
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.NotificationService.Companion.dismissContinuousNotification
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilder
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineConverter
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.SlideUpPopup
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class StartEmptyWorkoutActivity : AppCompatActivity(), ExerciseAddedListener, OnSetsUpdatedListener {
    private lateinit var binding: ActivityStartEmptyWorkoutBinding
    private lateinit var exercisesRecyclerView: RecyclerView
    var boundWorkout: UserWorkout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityStartEmptyWorkoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Bind UI to Workout fields (if they exist)
        bindWorkoutDetails()
        startContinuousNotification()
        // Add Exercise functionality
        setupAddExerciseButton()

        // Add done button onclick to save the workout
        if (boundWorkout != null)
            setupDoneButton()
    }

    private fun setupDoneButton()
    {
        // Subscribing this activity to the ExerciseAddedListener for the RoutineBuilder
        RoutineBuilder.addExerciseAddedListener(this)
        // Check for existing exercises
        this.onExerciseAdded()

        binding.doneButton.setOnClickListener {
            // Update workout in database
            saveUserWorkout()

            val intent = Intent(this, NotificationService::class.java)
            stopService(intent)

            dismissContinuousNotification(this)

            // UI stuffs (Anneme)
            binding.doneButton.setBackgroundResource(R.drawable.svg_green_bblbtn_clicked)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.doneButton.background = binding.doneButton.background
            }, 400)

            UserManager.resetWorkoutInProgress()

            // Navigating back to home activity
            navigateTo(this, HomeActivity::class.java, null)
        }
    }

    private fun saveUserWorkout()
    {
        val newWorkout = UserWorkout(
            workoutID = boundWorkout!!.workoutID,
            workoutExercises = RoutineBuilder.exercises, // using RoutineBuilder for exercises
            date = boundWorkout!!.date,
            name = getWorkoutTitle(),
            durationSeconds = calculateWorkoutDuration(),
            bodyWeight = getBodyWeight(),
            color = boundWorkout!!.color)

        UserManager.updateUserWorkout(boundWorkout!!.workoutID, newWorkout)
        UserManager.updateWorkoutInProgress(newWorkout.workoutID)
    }

    private fun calculateWorkoutDuration() : Int
    {
        // Only recalculate the duration if the workout is being created
        if (boundWorkout!!.durationSeconds == 0)
        {
            val now = LocalDateTime.now()
            val workoutStartTime = convertDateToLocalDateTime(boundWorkout!!.date)
            val duration = Duration.between(workoutStartTime, now)
            return duration.seconds.toInt()
        }
        return boundWorkout!!.durationSeconds
    }

    private fun setupAddExerciseButton()
    {
        val addButton = binding.addExerciseButton
        val popup = SlideUpPopup(
            supportFragmentManager,
            binding.chooseCategoryFragmentContainer,
            binding.chooseCategoryDismissArea,
            ChooseCategoryFragment(),
            this)

        addButton.setOnClickListener { popup.showPopup() }
        RoutineBuilder.addExerciseAddedListener(this)
    }

    private fun bindWorkoutDetails()
    {
        // Populating boundWorkout
        if (UserManager.user != null)
        {
            if (!getWorkoutIfExists() && !getRoutineIfExists())
            {
                // set BoundWorkout to a new Workout and add it to the database
                this.boundWorkout = UserWorkout()
                UserManager.addUserWorkout(this.boundWorkout!!)

                // TODO: Remove the exercises from RoutineBuilder for empty workout
            }
            else // Add existing exercises in the boundWorkout to the RoutineBuilder
            {


                for (exercise in boundWorkout!!.workoutExercises)
                {
                    RoutineBuilder.addWorkoutExercise(exercise.value)
                }
            }
        }

        // Setup for exercises in this workout
        setupExercises()

        // Bind workout details
        setupDetails()
    }

    private fun setupDetails()
    {
        if (boundWorkout != null)
        {
            // Title
            if (boundWorkout!!.name.isNotEmpty())
            {
                binding.workoutTitle.text = boundWorkout!!.name
            }
            else
            {
                binding.workoutTitle.text = "Empty Workout"
            }
            setTitleColor(boundWorkout!!.color)

            // Details
            val detailsComponent = binding.detailsComponent
            var startDate = LocalDateTime.now()
            var bodyWeight = "--"
            var endDate = "--"

            // if the workout has already taken place
            if (boundWorkout!!.durationSeconds > 0)
            {
                startDate = convertDateToLocalDateTime(boundWorkout!!.date)
                val calculatedEndDate = startDate.plusSeconds(boundWorkout!!.durationSeconds.toLong())
                endDate = formatWorkoutTime(calculatedEndDate)
                bodyWeight = "${boundWorkout!!.bodyWeight}kg"
            }
            // Setting UI text
            detailsComponent.workoutStartTime.text = formatWorkoutTime(startDate)
            detailsComponent.workoutEndTime.text = endDate
            detailsComponent.workoutBodyWeight.setText(bodyWeight)
        }
    }

    private fun getBodyWeight() : Int
    {
        val bodyWeightText = binding.detailsComponent.workoutBodyWeight.text.toString()

        // Remove any non-digit characters and extract the integer part
        val weight = bodyWeightText.replace("[^\\d]".toRegex(), "")

        return weight.toIntOrNull() ?: 0  // Return 0 if parsing fails
    }

    private fun getWorkoutTitle() : String
    {
        if (boundWorkout!!.name.isEmpty())
        {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return formatter.format(boundWorkout!!.date)
        }
        return boundWorkout!!.name
    }

    private fun convertDateToLocalDateTime(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    private fun setTitleColor(color: String)
    {
        val title = binding.workoutTitle

        when (color)
        {
            "blue" -> title.reInitialiseComponent(R.color.blue_start, R.color.blue_end)
            "red" -> title.reInitialiseComponent(R.color.red_start, R.color.red_end)
            "orange" -> title.reInitialiseComponent(R.color.orange_start, R.color.orange_end)
            "yellow" -> title.reInitialiseComponent(R.color.yellow_start, R.color.yellow_end)
            "green" -> title.reInitialiseComponent(R.color.green_start, R.color.green_end)
            "purple" -> title.reInitialiseComponent(R.color.purple_start, R.color.purple_end)
            else -> {title.reInitialiseComponent(R.color.orange_start, R.color.orange_end)} // set to orange
        }
    }

    private fun formatWorkoutTime(dateTime: LocalDateTime) : String
    {
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM 'at' HH:mm", Locale.ENGLISH)
        return dateTime.format(formatter)
    }

    private fun setupExercises()
    {
        this.exercisesRecyclerView = binding.workoutExercises

        // Add exercises if navigated to from a routine
        if (boundWorkout != null)
        {
            val adapter = WorkoutExercisesAdapter(boundWorkout!!.workoutExercises.values.toList(), this)
            exercisesRecyclerView.adapter = adapter
            exercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    /**
     * Gets the routine using a routine id passed when this activity is navigated to.
     * If the user clicked on a specific routine to navigate here, the boundRoutine will be set.
     */
    private fun getRoutineIfExists() : Boolean
    {
        // Getting data from the intent's bundle
        val extras = intent.extras
        if (extras != null) {

            // Unpacking bundle
            val bundle = extras.getBundle("data")
            val routineID = bundle?.getString("routineID")

            // Setting bound workout
            val routine = UserManager.user!!.userRoutines[routineID]
            if (routine != null)
            {
                this.boundWorkout = RoutineConverter().convertUserRoutineToUserWorkout(routine)
                UserManager.addUserWorkout(this.boundWorkout!!)
                return true
            }
        }
        return false
    }

    private fun getWorkoutIfExists() : Boolean
    { // Getting data from the intent's bundle
        val extras = intent.extras
        if (extras != null)
        {

            // Unpacking bundle
            val bundle = extras.getBundle("data")
            val workoutID = bundle?.getString("WorkoutID")

            // Setting bound workout
            val workout = UserManager.user!!.userWorkouts[workoutID]
            if (workout != null)
            {
                this.boundWorkout = workout
                UserManager.addUserWorkout(this.boundWorkout!!)
                return true
            }
        }
        return false
    }

    override fun onExerciseAdded()
    {
        if (RoutineBuilder.hasAnExercise()) {
            try
            {
                val recyclerView = binding.workoutExercises
                val userExercises = RoutineBuilder.exercises.values.toMutableList()
                val adapter = WorkoutExercisesAdapter(userExercises, this)
                adapter.addSetUpdatedListener(this)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this)

                // saving current version of workout
                this.saveUserWorkout()
            }
            catch (e: Exception)
            {
                Log.e("Failed to get exercises", e.toString())
            }
        }
    }

    override fun onSetsUpdated(exerciseID: String, sets: MutableList<WorkoutSet>) {
        val setsMap = mutableMapOf<String, WorkoutSet>()
        sets.forEach {
            setsMap[it.workoutSetID] = it
        }

        val oldExercise = RoutineBuilder.exercises[exerciseID]
        val newExercise = WorkoutExercise(
            exerciseID,
            oldExercise?.exerciseName!!,
            oldExercise.category,
            setsMap,
            Date(),
            oldExercise.notes,
            oldExercise.measurementType
        )
        RoutineBuilder.addWorkoutExercise(newExercise)

        // Saving current version of workout
        this.saveUserWorkout()
    }

    private fun startContinuousNotification() {
        // Start the continuous notification
        val intent = Intent(this, NotificationService::class.java)
        this.startService(intent) // Ensure the service is running
        NotificationService.showContinuousNotification(this,boundWorkout!!.date.time)
    }


}