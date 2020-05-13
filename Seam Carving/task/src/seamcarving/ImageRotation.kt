package seamcarving

import java.awt.geom.AffineTransform
import java.awt.image.AffineTransformOp
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun rotate90(image: BufferedImage, clockwise: Boolean): BufferedImage {
    var angle = Math.PI / 2
    if (clockwise) {
        angle = -angle
    }

    val rotatedWidth = image.height
    val rotatedHeight = image.width
    val rotatedImage = BufferedImage(rotatedWidth, rotatedHeight, image.type)

    val at = AffineTransform()
    at.translate(rotatedWidth.toDouble() / 2, rotatedHeight.toDouble() / 2)
    at.rotate(angle, 0.0, 0.0)
    at.translate(-image.width.toDouble() / 2, -image.height.toDouble() / 2)

    val rotateOp = AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR)
    rotateOp.filter(image, rotatedImage)

    return rotatedImage
}