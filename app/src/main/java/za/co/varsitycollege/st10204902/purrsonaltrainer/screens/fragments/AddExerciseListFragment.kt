package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.ExerciseAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Exercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.ExerciseService
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilder
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilderProvider

/**
 * Fragment for displaying a list of exercises and allowing the user to add exercises to a routine.
 */
class AddExerciseListFragment : Fragment() {
    private var categoryId: String? = null
    private lateinit var routineBuilder: RoutineBuilder

    /**
     * Called to do initial creation of the fragment.
     * @param savedInstanceState If the fragment is being re-created from a previous saved state, this is the state.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryId = it.getString(ARG_CATEGORY_ID)
        }
    }

    /**
     * Called to have the fragment instantiate its user interface view.
     * @param inflater The LayoutInflater object that can be used to inflate any views in the fragment.
     * @param container If non-null, this is the parent view that the fragment's UI should be attached to.
     * @param savedInstanceState If non-null, this fragment is being re-constructed from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add_exercise_list, container, false)

        routineBuilder = (activity as? RoutineBuilderProvider)?.routineBuilder
            ?: throw IllegalStateException("Parent activity must implement RoutineBuilderProvider")

        val exerciseService = ExerciseService(requireContext())
        //maintain a list of ALL exercises in the category
        val fullListOfCategoryExercises = exerciseService.getExerciseInCategory(categoryId!!)
        var displayedExerciseList = fullListOfCategoryExercises
        val txtSearch = view.findViewById<EditText>(R.id.searchInput)

        val recyclerView: RecyclerView = view.findViewById(R.id.exercisesRecycler)
        val addCategoryButton = view.findViewById<LinearLayout>(R.id.addCategoryButton)
        recyclerView.layoutManager = LinearLayoutManager(context)

        exerciseService.updateExerciseService()

        recyclerView.adapter = ExerciseAdapter(displayedExerciseList, requireContext(), object : ExerciseAdapter.OnItemClickListener{
            override fun onItemClick(exercise: Exercise) {
                routineBuilder.addExercise(exercise)
                exerciseService.updateExerciseService()
                requireActivity().findViewById<FrameLayout>(R.id.chooseCategoryFragmentContainer).visibility = View.GONE
                requireActivity().findViewById<View>(R.id.chooseCategoryDismissArea).visibility = View.GONE
            }
       },categoryId, parentFragmentManager)

        //onclick listener for the add category button
        addCategoryButton.setOnClickListener {
            val fragmentManager = parentFragmentManager
            fragmentManager.beginTransaction().apply {
                replace(R.id.chooseCategoryFragmentContainer, CreateExerciseFragment.newInstance(null, categoryId!!))
                addToBackStack(null)
                commit()
            }
        }

        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // No action needed before text changes
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                displayedExerciseList = exerciseService.searchExercises(s.toString(), fullListOfCategoryExercises)
            }

            override fun afterTextChanged(s: Editable?) {
                recyclerView.adapter = ExerciseAdapter(displayedExerciseList, requireContext(), object : ExerciseAdapter.OnItemClickListener{
                    override fun onItemClick(exercise: Exercise) {
                        routineBuilder.addExercise(exercise)
                        exerciseService.updateExerciseService()
                        requireActivity().findViewById<FrameLayout>(R.id.chooseCategoryFragmentContainer).visibility = View.GONE
                        requireActivity().findViewById<View>(R.id.chooseCategoryDismissArea).visibility = View.GONE
                    }
                },categoryId, parentFragmentManager)
            }
        })
        return view
    }

    companion object {
        private const val ARG_CATEGORY_ID = "categoryId"

        /**
         * Use this factory method to create a new instance of this fragment using the provided parameters.
         * @param categoryId The ID of the category to display exercises for.
         * @return A new instance of fragment AddExerciseListFragment.
         */
        @JvmStatic
        fun newInstance(categoryId: String) =
            AddExerciseListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY_ID, categoryId)
                }
            }
    }
}