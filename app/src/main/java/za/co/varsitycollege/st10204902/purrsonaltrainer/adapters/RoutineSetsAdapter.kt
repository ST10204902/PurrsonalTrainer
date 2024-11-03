package za.co.varsitycollege.st10204902.purrsonaltrainer.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import za.co.varsitycollege.st10204902.purrsonaltrainer.R
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.Exercise
import za.co.varsitycollege.st10204902.purrsonaltrainer.models.WorkoutSet

class RoutineSetsAdapter(
    private val sets: MutableList<WorkoutSet>,
    private val previousWeights:  List<String>,
    private val context: Context,
) : RecyclerView.Adapter<RoutineSetsAdapter.RoutineSetsViewHolder>()
{
    class RoutineSetsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    {
        val setType = itemView.findViewById<Spinner>(R.id.setType)
        val previousWeight = itemView.findViewById<TextView>(R.id.previousWeight)
        val weightInput = itemView.findViewById<EditText>(R.id.weightInput)
        val repsInput = itemView.findViewById<EditText>(R.id.repsInput)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoutineSetsViewHolder
    {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detail_display_reps, parent, false)
        return RoutineSetsViewHolder(view)
    }

    override fun onBindViewHolder(holder: RoutineSetsViewHolder, position: Int)
    {
        val previousWeight: String
        val set = sets[position]
        if (previousWeights.count() <= position)
        {
            previousWeight = ""
        }
        else {
            previousWeight = previousWeights[position]
        }
        // setType
        when (set.setType)
        {
            SetType.WARMUP.name ->
            {
                holder.setType.adapter = SetTypeSpinnerAdapter(context, listOf(SetType.WARMUP, SetType.NORMAL, SetType.FAILURE, SetType.DROP))
            }
            SetType.FAILURE.name ->
            {
                holder.setType.adapter = SetTypeSpinnerAdapter(context, listOf(SetType.FAILURE, SetType.NORMAL, SetType.WARMUP, SetType.DROP))
            }
            SetType.DROP.name ->
            {
                holder.setType.adapter = SetTypeSpinnerAdapter(context, listOf(SetType.DROP, SetType.NORMAL, SetType.WARMUP, SetType.FAILURE))
            }
            else ->
            {
                holder.setType.adapter = SetTypeSpinnerAdapter(context, listOf(SetType.NORMAL, SetType.WARMUP, SetType.FAILURE, SetType.DROP))
            }
        }
        // previousWeight
        // weightInput
        if (set.weight != null)
            holder.weightInput.setText(set.weight.toString())
        // repsInput
        if (set.reps != null)
            holder.repsInput.setText(set.reps.toString())


        holder.previousWeight.text = previousWeight


        holder.weightInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

                val weight = s.toString().toIntOrNull() ?: 0
                set.weight = weight

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Optional: Do something before text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Optional: Do something while the text is being changed
            }
        })

        holder.repsInput.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Run your code here after the text has changed
                val reps = s.toString().toIntOrNull() ?: 0
                set.reps = reps
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Optional: Do something before text is changed
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Optional: Do something while the text is being changed
            }
        })

        holder.setType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val setType = parent?.getItemAtPosition(position) as SetType
                set.setType = setType.name
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                Log.d("RoutineSetsAdapter", "Nothing selected")
            }
        }


    }

    override fun getItemCount(): Int
    {
        return sets.count()
    }
}