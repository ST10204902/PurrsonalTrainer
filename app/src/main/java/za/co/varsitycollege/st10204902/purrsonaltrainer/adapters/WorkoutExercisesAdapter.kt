package za.co.varsitycollege.st10204902.purrsonaltrainer.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.adapters.CreateRoutineExercisesAdapter.CreateRoutineExercisesViewHolder
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.UserManager
import za.co.varsitycollege.st10204902.purrsonaltrainer.backend.WorkoutWorker
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet
import za.co.varsitycollege.st10204902.purrsonaltrainer.services.ExerciseService

class WorkoutExercisesAdapter(
    private val exercises: List<WorkoutExercise>,
    private val context: Context
) : RecyclerView.Adapter<WorkoutExercisesAdapter.WorkoutExercisesViewHolder>()
{
    private val setsUpdatedListeners = mutableListOf<OnSetsUpdatedListener>()

    class WorkoutExercisesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val exerciseName = itemView.findViewById<TextView>(R.id.exerciseName)
        val notes = itemView.findViewById<EditText>(R.id.notesEditText)
        val workoutSets = itemView.findViewById<RecyclerView>(R.id.workoutSetsList)
        val addSetButton = itemView.findViewById<AppCompatButton>(R.id.addSetButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WorkoutExercisesViewHolder
    {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.component_exercisedetails_tracking, parent, false)
        return WorkoutExercisesViewHolder(view)
    }

    override fun onBindViewHolder(holder: WorkoutExercisesViewHolder, position: Int)
    {
        val exercise = exercises[position]

        // Binding exerciseName
        holder.exerciseName.text = exercise.exerciseName

        // Binding initial notes to exercise notes
        holder.notes.setText(exercise.notes)

       val workoutWorker = WorkoutWorker(UserManager.user!!.userWorkouts)
        // Binding routineSets
        val setsList = exercise.sets.values.toMutableList()
        val previousWeightList = workoutWorker.getPreviousWorkoutExercises(exercise.exerciseID, setsList)
        val adapter = WorkoutSetsAdapter(setsList,previousWeightList, context)
        holder.workoutSets.adapter = adapter
        holder.workoutSets.layoutManager = LinearLayoutManager(context)

        // Add set Onclick
        holder.addSetButton.setOnClickListener {
            val setToBeAdded = WorkoutSet()
            setsList.add(setToBeAdded)
            adapter.notifyItemInserted(setsList.count() - 1)
            // Notify set to be added in Activity
            notifySetsUpdated(exercise.exerciseID, setsList)
        }
    }

    override fun getItemCount(): Int
    {
        return exercises.count()
    }

    fun addSetUpdatedListener(listener: OnSetsUpdatedListener)
    {
        setsUpdatedListeners.add(listener)
    }

    /**
     * Notifies subscribers that a set has been added
     */
    private fun notifySetsUpdated(exerciseID: String, sets: MutableList<WorkoutSet>)
    {
        for (listener in setsUpdatedListeners)
        {
            listener.onSetsUpdated(exerciseID, sets)
        }
    }
}