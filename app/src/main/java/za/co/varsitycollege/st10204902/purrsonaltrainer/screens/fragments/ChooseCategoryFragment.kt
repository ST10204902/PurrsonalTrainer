package za.co.varsitycollege.st10204902.purrsonaltrainer.screens.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.CategoryAdapter
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Exercise
import java.io.InputStreamReader
import java.lang.Thread.sleep

class ChooseCategoryFragment : Fragment() {

    private lateinit var categories: MutableList<String>
    private val usersCustomExercises: Map<String, Exercise>?
        get() = UserManager.user?.userExercises

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        val view = inflater.inflate(R.layout.fragment_choose_category, container, false)
        categories = mutableListOf(
            "lower back",
            "biceps",
            "traps",
            "adductors",
            "abs",
            "middle back",
            "glutes",
            "neck",
            "calves",
            "shoulders",
            "triceps",
            "forearms",
            "abductors",
            "hamstrings",
            "quads",
            "lats",
            "chest"
        )

        // Add all the unique exercise categories from the user's custom exercises that don't already exist in the default exercises
        val completeCategoryList = usersCustomExercises?.let {
            addUsersCustomCategories(categories, it)
        } ?: categories

        val recyclerView: RecyclerView = view.findViewById(R.id.categoryRecycler)
        recyclerView.layoutManager = LinearLayoutManager(context)
        //print each category
        Log.d("ChooseCategoryFragment", "completeCategoryList: ${completeCategoryList.listIterator()}")
        recyclerView.adapter = CategoryAdapter(completeCategoryList, requireContext())

        // Add category navigation code
        setupAddCategoryPopup(view)

        return view
    }

    private fun addUsersCustomCategories(mainCategoryList: MutableList<String>, usersCustomExercises: Map<String, Exercise>): List<String> {
        usersCustomExercises.keys.forEach {
            if (!mainCategoryList.contains(it)) {
                mainCategoryList.add(it)
            }
        }
        return mainCategoryList
    }

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

    private fun setupAddCategoryPopup(view: View)
    {
        val addCategoryButton = view.findViewById<AppCompatButton>(R.id.addCategoryButton)
        val fragmentContainer = requireActivity().findViewById<FrameLayout>(R.id.createCategoryFragmentContainer)
        val dismissArea = requireActivity().findViewById<View>(R.id.createCategoryDismissArea)

        // Preset the CreateCategoryFragment
        parentFragmentManager.beginTransaction().apply {
            replace(R.id.createCategoryFragmentContainer, CreateCategoryFragment())
            addToBackStack(null)
            commit()
        }

        // Setting up onclicks to show/ dismiss popup
        addCategoryButton.setOnClickListener { showCreateCategoryPopup(fragmentContainer, dismissArea) }
        dismissArea.setOnClickListener { dismissCreateCategoryPopup(fragmentContainer, dismissArea) }
    }

    private fun showCreateCategoryPopup(fragmentContainer: FrameLayout, dismissArea: View)
    {
        fragmentContainer.visibility = View.VISIBLE
        dismissArea.visibility = View.VISIBLE
    }

    private fun dismissCreateCategoryPopup(fragmentContainer: FrameLayout, dismissArea: View)
    {
        fragmentContainer.visibility = View.GONE
        dismissArea.visibility = View.GONE
    }
}