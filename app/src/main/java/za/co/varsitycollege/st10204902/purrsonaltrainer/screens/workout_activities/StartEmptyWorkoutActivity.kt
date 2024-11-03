package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.workout_activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.OnSetsUpdatedListener
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.WorkoutExercisesAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityStartEmptyWorkoutBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserWorkout
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.HomeActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments.ChooseCategoryFragment
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.ExerciseAddedListener
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.NotificationService
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.NotificationService.Companion.dismissContinuousNotification
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilder
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilderProvider
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineConverter
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.SlideUpPopup
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.GamifiedStatsManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo
import za.co.varsitycollege.st10204902.purrsonaltrainer.stores.ItemsStore
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Activity to start an empty workout.
 * Implements ExerciseAddedListener and OnSetsUpdatedListener interfaces.
 */
class StartEmptyWorkoutActivity : AppCompatActivity(), ExerciseAddedListener, OnSetsUpdatedListener,
    RoutineBuilderProvider {
    private lateinit var binding: ActivityStartEmptyWorkoutBinding
    private lateinit var exercisesRecyclerView: RecyclerView
    override val routineBuilder = RoutineBuilder()
    private val calculator = GamifiedStatsManager(this)
    var boundWorkout: UserWorkout? = null
    var currentUser = UserManager.user!!

    /**
     * Called when the activity is starting.
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down then this Bundle contains the data it most recently supplied in onSaveInstanceState(Bundle).
     */
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

    /**
     * Sets up the done button to save the workout and navigate back to the home activity.
     */
    private fun setupDoneButton() {
        // Subscribing this activity to the ExerciseAddedListener for the RoutineBuilder
        routineBuilder.addExerciseAddedListener(this)
        // Check for existing exercises
        this.onExerciseAdded()

        binding.doneButton.setOnClickListener {
            // Update workout in database
            val workout = saveUserWorkout()


            val intent = Intent(this, NotificationService::class.java)
            stopService(intent)

            dismissContinuousNotification(this)

            // UI stuffs (Anneme)
            binding.doneButton.setBackgroundResource(R.drawable.svg_green_bblbtn_clicked)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.doneButton.background = binding.doneButton.background
            }, 400)


            if (UserManager.user != null)
            {
                val index = UserManager.user!!.equippedItem
                val equippedItem = if (index != "") ItemsStore.globalItems[UserManager.user!!.equippedItem.toInt()] else null

                calculator.updateUserStatsAfterWorkout(currentUser,workout, equippedItem)
                UserManager.resetWorkoutInProgress()
            }
            //TODO: change equippedItem in the database an model to be a int


            // Navigating back to home activity
            navigateTo(this, HomeActivity::class.java, null)
        }
    }

    /**
     * Saves the current workout to the UserManager.
     */
    private fun saveUserWorkout(): UserWorkout {
        val newWorkout = UserWorkout(
            workoutID = boundWorkout!!.workoutID,
            workoutExercises = routineBuilder.exercises, // using routineBuilder for exercises
            date = boundWorkout!!.date,
            name = getWorkoutTitle(),
            durationSeconds = calculateWorkoutDuration(),
            bodyWeight = getBodyWeight(),
            color = boundWorkout!!.color
        )



        UserManager.updateUserWorkout(boundWorkout!!.workoutID, newWorkout)
        UserManager.updateWorkoutInProgress(newWorkout.workoutID)
        return newWorkout
    }

    /**
     * Calculates the duration of the workout.
     * @return The duration in seconds.
     */
    private fun calculateWorkoutDuration(): Int {
        // Only recalculate the duration if the workout is being created
        if (boundWorkout!!.durationSeconds == 0) {
            val now = LocalDateTime.now()
            val workoutStartTime = convertDateToLocalDateTime(boundWorkout!!.date)
            val duration = Duration.between(workoutStartTime, now)
            return duration.seconds.toInt()
        }
        return boundWorkout!!.durationSeconds
    }

    /**
     * Sets up the add exercise button to show a popup for choosing a category.
     */
    private fun setupAddExerciseButton() {
        val addButton = binding.addExerciseButton
        val popup = SlideUpPopup(
            supportFragmentManager,
            binding.chooseCategoryFragmentContainer,
            binding.chooseCategoryDismissArea,
            ChooseCategoryFragment(),
            this
        )

        addButton.setOnClickListener { popup.showPopup() }
        routineBuilder.addExerciseAddedListener(this)
    }

    /**
     * Binds the workout details to the UI.
     */
    private fun bindWorkoutDetails() {
        // Populating boundWorkout
        if (UserManager.user != null) {
            if (!getWorkoutIfExists() && !getRoutineIfExists()) {
                // set BoundWorkout to a new Workout and add it to the database
                this.boundWorkout = UserWorkout()
                UserManager.addUserWorkout(this.boundWorkout!!)

                // TODO: Remove the exercises from routineBuilder for empty workout
            } else { // Add existing exercises in the boundWorkout to the routineBuilder
                for (exercise in boundWorkout!!.workoutExercises) {
                    routineBuilder.addWorkoutExercise(exercise.value)
                }
            }
        }

        // Setup for exercises in this workout
        setupExercises()

        // Bind workout details
        setupDetails()
    }

    /**
     * Sets up the workout details in the UI.
     */
    private fun setupDetails() {
        if (boundWorkout != null) {
            // Title
            if (boundWorkout!!.name.isNotEmpty()) {
                binding.workoutTitle.text = boundWorkout!!.name
            } else {
                binding.workoutTitle.text = "Empty Workout"

            }
            setTitleColor(boundWorkout!!.color)

            // Details
            val detailsComponent = binding.detailsComponent
            var startDate = LocalDateTime.now()
            var bodyWeight = "--"
            var endDate = "--"

            // if the workout has already taken place
            if (boundWorkout!!.durationSeconds > 0) {
                startDate = convertDateToLocalDateTime(boundWorkout!!.date)
                val calculatedEndDate = startDate.plusSeconds(boundWorkout!!.durationSeconds.toLong())
                endDate = formatWorkoutTime(calculatedEndDate)
                bodyWeight = "${boundWorkout!!.bodyWeight}kg"
            }
            // Setting UI text
            detailsComponent.workoutStartTime.text = formatWorkoutTime(startDate)
            detailsComponent.workoutEndTime.text = endDate
            detailsComponent.workoutBodyWeight.setText(bodyWeight)
            detailsComponent.workoutBodyWeight.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    detailsComponent.workoutBodyWeight.text.clear()  // Clear the text when the TextView is selected
                }
                else
                {
                    if (detailsComponent.workoutBodyWeight.text.isEmpty()) {
                        detailsComponent.workoutBodyWeight.text = Editable.Factory.getInstance().newEditable("--")
                    }
                }
            }
        }
    }

    /**
     * Retrieves the body weight from the UI.
     * @return The body weight as an integer.
     */
    private fun getBodyWeight(): Int {
        val bodyWeightText = binding.detailsComponent.workoutBodyWeight.text.toString()

        // Remove any non-digit characters and extract the integer part
        val weight = bodyWeightText.replace("[^\\d]".toRegex(), "")

        return weight.toIntOrNull() ?: 0  // Return 0 if parsing fails
    }

    /**
     * Retrieves the workout title.
     * @return The workout title as a string.
     */
    private fun getWorkoutTitle(): String {
        if (boundWorkout!!.name.isEmpty()) {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            return formatter.format(boundWorkout!!.date)
        }
        return boundWorkout!!.name
    }

    /**
     * Converts a Date object to a LocalDateTime object.
     * @param date The date to convert.
     * @return The converted LocalDateTime object.
     */
    private fun convertDateToLocalDateTime(date: Date): LocalDateTime {
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault())
    }

    /**
     * Sets the title color based on the workout color.
     * @param color The color of the workout.
     */
    private fun setTitleColor(color: String) {
        val title = binding.workoutTitle

        when (color) {
            "blue" -> title.reInitialiseComponent(R.color.blue_start, R.color.blue_end)
            "red" -> title.reInitialiseComponent(R.color.red_start, R.color.red_end)
            "orange" -> title.reInitialiseComponent(R.color.orange_start, R.color.orange_end)
            "yellow" -> title.reInitialiseComponent(R.color.yellow_start, R.color.yellow_end)
            "green" -> title.reInitialiseComponent(R.color.green_start, R.color.green_end)
            "purple" -> title.reInitialiseComponent(R.color.purple_start, R.color.purple_end)
            else -> {title.reInitialiseComponent(R.color.orange_start, R.color.orange_end)} // set to orange
        }
    }

    /**
     * Formats a LocalDateTime object to a string.
     * @param dateTime The LocalDateTime object to format.
     * @return The formatted string.
     */
    private fun formatWorkoutTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("EEE, d MMM 'at' HH:mm", Locale.ENGLISH)
        return dateTime.format(formatter)
    }

    /**
     * Sets up the exercises RecyclerView.
     */
    private fun setupExercises() {
        this.exercisesRecyclerView = binding.workoutExercises

        // Add exercises if navigated to from a routine
        if (boundWorkout != null) {
            val adapter = WorkoutExercisesAdapter(boundWorkout!!.workoutExercises.values.toList(),boundWorkout!!.workoutID, this)
            exercisesRecyclerView.adapter = adapter
            exercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        }
    }

    /**
     * Gets the routine using a routine id passed when this activity is navigated to.
     * If the user clicked on a specific routine to navigate here, the boundRoutine will be set.
     * @return True if a routine exists, false otherwise.
     */
    private fun getRoutineIfExists(): Boolean {
        // Getting data from the intent's bundle
        val extras = intent.extras
        if (extras != null) {

            // Unpacking bundle
            val bundle = extras.getBundle("data")
            val routineID = bundle?.getString("routineID")

            // Setting bound workout
            val routine = UserManager.user!!.userRoutines[routineID]
            if (routine != null) {
                this.boundWorkout = RoutineConverter().convertUserRoutineToUserWorkout(routine)
                UserManager.addUserWorkout(this.boundWorkout!!)
                return true
            }
        }
        return false
    }

    /**
     * Gets the workout using a workout id passed when this activity is navigated to.
     * If the user clicked on a specific workout to navigate here, the boundWorkout will be set.
     * @return True if a workout exists, false otherwise.
     */
    private fun getWorkoutIfExists(): Boolean {
        // Getting data from the intent's bundle
        val extras = intent.extras
        if (extras != null) {

            // Unpacking bundle
            val bundle = extras.getBundle("data")
            val workoutID = bundle?.getString("WorkoutID")

            // Setting bound workout
            val workout = UserManager.user!!.userWorkouts[workoutID]
            if (workout != null) {
                this.boundWorkout = workout
                UserManager.addUserWorkout(this.boundWorkout!!)
                return true
            }
        }
        return false
    }

    /**
     * Called when an exercise is added.
     */
    override fun onExerciseAdded() {
        if (routineBuilder.hasAnExercise()) {
            try {
                val recyclerView = binding.workoutExercises
                val userExercises = routineBuilder.exercises.values.toMutableList()
                val adapter = WorkoutExercisesAdapter(userExercises,boundWorkout!!.workoutID, this)
                adapter.addSetUpdatedListener(this)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this)

                // saving current version of workout
                this.saveUserWorkout()
            } catch (e: Exception) {
                Log.e("Failed to get exercises", e.toString())
            }
        }
    }

    /**
     * Called when sets are updated.
     * @param exerciseID The ID of the exercise.
     * @param sets The list of updated sets.
     */
    override fun onSetsUpdated(exerciseID: String, sets: MutableList<WorkoutSet>) {
        val setsMap = mutableMapOf<String, WorkoutSet>()
        sets.forEach {
            setsMap[it.workoutSetID] = it
        }

        val oldExercise = routineBuilder.exercises[exerciseID]
        val newExercise = WorkoutExercise(
            exerciseID,
            oldExercise?.exerciseName!!,
            oldExercise.category,
            setsMap,
            Date(),
            oldExercise.notes,
            oldExercise.measurementType
        )
        routineBuilder.addWorkoutExercise(newExercise)

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