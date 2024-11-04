package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.CategoryAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.ExerciseAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Exercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.ExerciseService
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilder
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.RoutineBuilderProvider
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.SlideUpPopup
import java.io.InputStreamReader

/**
 * Fragment for choosing an exercise category.
 */
class ChooseCategoryFragment() : Fragment() {

    private lateinit var categories: MutableList<String>
    private lateinit var routineBuilder: RoutineBuilder
    private val usersCustomCategories: List<String>?
        get() = UserManager.user?.customCategories

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
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_choose_category, container, false)

        routineBuilder = (activity as? RoutineBuilderProvider)?.routineBuilder
            ?: throw IllegalStateException("Parent activity must implement RoutineBuilderProvider")


        val exerciseService = ExerciseService(requireContext())
        categories = exerciseService.defaultCategories.toMutableList()

        // Add all the unique exercise categories from the user's custom exercises that don't already exist in the default exercises
        val completeCategoryList = usersCustomCategories?.let {
            addUsersCustomCategories(categories, it)
        } ?: categories

        val fullListOfCategoryExercises = exerciseService.loadObjectsFromJson()
        exerciseService.updateExerciseService()
        var displayedExerciseList = fullListOfCategoryExercises
        val recyclerView: RecyclerView = view.findViewById(R.id.categoryRecycler)

        val txtSearch = view.findViewById<EditText>(R.id.searchInput)

        txtSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                exerciseService.updateExerciseService()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                displayedExerciseList = exerciseService.searchExercises(s.toString(), fullListOfCategoryExercises)
            }

            override fun afterTextChanged(s: Editable?) {
                if (s != null) {
                    if (s.length == 0) {
                        recyclerView.layoutManager = LinearLayoutManager(context)
                        // Print each category
                        Log.d("ChooseCategoryFragment", "completeCategoryList: ${completeCategoryList.listIterator()}")
                        recyclerView.adapter = CategoryAdapter(completeCategoryList, requireContext(), object : CategoryAdapter.OnItemClickListener {
                            override fun onItemClick(category: String) {
                                exerciseService.updateExerciseService()
                                val fragmentManager = parentFragmentManager
                                fragmentManager.beginTransaction().apply {
                                    replace(R.id.chooseCategoryFragmentContainer, AddExerciseListFragment.newInstance(category))
                                    addToBackStack(null)
                                    commit()
                                }
                            }
                        })
                    } else {
                        recyclerView.adapter = ExerciseAdapter(
                            displayedExerciseList,
                            requireContext(),
                            object : ExerciseAdapter.OnItemClickListener {
                                override fun onItemClick(exercise: Exercise) {
                                    routineBuilder.addExercise(exercise)
                                    exerciseService.updateExerciseService()
                                    requireActivity().findViewById<FrameLayout>(R.id.chooseCategoryFragmentContainer).visibility = View.GONE
                                    requireActivity().findViewById<View>(R.id.chooseCategoryDismissArea).visibility = View.GONE
                                }
                            },categoryId = null, parentFragmentManager
                        )
                    }
                }
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(context)
        // Print each category
        Log.d("ChooseCategoryFragment", "completeCategoryList: ${completeCategoryList.listIterator()}")
        recyclerView.adapter = CategoryAdapter(completeCategoryList, requireContext(), object : CategoryAdapter.OnItemClickListener {
            override fun onItemClick(category: String) {
                val fragmentManager = parentFragmentManager
                fragmentManager.beginTransaction().apply {
                    replace(R.id.chooseCategoryFragmentContainer, AddExerciseListFragment.newInstance(category))
                    addToBackStack(null)
                    commit()
                }
            }
        })

        // Add category navigation code
        setupAddCategoryPopup(view)

        return view
    }

    /**
     * Adds the user's custom categories to the main category list if they don't already exist.
     * @param mainCategoryList The main list of categories.
     * @param customCategories The user's custom categories.
     * @return The updated list of categories.
     */
    private fun addUsersCustomCategories(mainCategoryList: MutableList<String>, customCategories: List<String>): List<String> {
        customCategories.forEach {
            if (!mainCategoryList.contains(it)) {
                mainCategoryList.add(it)
            }
        }
        return mainCategoryList
    }

    /**
     * Loads exercises mapped by category from a JSON file.
     * @param jsonFileName The name of the JSON file.
     * @return A list of exercise categories.
     */
    private suspend fun loadExercisesMappedByCategory(jsonFileName: String): List<String> {
        return CoroutineScope(Dispatchers.IO).async {
            val gson = Gson()
            val assetManager = requireContext().assets
            val inputStream = assetManager.open(jsonFileName)
            val reader = InputStreamReader(inputStream)
            val exerciseListType = object : TypeToken<List<Exercise>>() {}.type
            val exercises: List<Exercise> = gson.fromJson(reader, exerciseListType)
            reader.close()
            exercises.map { it.category }.distinct()
        }.await()
    }

    /**
     * Sets up the popup for adding a new category.
     * @param view The view to attach the popup to.
     */
    private fun setupAddCategoryPopup(view: View) {
        val addCategoryButton = view.findViewById<LinearLayout>(R.id.addCategoryButton)
        val fragmentContainer = requireActivity().findViewById<FrameLayout>(R.id.createCategoryFragmentContainer)
        val dismissArea = requireActivity().findViewById<View>(R.id.createCategoryDismissArea)

        // Setup popup
        val fragment = CreateCategoryFragment()
        val popup = SlideUpPopup(parentFragmentManager, fragmentContainer, dismissArea, fragment, requireContext())
        addCategoryButton.setOnClickListener { popup.showPopup() }

        // Setting dismiss action so that fragment is removed when user enters category
        fragment.setDismissAction { popup.dismissPopup() }
    }
}