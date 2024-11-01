package za.co.varsitycollege.st10204902.purrsonaltrainer.services

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.CreateID
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Exercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.UserRoutine
import java.io.InputStreamReader
import java.util.Locale

class ExerciseService(private val context: Context) {
    companion object {
        val defaultCategories: List<String> = if (Locale.getDefault().language == "af") {
            listOf(
                "adduktore",
                "middelrug",
                "kalfspiere",
                "gluteus",
                "hamstrings",
                "lats",
                "triseps",
                "bors",
                "rug",
                "maagspiere",
                "skouers",
                "biseps",
                "laerug",
                "kwadriseps"
            )
        } else {
            listOf(
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
        }
    }

    //                          PROPERTIES                       //
    //-----------------------------------------------------------//

    /**
     * List of all exercises including default and custom
     */
    var exercisesList = loadObjectsFromJson()

    /**
     * List of all default exercise categories
     */
    val defaultCategories: List<String> = Companion.defaultCategories
    //-----------------------------------------------------------//
    //                          METHODS                          //
    //-----------------------------------------------------------//

    /**
     * Load all exercises from the JSON file and add users exercises if the user has any
     * @return List<Exercise> - List of all exercises
     */
     fun loadObjectsFromJson(): List<Exercise> {
        val currentLocale = Locale.getDefault()
        val languageCode = currentLocale.language
        val jsonFileName = if (languageCode == "af") {
            "globalExercises_af.json" // English version
        } else {
            "globalExercises_eng.json" // Afrikaans version
        }
        val jsonFile = context.assets.open(jsonFileName)
        // Use InputStreamReader to read the file
        val reader = InputStreamReader(jsonFile)
        // Create a type token for List<MyObject>
        val listType = object : TypeToken<List<Exercise>>() {}.type
        // Parse the JSON using Gson
        var listOfAllExercises: List<Exercise> = Gson().fromJson(reader, listType)
        // Close the reader
        reader.close()
        val usersExercises = UserManager.user?.userExercises
        // add all users exercises to the list of all exercises
        usersExercises?.let {
            it.values.forEach { userExercise ->
                if (!listOfAllExercises.contains(userExercise)) {
                    listOfAllExercises = listOfAllExercises.plus(userExercise)
                }
            }
        }
        return listOfAllExercises
    }

    /**
     * Get all exercises in a specific category
     * @param category - Category to filter exercises by
     * @return List<Exercise> - List of exercises in the category
     */
    fun getExerciseInCategory(category: String): List<Exercise> {
        if (category.isNotEmpty()) {
            return exercisesList.filter { it.category == category }
        }
        return exercisesList
    }

    /**
     * checks the user's exercises and adds any that are not already in the exercisesList
     */
    fun updateExerciseService() {
       // add any exercises that do not already exist in exercisesList to the list from the userManager
        UserManager.user?.userExercises?.let {
            it.values.forEach { userExercise ->
                if (!exercisesList.contains(userExercise)) {
                    exercisesList = exercisesList.plus(userExercise)
                    println("added user exercise ${userExercise.exerciseName}")
                }
            }
        }
    }

    /**
     * Search for exercises in the list of exercises
     * @param search - Search query
     * @param listToSearch - List of exercises to search
     * @return List<Exercise> - List of exercises that match the search query
     */
    fun searchExercises(search: String, listToSearch: List<Exercise>): List<Exercise> {
        if (search.isEmpty()) {
            return listToSearch
        }
        return listToSearch.filter { it.exerciseName.contains(search, ignoreCase = true) }
    }
}
//------------------------***EOF***-----------------------------//