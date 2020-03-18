package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.sqrt

fun main(args: Array<String>) {
    try {
        val files = retrieveFiles(args)
        createNegativeImage(files.first, files.second)
    } catch (e: ImageProcessingException) {
        System.err.println(e.message)
    }
}

fun retrieveFiles(args: Array<String>): Pair<File, File> {
    if (args.size != 4) {
        throw ImageProcessingException("Usage : java Main -in <input> -out <output>")
    }

    var input: String? = null
    var output: String? = null

    for (i in 0..args.size - 2) {
        when (args[i]) {
            "-in" -> input = args[i + 1]
            "-out" -> output = args[i + 1]
        }
    }

    if (input == null || output == null) {
        throw ImageProcessingException("Usage : java Main -in <input> -out <output>")
    }

    if (Files.notExists(Paths.get(input))) {
        throw ImageProcessingException("Input file doesn't exist")
    }

    return Pair(File(input), File(output))
}

fun createNegativeImage(input: File, output: File) {
    val image: BufferedImage

    try {
        image = ImageIO.read(input)
    } catch (e: IOException) {
        throw ImageProcessingException("Can't read input file: ${input.absolutePath}")
    }

    convertToEnergyImage(image)

    try {
        ImageIO.write(image, "png", output)
    } catch (e: IOException) {
        throw ImageProcessingException("Can't write output file: ${output.absolutePath}")
    }
}

fun convertToEnergyImage(image: BufferedImage) {
    val energy: Array<DoubleArray> = Array(image.width) { DoubleArray(image.height) }

    var maxEnergy = Double.MIN_VALUE
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            energy[x][y] = calcEnergy(x, y, image)
            maxEnergy = max(energy[x][y], maxEnergy)
        }
    }

    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            val intensity = (255.0 * energy[x][y] / maxEnergy).toInt()
            image.setRGB(x, y, Color(intensity, intensity, intensity).rgb)
        }
    }
}

fun correctCoord(x: Int, max: Int): Int {
    return when (x) {
        -1 -> 0
        max - 1 -> x - 1
        else -> x
    }
}

fun calcEnergy(x: Int, y: Int, image: BufferedImage): Double {
    val leftX = correctCoord(x - 1, image.width - 1)
    val topY = correctCoord(y - 1, image.height - 1)

    val leftColor = Color(image.getRGB(leftX, y))
    val rightColor = Color(image.getRGB(leftX + 2, y))
    val topColor = Color(image.getRGB(x, topY))
    val downColor = Color(image.getRGB(x, topY + 2))

    val xDiff = intArrayOf(leftColor.red - rightColor.red, leftColor.green - rightColor.green, leftColor.blue - rightColor.blue)
    val yDiff = intArrayOf(topColor.red - downColor.red, topColor.green - downColor.green, topColor.blue - downColor.blue)

    var xGradient = 0
    var yGradient = 0
    for (i in 0..2) {
        xGradient += xDiff[i] * xDiff[i]
        yGradient += yDiff[i] * yDiff[i]
    }

    return sqrt((xGradient + yGradient).toDouble())
}

class ImageProcessingException(message: String) : Exception(message)

