package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.workout_activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.AdapterView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.ColorSpinnerAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.CreateRoutineExercisesAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.OnSetsUpdatedListener
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.databinding.ActivityMadeRoutineBinding
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserRoutine
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.HomeActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments.ChooseCategoryFragment
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.ExerciseAddedListener
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilder
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilderProvider
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.SlideUpPopup
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo
import java.util.Date

class MadeRoutineActivity : AppCompatActivity(), ExerciseAddedListener, OnSetsUpdatedListener,
    RoutineBuilderProvider {
    private lateinit var binding: ActivityMadeRoutineBinding
    private lateinit var exercisesRecyclerView: RecyclerView
    override val routineBuilder = RoutineBuilder()
    var selectedColor = "blue"
    var boundRoutine: UserRoutine? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMadeRoutineBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Getting data provided from navigation
        getRoutineIfExists()
        // Setting up exercises for this routine
        setupExercises()
        // Bind routine details to fields
        bindRoutineDetails()
        // Adding an exercise Onclick setup
        setupAddExerciseButton()

        // Setup done button onclick to update routine
        if (boundRoutine != null)
            setupDoneButton()

        // Navigation for starting a workout from
        setupWorkoutNavigation()
    }

    private fun setupWorkoutNavigation()
    {
        if (boundRoutine != null)
        {
            binding.startWorkoutButton.setOnClickListener {
                // Adding routineID for which the workout will be created
                val bundle = Bundle()
                bundle.putString("routineID", boundRoutine!!.routineID)
                navigateTo(this, StartEmptyWorkoutActivity::class.java, bundle)
            }
        }
    }

    private fun setupDoneButton()
    {
        // Subscribing this activity to the ExerciseAddedListener for the RoutineBuilder
        routineBuilder.addExerciseAddedListener(this)
        // Check for existing exercises
        this.onExerciseAdded()

        val txtRoutineName = binding.routineTitle
        val txtDescription = binding.notes

        binding.doneButton.setOnClickListener {
            // Add Routine to database
            routineBuilder.setRoutineName(txtRoutineName.text.toString())
            routineBuilder.setRoutineColor(selectedColor)
            routineBuilder.setRoutineDescription(txtDescription.text.toString())

            if (routineBuilder.hasAnExercise())
                UserManager.updateUserRoutine(boundRoutine!!.routineID, routineBuilder.buildRoutine())
            else
                Log.d("CreateRoutineActivity", "No exercises added")

            // UI stuffs (Anneme)
            binding.doneButton.setBackgroundResource(R.drawable.svg_green_bblbtn_clicked)
            Handler(Looper.getMainLooper()).postDelayed({
                binding.doneButton.background = binding.doneButton.background
            }, 400)

            // Navigating back to home activity
            navigateTo(this, HomeActivity::class.java, null)
        }
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
    }

    private fun bindRoutineDetails()
    {
        if (boundRoutine != null)
        {
            val title = binding.routineTitle
            val notes = binding.notes

            title.setText(boundRoutine!!.name)
            notes.setText(boundRoutine!!.description)

            // Setting title colour
            setTitleColor(boundRoutine!!.color)
            selectedColor = boundRoutine!!.color

            // Setting the colour spinner and the selected item
            val spinner = binding.colorPickerSpinner
            val colors = mutableListOf("blue", "red", "orange", "yellow", "green", "purple")
            val adapter = ColorSpinnerAdapter(this, colors)
            spinner.adapter = adapter

            // Setting the selected colour to the routine color
            val position = colors.indexOf(colors.find { it == boundRoutine!!.color })
            if (position >= 0)
            {
                spinner.setSelection(position)
            }

            // Setting the onclick listener so the title color changes when the user chooses a new color
            spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                    val selectedItem = parent.getItemAtPosition(position).toString()
                    setTitleColor(selectedItem)
                    selectedColor = selectedItem
                }
                override fun onNothingSelected(parent: AdapterView<*>) {}
            }
        }
    }

    private fun setTitleColor(color: String)
    {
        val title = binding.routineTitle

        when (color)
        {
            "blue" -> title.reInitialiseComponent(R.color.blue_start, R.color.blue_end)
            "red" -> title.reInitialiseComponent(R.color.red_start, R.color.red_end)
            "orange" -> title.reInitialiseComponent(R.color.orange_start, R.color.orange_end)
            "yellow" -> title.reInitialiseComponent(R.color.yellow_start, R.color.yellow_end)
            "green" -> title.reInitialiseComponent(R.color.green_start, R.color.green_end)
            "purple" -> title.reInitialiseComponent(R.color.purple_start, R.color.purple_end)
        }
    }

    private fun setupExercises()
    {
        this.exercisesRecyclerView = binding.exercisesRecyclerView
        val workoutExercises = boundRoutine!!.exercises.values.toList()
        // Add exercises if navigated to from a routine
        if (boundRoutine != null)
        {
            val adapter = CreateRoutineExercisesAdapter(workoutExercises, this)
            exercisesRecyclerView.adapter = adapter
            exercisesRecyclerView.layoutManager = LinearLayoutManager(this)
        }

        // Setup routineBuilder
        for (exercise in workoutExercises)
        {
            routineBuilder.addWorkoutExercise(exercise)
        }
    }

    private fun getRoutineIfExists()
    {
        // Getting data from the intent's bundle
        val extras = intent.extras
        if (extras != null) {

            // Unpacking bundle
            val bundle = extras.getBundle("data")
            val routineID = bundle?.getString("routineID")

            // Setting bound routine
            boundRoutine = UserManager.user!!.userRoutines[routineID] // TESTED, Works
        }
    }

    override fun onExerciseAdded()
    {
        if (routineBuilder.hasAnExercise()) {
            try
            {
                val recyclerView = binding.exercisesRecyclerView
                val userExercises = routineBuilder.exercises.values.toMutableList()
                val adapter = CreateRoutineExercisesAdapter(userExercises, this)
                adapter.addSetUpdatedListener(this)
                recyclerView.adapter = adapter
                recyclerView.layoutManager = LinearLayoutManager(this)
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
    }
}