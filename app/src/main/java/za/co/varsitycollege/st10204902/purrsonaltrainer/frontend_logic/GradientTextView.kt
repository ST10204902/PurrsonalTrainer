package za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic

// GradientTextView.kt
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import za.co.varsitycollege.st10204902.purrsonaltrainer.R

/**
 * A custom TextView with gradient text and stroke.
 *
 * @constructor Creates a GradientTextView with the specified attributes.
 * @param context The context in which the TextView is used.
 * @param attrs The attributes of the XML tag that is inflating the view.
 * @param defStyleAttr An attribute in the current theme that contains a reference to a style resource that supplies default values for the view.
 */
class GradientTextView @JvmOverloads constructor(
    private val context: Context,
    private val attrs: AttributeSet? = null,
    private val defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    // Initialize paint objects
    private val strokePaint = Paint()
    private var startColor: Int
    private var endColor: Int
    private var strokeColor: Int
    private var strokeWidth: Float

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GradientTextView,
            0, 0
        ).apply {
            try {
                startColor = getColor(R.styleable.GradientTextView_startColor, context.getColor(R.color.default_start_color)) // Set default color of the start of the gradient
                endColor = getColor(R.styleable.GradientTextView_endColor, context.getColor(R.color.default_end_color)) // Set default color of the end of the gradient
                strokeColor = getColor(R.styleable.GradientTextView_strokeColor, context.getColor(R.color.default_stroke_color)) // Set default color of the stroke
                strokeWidth = getDimension(R.styleable.GradientTextView_strokeWidth, 30f)
            } finally {
                recycle()
            }
        }

        // Initialize paint
        val shader = LinearGradient(
            0f, 0f, 0f, textSize,
            intArrayOf(startColor, endColor),
            floatArrayOf(0.50f, 1.0f),
            Shader.TileMode.CLAMP
        )
        paint.shader = shader

        // Initialize stroke paint
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = strokeWidth // Set stroke width
        strokePaint.color = strokeColor
        strokePaint.isAntiAlias = true

        // Set padding to accommodate the stroke
        setPadding(strokeWidth.toInt(), strokeWidth.toInt(), strokeWidth.toInt(), strokeWidth.toInt())
    }

    fun reInitialiseComponent(startColourIn: Int, endColourIn: Int)
    {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GradientTextView,
            0, 0
        ).apply {
            try {
                startColor = getColor(R.styleable.GradientTextView_startColor, context.getColor(startColourIn)) // Set default color of the start of the gradient
                endColor = getColor(R.styleable.GradientTextView_endColor, context.getColor(endColourIn)) // Set default color of the end of the gradient
                strokeColor = getColor(R.styleable.GradientTextView_strokeColor, context.getColor(R.color.default_stroke_color)) // Set default color of the stroke
                strokeWidth = getDimension(R.styleable.GradientTextView_strokeWidth, 30f)
            } finally {
                recycle()
            }
        }

        // Initialize paint
        val shader = LinearGradient(
            0f, 0f, 0f, textSize,
            intArrayOf(startColor, endColor),
            floatArrayOf(0.50f, 1.0f),
            Shader.TileMode.CLAMP
        )
        paint.shader = shader

        // Initialize stroke paint
        strokePaint.style = Paint.Style.STROKE
        strokePaint.strokeWidth = strokeWidth // Set stroke width
        strokePaint.color = strokeColor
        strokePaint.isAntiAlias = true

        // Set padding to accommodate the stroke
        setPadding(strokeWidth.toInt(), strokeWidth.toInt(), strokeWidth.toInt(), strokeWidth.toInt())
    }

    /**
     * Draws the text with gradient and stroke.
     * @param canvas The canvas on which the background will be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        val text = text.toString()
        val x = ((width - paint.measureText(text)) / 2)
        val y = (height / 2) - ((strokePaint.descent() + strokePaint.ascent()) / 2)

        // Set the text alignment and size for stroke paint
        strokePaint.textSize = textSize
        strokePaint.typeface = typeface
        strokePaint.textAlign = paint.textAlign

        // Draw stroke text
        canvas.drawText(text, x, y, strokePaint)

        // Draw gradient text
        paint.style = Paint.Style.FILL
        canvas.drawText(text, x, y, paint)
    }
}