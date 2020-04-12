package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO
import kotlin.math.sqrt

fun main(args: Array<String>) {
    try {
        val files = retrieveFiles(args)
        createSeamImage(files.first, files.second)
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

fun createSeamImage(input: File, output: File) {
    val image: BufferedImage

    try {
        image = ImageIO.read(input)
    } catch (e: IOException) {
        throw ImageProcessingException("Can't read input file: ${input.absolutePath}")
    }

    highlightSeam(image, Color(255, 0, 0))

    try {
        ImageIO.write(image, "png", output)
    } catch (e: IOException) {
        throw ImageProcessingException("Can't write output file: ${output.absolutePath}")
    }
}

fun highlightSeam(image: BufferedImage, color: Color) {
    //the first value of pair indicates shortest path of vertical seam to the pixel
    //the second value indicates which pixel of the previous row we came from
    val pixels: Array<Array<Pair<Double, Int>>> = Array(image.width) { Array(image.height) { Pair(0.0, 0) } }

    calcShortestVerticalPaths(pixels, image)

    highlightShortestPath(pixels, image, color)
}

fun calcShortestVerticalPaths(pixels: Array<Array<Pair<Double, Int>>>, image: BufferedImage) {
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val energy = calcEnergy(x, y, image)
            if (y == 0) {
                pixels[x][y] = Pair(energy, 0)
            } else {
                val topLeft = if (x == 0) Double.MAX_VALUE else pixels[x - 1][y - 1].first
                val top = pixels[x][y - 1].first
                val topRight = if (x == image.width - 1) Double.MAX_VALUE else pixels[x + 1][y - 1].first

                if (topLeft <= top && topLeft <= topRight) {
                    pixels[x][y] = Pair(topLeft + energy, -1)
                } else if (top <= topRight && top <= topLeft) {
                    pixels[x][y] = Pair(top + energy, 0)
                } else if (topRight <= topLeft && topRight <= top) {
                    pixels[x][y] = Pair(topRight + energy, 1)
                }
            }
        }
    }
}

fun highlightShortestPath(pixels: Array<Array<Pair<Double, Int>>>, image: BufferedImage, color: Color) {
    var bottomRowMin = Double.MAX_VALUE
    var coordOfBottomRowMin = -1
    val bottomRow = image.height - 1
    for (x in 0 until image.width) {
        if (pixels[x][bottomRow].first < bottomRowMin) {
            coordOfBottomRowMin = x
            bottomRowMin = pixels[x][bottomRow].first
        }
    }

    println(bottomRowMin)

    var x = coordOfBottomRowMin
    var y = bottomRow
    while (y != -1) {
        image.setRGB(x, y, color.rgb)
        x += pixels[x][y].second
        y--
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