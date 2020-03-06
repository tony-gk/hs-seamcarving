package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

fun main(args: Array<String>) {
    try {
        val files = retrieveFiles(args)
        createNegativeImage(files.first, files.second)
    } catch (e: NegativeImageException) {
        System.err.println(e.message)
    }
}

fun retrieveFiles(args: Array<String>): Pair<File, File> {
    if (args.size != 4) {
        throw NegativeImageException("Usage : java Main -in <input> -out <output>")
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
        throw NegativeImageException("Usage : java Main -in <input> -out <output>")
    }

    if (Files.notExists(Paths.get(input))) {
        throw NegativeImageException("Input file doesn't exist")
    }

    return Pair(File(input), File(output))
}

fun createNegativeImage(input: File, output: File) {
    val image: BufferedImage

    try {
        image = ImageIO.read(input)
    } catch (e: IOException) {
        throw NegativeImageException("Can't read input file: ${input.absolutePath}")
    }

    negateImage(image)

    try {
        ImageIO.write(image, "png", output)
    } catch (e :IOException) {
        throw NegativeImageException("Can't write output file: ${output.absolutePath}")
    }
}

fun negateImage(image: BufferedImage) {
    for (x in 0 until image.width) {
        for (y in 0 until image.height) {
            var color = Color(image.getRGB(x, y))

            color = Color(255 - color.red,
                    255 - color.green,
                    255 - color.blue)

            image.setRGB(x, y, color.rgb)
        }
    }
}



