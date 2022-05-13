package software.blob.audio.tuner.view

import android.content.Context
import android.graphics.Color
import android.opengl.GLES30.*
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.core.math.MathUtils.clamp
import software.blob.audio.tuner.R
import software.blob.android.opengl.drawable.GLRectangle
import software.blob.audio.tuner.data.NoteCurve
import software.blob.audio.tuner.data.NoteSample
import software.blob.android.opengl.layer.GLBasic2DLayer
import software.blob.android.opengl.layer.GLCachedTextLayer
import software.blob.android.opengl.layer.GLLayer
import software.blob.audio.tuner.gl.layer.GLNoteCurveLayer
import software.blob.audio.tuner.gl.drawable.GLNoteCurve
import software.blob.android.opengl.shader.GLBasicShader
import software.blob.audio.tuner.preference.TunerPreferences
import software.blob.audio.tuner.util.getNoteName
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGL10.*
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.opengles.GL10
import kotlin.math.*

private const val TAG = "GraphMeterView"
private const val NOTE_WIDTH = 0.25

/**
 * Tuner view that shows pitch values on a graph
 */
class GraphMeterView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null)
    : GLSurfaceView(context, attrs),
    GLSurfaceView.Renderer,
    GLSurfaceView.EGLConfigChooser,
    ScaleGestureDetector.OnScaleGestureListener {

    private val prefs = TunerPreferences(context)
    private var startMillis = System.currentTimeMillis()
    private val density = resources.displayMetrics.density

    /**
     * Viewport parameters
     */
    private var vpWidth = 0
    private var vpHeight = 0
    private var latestNote = 48.0
    private var drawNote = latestNote
    private var minTime = 0.0
    private var minDrawNote = 0.0
    private var pixelsPerNote = 0.0
    private var pixelsPerSecond = 0.0

    /**
     * Gesture values
     */
    private val scaleDetector = ScaleGestureDetector(context, this)
    private var scaleActive = false
    private var startScaleX = Float.NaN
    private var startScaleY = Float.NaN
    private var startTimeRange = 1f
    private var startNoteRange = 1f

    /**
     * Shader used for drawing operations
     */
    private lateinit var shader: GLBasicShader
    private val clearColor = floatArrayOf(0f, 0f, 0f)
    private var textColor = Color.WHITE

    /**
     * Layers drawn to the view
     */
    private val layers = ArrayList<GLLayer>()
    private val shapeLayer = GLBasic2DLayer()
    private val curveLayer = GLNoteCurveLayer()
    private val textLayer = GLCachedTextLayer()
    private val noteBars1 = GLRectangle()
    private val noteBars2 = GLRectangle()

    /**
     * Preference values stored locally for performance
     */
    private var segmentColor = Color.RED
    private var noteRange = 0f
    private var timeRange = 0f
    private var timeThresh = 0f
    private var noteThresh = 0f
    private var focusLag = 0f

    init {

        // Preference listeners
        prefs.listenAndRun(R.string.pref_graph_time_range) {
            timeRange = prefs.graphTimeRange
        }
        prefs.listenAndRun(R.string.pref_graph_line_color) {
            segmentColor = prefs.graphLineColor
        }
        prefs.listenAndRun(R.string.pref_graph_time_thresh) {
            timeThresh = prefs.graphTimeThresh
        }
        prefs.listenAndRun(R.string.pref_graph_note_thresh) {
            noteThresh = prefs.graphNoteThresh
        }
        prefs.listenAndRun(R.string.pref_graph_note_range) {
            noteRange = prefs.graphNoteRange
        }
        prefs.listenAndRun(R.string.pref_graph_focus_lag) {
            focusLag = prefs.graphFocusLag
        }

        val bgColor = context.getColor(R.color.note_background)
        clearColor[0] = Color.red(bgColor).toFloat() / 255f
        clearColor[1] = Color.green(bgColor).toFloat() / 255f
        clearColor[2] = Color.blue(bgColor).toFloat() / 255f

        textColor = context.getColor(R.color.title_text)

        noteBars1.color = context.getColor(R.color.note_background_even)
        shapeLayer += noteBars1

        noteBars2.color = context.getColor(R.color.note_background_odd)
        shapeLayer += noteBars2

        // Layer draw order
        layers += shapeLayer
        layers += curveLayer
        layers += textLayer

        // Setup OpenGL
        setEGLConfigChooser(this)
        setEGLContextClientVersion(2)
        setRenderer(this)
    }

    /**
     * Add a note sample to the tuner view
     * @param note Note value
     */
    fun addSample(note: Double) {
        if (note <= 0 || note.isNaN()) return
        val sample = NoteSample(curTimeDelta(), note)
        queueEvent { addSample(sample) }
    }

    /**
     * Used to create the EGL config
     * @param egl Unused
     * @param display EGL display
     * @return EGL config
     */
    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig? {

        // Create configs and attributes
        val configs = arrayOfNulls<EGLConfig>(1)
        val configCounts = IntArray(1)
        val attribs = intArrayOf(
            EGL_LEVEL, 0,
            EGL_RENDERABLE_TYPE, 4,
            EGL_COLOR_BUFFER_TYPE, EGL_RGB_BUFFER,
            EGL_RED_SIZE, 8,
            EGL_GREEN_SIZE, 8,
            EGL_BLUE_SIZE, 8,
            EGL_DEPTH_SIZE, 16,
            // 4x MSAA
            EGL_SAMPLE_BUFFERS, 1,
            EGL_SAMPLES, 4,
            // End configs
            EGL_NONE
        )

        // Check if this configuration is supported
        if (!egl.eglChooseConfig(display, attribs, configs, 1, configCounts)) {
            Log.e(TAG, "Anti-alias config not supported on this device. Trying fallback...")

            // If it's not supported then try disabling anti-aliasing
            val fallback = intArrayOf(
                EGL_LEVEL, 0,
                EGL_RENDERABLE_TYPE, 4,
                EGL_COLOR_BUFFER_TYPE, EGL_RGB_BUFFER,
                EGL_RED_SIZE, 8,
                EGL_GREEN_SIZE, 8,
                EGL_BLUE_SIZE, 8,
                EGL_DEPTH_SIZE, 16,
                // End configs
                EGL_NONE
            )
            if (!egl.eglChooseConfig(display, fallback, configs, 1, configCounts))
                Log.e(TAG, "Fatal error: Failed to set EGL config", Throwable())
        }

        return configs[0]
    }

    /**
     * The GL surface has been created
     * @param gl Unused
     * @param config EGL config
     */
    override fun onSurfaceCreated(gl: GL10, config: EGLConfig?) {
        shader = GLBasicShader()
        glClearColor(clearColor[0], clearColor[1], clearColor[2], 1f)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
    }

    /**
     * The GL surface dimensions have been changed
     * @param gl Unused
     * @param width Width in pixels
     * @param height Height in pixels
     */
    override fun onSurfaceChanged(gl: GL10, width: Int, height: Int) {
        vpWidth = width
        vpHeight = height

        updateScales()

        for (layer in layers)
            layer.setViewport(width, height)
    }

    /**
     * Frame is ready to be drawn
     * @param gl Unused
     */
    override fun onDrawFrame(gl: GL10) {
        glClear(GL_COLOR_BUFFER_BIT)

        val curTime = curTimeDelta()
        minTime = curTime - timeRange

        val lagSlide = max(1f - focusLag, 0.05f).pow(2)

        drawNote = if (drawNote == 0.0) latestNote else
            drawNote * (1 - lagSlide) + latestNote * lagSlide

        val noteRangeHalf = noteRange / 2
        val curMax = drawNote + noteRangeHalf + NOTE_WIDTH
        minDrawNote = drawNote - noteRangeHalf - NOTE_WIDTH
        val curMin = minDrawNote
        val w = vpWidth.toFloat()
        val minNote = floor(curMin).toInt()
        val maxNote = ceil(curMax).toInt() + 1

        // Update the matrix used to project notes to pixels
        curveLayer.setNoteMatrix(minTime, minDrawNote, pixelsPerSecond, pixelsPerNote)

        // Set the color of the curves
        curveLayer.color = segmentColor

        // Draw the note bars and names in the background
        noteBars1.resetVertices()
        noteBars2.resetVertices()
        textLayer.setTextVisible(false)
        for (note in minNote..maxNote) {
            val maxY = getY(note - NOTE_WIDTH)
            val minY = getY(note + NOTE_WIDTH)
            val h = maxY - minY
            val bars = if ((note % 2) == 0) noteBars1 else noteBars2
            bars.addRectangle(0f, minY, w, minY + h)

            val noteName = getNoteName(note)
            val text = textLayer[noteName]
            text.x = 10f
            text.y = minY + (h - text.height) / 2
            text.textColor = textColor
            text.textSize = 12 * density
            text.visible = true
        }

        // Draw the layers
        for (layer in layers)
            layer.draw(shader)

        // Prune curves that are no longer on screen
        pruneCurves()
    }

    /**
     * Handles scale gesture
     * @param event Motion event
     * @return True if handled
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return scaleDetector.onTouchEvent(event)
    }

    /**
     * Scale controls for the time and note range
     * @param detector Scale detector
     * @return True if handled
     */
    override fun onScale(detector: ScaleGestureDetector): Boolean {
        if (!scaleActive) return false
        val scaleX = if (startScaleX > 0) startScaleX / detector.currentSpanX else 1f
        val scaleY = if (startScaleY > 0) startScaleY / detector.currentSpanY else 1f
        val newTimeRange = clamp(startTimeRange * scaleX, 0.5f, 10f)
        val newNoteRange = clamp(startNoteRange * scaleY, 5f, 12f)
        queueEvent {
            timeRange = newTimeRange
            noteRange = newNoteRange
            updateScales()
        }
        return true
    }

    /**
     * Scale controls for the time and note range
     * @param detector Scale detector
     * @return True if handled
     */
    override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
        startScaleX = detector.currentSpanX
        startScaleY = detector.currentSpanY
        val dir = Math.toDegrees(atan2(startScaleX, startScaleY).toDouble())
        if (dir < 30)
            startScaleX = 0f
        else if (dir >= 60)
            startScaleY = 0f
        startTimeRange = timeRange
        startNoteRange = noteRange
        scaleActive = true
        return true
    }

    /**
     * Scale controls for the time and note range
     * @param detector Scale detector
     */
    override fun onScaleEnd(detector: ScaleGestureDetector) {
        if (!scaleActive) return
        prefs.graphTimeRange = timeRange
        prefs.graphNoteRange = noteRange
    }

    /**
     * Add a sample to the view
     * @param sample Note sample to add
     */
    private fun addSample(sample: NoteSample) {

        // Out of the existing curves find the one closest to the current sample
        var minNote = sample.note
        var maxNote = sample.note
        var curve: GLNoteCurve? = null
        val curTime = curTimeDelta()
        for (o in curveLayer) {
            val c = o as GLNoteCurve
            val cLast = c.last()
            if (cLast != null) {
                if (curve == null && (curTime - cLast.time) <= timeThresh &&
                    cLast.note - sample.note < noteThresh)
                    curve = c
                minNote = min(minNote, cLast.note)
                maxNote = max(maxNote, cLast.note)
            }
        }

        // Create a new curve if needed
        if (curve == null) {
            curve = GLNoteCurve(NoteCurve()).also {
                it.lineWidth = density
                curveLayer += it
            }
        }

        // Set the display focus
        val noteRange = maxNote - minNote
        latestNote = if (noteRange < noteRange) (minNote + maxNote) / 2.0 else sample.note

        // Finally add the sample
        curve += sample
    }

    /**
     * Remove curves that are no longer on screen
     */
    private fun pruneCurves() {
        var i = 0
        while (i < curveLayer.size) {
            val cLast = (curveLayer[i] as GLNoteCurve).last()
            if (cLast == null || cLast.time < minTime)
                curveLayer.removeAt(i--)
            i++
        }
    }

    /**
     * Update pixel scales for time and note values
     */
    private fun updateScales() {
        pixelsPerNote = vpHeight.toDouble() / noteRange
        pixelsPerSecond = vpWidth.toDouble() / timeRange
    }

    /**
     * Get the current time delta
     * @return Time delta in seconds
     */
    private fun curTimeDelta() = (System.currentTimeMillis() - startMillis) / 1000.0

    /**
     * Get the screen Y value given a note
     * @param note Note value
     * @return Corresponding Y value
     */
    private fun getY(note: Double) = vpHeight - ((note - minDrawNote) * pixelsPerNote).toFloat()
}