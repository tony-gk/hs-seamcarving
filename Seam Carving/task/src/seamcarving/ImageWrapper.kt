package seamcarving

import java.awt.image.BufferedImage

class ImageWrapper(private val image: BufferedImage) {
    private var rotated = false

    var height = 0
        get() = if (!rotated) image.height else image.width
        private set

    var width = 0
        get() = if (!rotated) image.width else image.height
        private set

    fun getImage() = image

    fun rotate() {
        rotated = !rotated
    }

    fun getRGB(x: Int, y: Int): Int {
        return when (rotated) {
            false -> image.getRGB(x, y)
            true -> image.getRGB(y, x)
        }
    }

    fun setRGB(x: Int, y: Int, rgb: Int) {
        when (rotated) {
            false -> image.setRGB(x, y, rgb)
            true -> image.setRGB(y, x, rgb)
        }
    }
}