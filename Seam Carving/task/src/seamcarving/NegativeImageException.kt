package seamcarving

class NegativeImageException : Exception {
    constructor(message: String) : super(message)

    constructor(message: String, e: Throwable) : super(message, e)
}