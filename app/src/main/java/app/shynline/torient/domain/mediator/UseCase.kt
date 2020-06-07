package app.shynline.torient.domain.mediator

abstract class UseCase<INPUT, OUTPUT> {

    suspend operator fun invoke(input: INPUT): OUTPUT {
        return execute(input)
    }

    protected abstract suspend fun execute(input: INPUT): OUTPUT
}