package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.util.*
import javax.imageio.ImageIO

fun main() {
    val input = Scanner(System.`in`)

    println("Enter rectangle width:")
    val width = input.nextInt()

    println("Enter rectangle height:")
    val height = input.nextInt();

    println("Enter output image name:")
    val fileName = input.next()

    createImageFile(width, height, fileName)
}

fun createImageFile(width: Int, height: Int, fileName: String) {
    val image = BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
    val graphics = image.graphics

    graphics.color = Color.RED
    graphics.drawLine(0, 0, width - 1, height - 1)
    graphics.drawLine(0, height - 1, width - 1, 0)

    ImageIO.write(image, "png", File(fileName))
}
