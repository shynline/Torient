package app.shynline.torient.torrent.mediator

abstract class UseCase<INPUT, OUTPUT> {

    suspend operator fun invoke(input: INPUT): OUTPUT {
        return execute(input)
    }

    abstract suspend fun execute(input: INPUT): OUTPUT
}