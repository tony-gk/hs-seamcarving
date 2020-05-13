package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import kotlin.math.sqrt

fun removeSeams(image: BufferedImage, count: Int): BufferedImage {
    var img = image

    var i = count
    while (i > 0 && img.width > 0) {
        img = removeSeam(img)
        i--
    }

    return img
}

private fun removeSeam(image: BufferedImage): BufferedImage {
    //the first value of pair indicates shortest path of vertical seam to the pixel
    //the second value indicates which pixel of the previous row we came from
    val path: Array<Array<Pair<Double, Int>>> = Array(image.width) { Array(image.height) { Pair(0.0, 0) } }

    calcShortestVerticalPaths(path, image)

    val seam = findSeam(path, image)

    for ((x, y) in seam) {
        shiftLeft(x, y, image)
    }

    return image.getSubimage(0, 0, image.width - 1, image.height)
}

private fun calcShortestVerticalPaths(path: Array<Array<Pair<Double, Int>>>, image: BufferedImage) {
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val energy = calcEnergy(x, y, image)
            if (y == 0) {
                path[x][y] = Pair(energy, 0)
            } else {
                val topLeft = if (x == 0) Double.MAX_VALUE else path[x - 1][y - 1].first
                val top = path[x][y - 1].first
                val topRight = if (x == image.width - 1) Double.MAX_VALUE else path[x + 1][y - 1].first

                if (topLeft <= top && topLeft <= topRight) {
                    path[x][y] = Pair(topLeft + energy, -1)
                } else if (top <= topRight && top <= topLeft) {
                    path[x][y] = Pair(top + energy, 0)
                } else if (topRight <= topLeft && topRight <= top) {
                    path[x][y] = Pair(topRight + energy, 1)
                }
            }
        }
    }
}

private fun findSeam(path: Array<Array<Pair<Double, Int>>>, image: BufferedImage): List<Pair<Int, Int>> {
    var bottomRowMin = Double.MAX_VALUE
    var coordOfBottomRowMin = -1
    val bottomRow = image.height - 1
    for (x in 0 until image.width) {
        if (path[x][bottomRow].first < bottomRowMin) {
            coordOfBottomRowMin = x
            bottomRowMin = path[x][bottomRow].first
        }
    }

    val seam = mutableListOf<Pair<Int, Int>>()
    var x = coordOfBottomRowMin
    var y = bottomRow
    while (y != -1) {
        seam.add(Pair(x, y))
        x += path[x][y].second
        y--
    }

    return seam
}

private fun correctCoord(x: Int, max: Int): Int {
    return when (x) {
        -1 -> 0
        max - 1 -> x - 1
        else -> x
    }
}

private fun calcEnergy(x: Int, y: Int, image: BufferedImage): Double {
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

private fun shiftLeft(x: Int, y: Int, image: BufferedImage) {
    var i = x
    while (i < image.width - 1) {
        image.setRGB(i, y, image.getRGB(i + 1, y))
        i++
    }
}
