package za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic

// GradientTextView.kt
import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import za.co.varsitycollege.st10204902.purrsonaltrainer.R

/**
 * A custom TextView with gradient text and stroke that supports text wrapping and multiple lines.
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
    private val strokePaint = TextPaint()
    private val fillPaint = TextPaint()
    private var startColor: Int
    private var endColor: Int
    private var strokeColor: Int
    private var strokeWidth: Float
    private var shader: Shader? = null

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.GradientTextView,
            0, 0
        ).apply {
            try {
                startColor = getColor(
                    R.styleable.GradientTextView_startColor,
                    context.getColor(R.color.default_start_color)
                )
                endColor = getColor(
                    R.styleable.GradientTextView_endColor,
                    context.getColor(R.color.default_end_color)
                )
                strokeColor = getColor(
                    R.styleable.GradientTextView_strokeColor,
                    context.getColor(R.color.default_stroke_color)
                )
                strokeWidth = getDimension(R.styleable.GradientTextView_strokeWidth, 30f)
            } finally {
                recycle()
            }
        }

        // Initialize stroke paint
        strokePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = this@GradientTextView.strokeWidth
            color = strokeColor
            isAntiAlias = true
            textSize = this@GradientTextView.textSize
            typeface = this@GradientTextView.typeface
        }

        // Initialize fill paint
        fillPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            textSize = this@GradientTextView.textSize
            typeface = this@GradientTextView.typeface
        }

        // Initialize shader for gradient
        shader = LinearGradient(
            0f, 0f, 0f, textSize,
            intArrayOf(startColor, endColor),
            floatArrayOf(0.50f, 1.0f),
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = shader

        // Set padding to accommodate the stroke
        setPadding(
            strokeWidth.toInt(),
            strokeWidth.toInt(),
            strokeWidth.toInt(),
            strokeWidth.toInt()
        )
    }

    /**
     * Draws the text with gradient and stroke.
     * @param canvas The canvas on which the background will be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        val width = measuredWidth - paddingLeft - paddingRight
        val text = text.toString()

        // Create StaticLayout for stroke text
        val textLayoutStroke = createStaticLayout(text, strokePaint, width)

        // Draw stroke text
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        textLayoutStroke.draw(canvas)
        canvas.restore()

        // Create StaticLayout for fill (gradient) text
        val textLayoutFill = createStaticLayout(text, fillPaint, width)

        // Draw fill (gradient) text
        canvas.save()
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())
        textLayoutFill.draw(canvas)
        canvas.restore()
    }

    /**
     * Measures the view and its content to determine the measured width and the measured height.
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val textWidth = width - paddingLeft - paddingRight
        val text = text.toString()

        val textLayout = createStaticLayout(text, paint, textWidth)
        var desiredHeight = textLayout.height + paddingTop + paddingBottom

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> MeasureSpec.getSize(heightMeasureSpec)
            MeasureSpec.AT_MOST -> Math.min(desiredHeight, MeasureSpec.getSize(heightMeasureSpec))
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    /**
     * Creates a StaticLayout for the provided text and paint.
     * @param text The text to layout.
     * @param textPaint The paint to use for the text.
     * @param width The width for the layout.
     * @return A StaticLayout instance.
     */
    private fun createStaticLayout(text: String, textPaint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(0f, 1f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                textPaint,
                width,
                Layout.Alignment.ALIGN_CENTER,
                1.0f,
                0.0f,
                false
            )
        }
    }

    /**
     * Re-initializes the component with new start and end colors for the gradient.
     * @param startColourIn The new start color.
     * @param endColourIn The new end color.
     */
    fun reInitialiseComponent(startColourIn: Int, endColourIn: Int) {
        startColor = context.getColor(startColourIn)
        endColor = context.getColor(endColourIn)

        // Update the shader for the fill paint
        shader = LinearGradient(
            0f, 0f, 0f, textSize,
            intArrayOf(startColor, endColor),
            floatArrayOf(0.50f, 1.0f),
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = shader

        invalidate()
    }
}
