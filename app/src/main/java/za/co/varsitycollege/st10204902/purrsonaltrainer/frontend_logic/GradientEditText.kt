package za.co.varsitycollege.st10204902.purrsonaltrainer.frontend_logic

import android.content.Context
import android.graphics.*
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatEditText
import za.co.varsitycollege.st10204902.purrsonaltrainer.R

class GradientEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.appcompat.R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    private val strokePaint = TextPaint()
    private val fillPaint = TextPaint()
    private var startColor: Int
    private var endColor: Int
    private var strokeColor: Int
    private var strokeWidth: Float
    private var shader: Shader? = null

    // Adjusted line spacing to reduce line height moderately
    private val lineSpacingMultiplierCustom = 0.95f
    private val lineSpacingExtraCustom = 0f

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
                strokeWidth = getDimension(R.styleable.GradientTextView_strokeWidth, 2f)
            } finally {
                recycle()
            }
        }

        // Initialize stroke paint
        strokePaint.apply {
            style = Paint.Style.STROKE
            strokeWidth = this@GradientEditText.strokeWidth
            color = strokeColor
            isAntiAlias = true
            textSize = this@GradientEditText.textSize
            typeface = this@GradientEditText.typeface
            includeFontPadding = false // Disable font padding
        }

        // Initialize fill paint
        fillPaint.apply {
            style = Paint.Style.FILL
            isAntiAlias = true
            textSize = this@GradientEditText.textSize
            typeface = this@GradientEditText.typeface
            includeFontPadding = false // Disable font padding
        }

        // Initialize shader for gradient
        shader = LinearGradient(
            0f, 0f, 0f, textSize,
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = shader

        // Remove the default background to eliminate the grey underline
        background = null

        // Set text color to transparent to prevent default text rendering
        setTextColor(Color.TRANSPARENT)

        // Adjusted horizontal padding
        val extraPadding = dpToPx(2f)
        setPadding(
            (strokeWidth + extraPadding).toInt(),
            strokeWidth.toInt(),
            (strokeWidth + extraPadding).toInt(),
            strokeWidth.toInt()
        )

        // Set gravity to center horizontally
        gravity = gravity or android.view.Gravity.CENTER_HORIZONTAL
    }

    override fun onDraw(canvas: Canvas) {
        // Draw our custom text first
        val text = text?.toString() ?: ""
        val width = measuredWidth - paddingLeft - paddingRight

        // Save the canvas state
        val saveCount = canvas.save()

        // Translate the canvas to account for padding
        canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

        // Create layouts for stroke and fill text with center alignment
        val strokeLayout = createStaticLayout(text, strokePaint, width)
        val fillLayout = createStaticLayout(text, fillPaint, width)

        // Draw stroke text
        strokeLayout.draw(canvas)

        // Draw fill (gradient) text
        fillLayout.draw(canvas)

        // Restore the canvas state
        canvas.restoreToCount(saveCount)

        // Now draw the cursor, selection handles, and other components
        super.onDraw(canvas)
    }

    private fun createStaticLayout(text: String, textPaint: TextPaint, width: Int): StaticLayout {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(text, 0, text.length, textPaint, width)
                .setAlignment(Layout.Alignment.ALIGN_CENTER) // Set alignment to center
                .setLineSpacing(lineSpacingExtraCustom, lineSpacingMultiplierCustom)
                .setIncludePad(false) // Disable padding
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(
                text,
                textPaint,
                width,
                Layout.Alignment.ALIGN_CENTER, // Set alignment to center
                lineSpacingMultiplierCustom,
                lineSpacingExtraCustom,
                false /* includePad */
            )
        }
    }

    fun reInitialiseComponent(startColour: Int, endColour: Int) {
        startColor = context.getColor(startColour)
        endColor = context.getColor(endColour)

        // Update the shader for the fill paint
        shader = LinearGradient(
            0f, 0f, 0f, textSize,
            startColor,
            endColor,
            Shader.TileMode.CLAMP
        )
        fillPaint.shader = shader

        invalidate()
    }

    private fun dpToPx(dp: Float): Float {
        return dp * resources.displayMetrics.density
    }
}
