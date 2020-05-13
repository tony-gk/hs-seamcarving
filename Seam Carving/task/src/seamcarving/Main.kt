package seamcarving

import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import java.lang.NumberFormatException
import java.nio.file.Files
import java.nio.file.Paths
import javax.imageio.ImageIO

class ImageProcessingException(message: String) : Exception(message)

fun main(args: Array<String>) {
    try {
        val (input, output, width, height) = parseArgs(args)
        resizeImage(File(input), File(output), width, height)
    } catch (e: ImageProcessingException) {
        System.err.println(e.message)
    } catch (e: NumberFormatException) {
        System.err.println("Width and height must be integer")
    }
}

fun invalidUsage(): Nothing =
        throw ImageProcessingException(
                "Usage : java Main -in <input> -out <output> [-width <width>] [-height <height]"
        )

fun parseArgs(args: Array<String>): Arguments {
    if (args.size !in 4..8 step 2) {
        invalidUsage()
    }

    val arguments = Arguments()

    for (i in 0..args.size - 2) {
        when (args[i]) {
            "-in" -> arguments.inputFileName = args[i + 1]
            "-out" -> arguments.outputFileName = args[i + 1]
            "-width" -> arguments.width = args[i + 1].toInt()
            "-height" -> arguments.height = args[i + 1].toInt()
        }
    }

    if (arguments.inputFileName.isBlank() || arguments.outputFileName.isBlank()) {
        invalidUsage()
    }

    if (Files.notExists(Paths.get(arguments.inputFileName))) {
        throw ImageProcessingException("Input file doesn't exist")
    }

    return arguments
}

fun resizeImage(input: File, output: File, width: Int, height: Int) {
    var image: BufferedImage

    try {
        image = ImageIO.read(input)
    } catch (e: IOException) {
        throw ImageProcessingException("Can't read input file: ${input.absolutePath}")
    }

    image = removeSeams(image, width)
    image = rotate90(image, false)
    image = removeSeams(image, height)
    image = rotate90(image, true)

    try {
        ImageIO.write(image, "png", output)
    } catch (e: IOException) {
        throw ImageProcessingException("Can't write output file: ${output.absolutePath}")
    }
}


