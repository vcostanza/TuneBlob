package software.blob.audio.tuner.gl.drawable

import android.opengl.GLES30.*
import software.blob.android.opengl.drawable.GLShape
import software.blob.audio.tuner.data.NoteCurve
import software.blob.audio.tuner.data.NoteSample

class GLNoteCurve(var impl: NoteCurve) : GLShape(100) {

    init {
        drawMode = GL_LINE_STRIP
    }

    /**
     * Redirect for [NoteCurve.size]
     * @return The amount of samples in this curve
     */
    val size: Int get() {
        return impl.size
    }

    /**
     * Redirect for [NoteCurve.get]
     * @param index Sample index
     * @return Note sample at the given index
     */
    operator fun get(index: Int): NoteSample {
        return impl[index]
    }

    /**
     * Redirect for [NoteCurve.add]
     * @param sample Sample to add
     */
    operator fun plusAssign(sample: NoteSample) {
        impl += sample
        addVertex(sample.time.toFloat(), sample.note.toFloat())
    }

    /**
     * Redirect for [NoteCurve.isEmpty]
     * @return True if curve is empty
     */
    fun isEmpty(): Boolean {
        return impl.isEmpty()
    }

    /**
     * Redirect for [NoteCurve.last]
     * @return Last sample in the curve
     */
    fun last(): NoteSample? {
        return impl.last()
    }
}