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
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutExercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet

interface OnSetsUpdatedListener
{
    fun onSetsUpdated(exerciseID: String, set: WorkoutSet)
}

open class CreateRoutineExercisesAdapter(
    private val exercises: List<WorkoutExercise>,
    private val context: Context
) : RecyclerView.Adapter<CreateRoutineExercisesAdapter.CreateRoutineExercisesViewHolder>()
{
    private val setsUpdatedListeners = mutableListOf<OnSetsUpdatedListener>()

    class CreateRoutineExercisesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val exerciseName = itemView.findViewById<TextView>(R.id.exerciseName)
        val notes = itemView.findViewById<EditText>(R.id.notesEditText)
        val routineSets = itemView.findViewById<RecyclerView>(R.id.routineSetsList)
        val addSetButton = itemView.findViewById<AppCompatButton>(R.id.addSetButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CreateRoutineExercisesViewHolder
    {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.component_exercisedetails_display, parent, false)
        return CreateRoutineExercisesViewHolder(view)
    }

    override fun onBindViewHolder(holder: CreateRoutineExercisesViewHolder, position: Int)
    {
        val exercise = exercises[position]

        // Binding exerciseName
        holder.exerciseName.text = exercise.exerciseName

        // Binding initial notes to exercise notes
        holder.notes.setText(exercise.notes)

        // Binding routineSets
        val setsList = exercise.sets.values.toMutableList()
        val adapter = RoutineSetsAdapter(setsList, context)
        holder.routineSets.adapter = adapter
        holder.routineSets.layoutManager = LinearLayoutManager(context)

        // Add set Onclick
        holder.addSetButton.setOnClickListener {
            val setToBeAdded = WorkoutSet()
            setsList.add(setToBeAdded)
            adapter.notifyItemInserted(setsList.count() - 1)
            // Notify set to be added in Activity
            notifySetsUpdated(exercise.exerciseID, setToBeAdded)
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
    private fun notifySetsUpdated(exerciseID: String, set: WorkoutSet)
    {
        for (listener in setsUpdatedListeners)
        {
            listener.onSetsUpdated(exerciseID, set)
        }
    }
}