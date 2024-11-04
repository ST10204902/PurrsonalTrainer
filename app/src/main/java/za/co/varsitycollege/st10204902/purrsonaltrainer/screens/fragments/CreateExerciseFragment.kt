package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatSpinner
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.ExerciseTypeSpinnerAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Exercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.screens.workout_activities.CreateRoutineActivity
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilder
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilderProvider
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.navigateTo


private var exercise: Exercise? = null
private var category: String? = null

/**
 * A simple [Fragment] subclass.
 * Use the [CreateExerciseFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class CreateExerciseFragment : Fragment() {
    private var spinnerItemList = listOf("Reps & Weight", "Time & Distance", "Time")
    private lateinit var routineBuilder: RoutineBuilder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            category = it.getString("category")
            val exerciseID = it.getString("exerciseID")
            if (exerciseID != null) {
                exercise = UserManager.user?.userExercises?.get(exerciseID)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_create_exercise, container, false)

        routineBuilder = (activity as? RoutineBuilderProvider)?.routineBuilder
            ?: throw IllegalStateException("Parent activity must implement RoutineBuilderProvider")

        val title = view.findViewById<EditText>(R.id.exerciseTitle)
        val workoutType = view.findViewById<AppCompatSpinner>(R.id.workoutTypeSpinner)
        workoutType.adapter = ExerciseTypeSpinnerAdapter(requireContext(), spinnerItemList)
        val notes = view.findViewById<EditText>(R.id.notes)
        val doneBTN = view.findViewById<AppCompatButton>(R.id.doneButton)

        //EDITING A EXERCISE
        if (exercise != null) {
            title.setText(exercise!!.exerciseName)
            workoutType.setSelection(spinnerItemList.indexOf(exercise!!.measurementType))
            notes.setText(exercise!!.notes)
        }

        doneBTN.setOnClickListener {
            if(exercise == null){
                val newExercise = Exercise(
                    exerciseName = title.text.toString(),
                    category = category!!,
                    notes = notes.text.toString(),
                    // set measurement type based on spinner
                    measurementType = workoutType.selectedItem.toString(),
                    isCustom = true
                )
                UserManager.addUserExercise(newExercise)
                routineBuilder.addExercise(newExercise)
                requireActivity().findViewById<FrameLayout>(R.id.chooseCategoryFragmentContainer).visibility = View.GONE
                requireActivity().findViewById<View>(R.id.chooseCategoryDismissArea).visibility = View.GONE
            }else{
                val newExercise = Exercise(
                    exerciseName = title.text.toString(),
                    category = category!!,
                    notes = notes.text.toString(),
                    // set measurement type based on spinner
                    measurementType = workoutType.selectedItem.toString(),
                    isCustom = true
                )
                if(exercise!!.exerciseName != newExercise.exerciseName) {
                    UserManager.updateUserExercise(exercise!!.exerciseID, newExercise)
                    var editedExercise = UserManager.user?.userExercises?.get(exercise!!.exerciseID)
                    routineBuilder.addExercise(editedExercise!!)
                }
                if(exercise!!.notes != newExercise.notes) {
                    UserManager.updateUserExercise(exercise!!.exerciseID, newExercise)
                    var editedExercise = UserManager.user?.userExercises?.get(exercise!!.exerciseID)
                    routineBuilder.addExercise(editedExercise!!)
                }
                // if they change the measurement type update the exercise
                if(exercise!!.measurementType != newExercise.measurementType) {
                    UserManager.updateUserExercise(exercise!!.exerciseID, newExercise)
                    var editedExercise = UserManager.user?.userExercises?.get(exercise!!.exerciseID)
                    routineBuilder.addExercise(editedExercise!!)
                }
                requireActivity().findViewById<FrameLayout>(R.id.chooseCategoryFragmentContainer).visibility = View.GONE
                requireActivity().findViewById<View>(R.id.chooseCategoryDismissArea).visibility = View.GONE
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance(exerciseID: String?, categoryName: String?) =
            CreateExerciseFragment().apply {
                arguments = Bundle().apply {
                    if (categoryName != null) {
                        putString("category", categoryName)
                    }
                    if (exerciseID != null) {
                        putString("exerciseID", exerciseID)
                    }
                }
            }
    }
}