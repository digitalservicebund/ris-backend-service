package de.bund.digitalservice.ris.norms.framework.adapter.output.s3

import de.bund.digitalservice.ris.norms.application.port.output.GetFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.getHashFromContent
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.core.async.AsyncResponseTransformer
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.ByteBuffer

@Component
class FilesService(
    val s3AsyncClient: S3AsyncClient,
    @Value("\${otc.obs.bucket-name}")
    val bucketName: String? = null,
) : SaveFileOutputPort, GetFileOutputPort {

    private val folder: String = "norms/"

    companion object {
        var logger: Logger = LoggerFactory.getLogger(FilesService::class.java)
    }

    override fun saveFile(command: SaveFileOutputPort.Command): Mono<Boolean> {
        val mediaType = MediaType.APPLICATION_OCTET_STREAM

        val asyncRequestBody = AsyncRequestBody.fromPublisher(
            Mono.just(ByteBuffer.wrap(command.file)),
        )
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(folder + getHashFromContent(command.file))
            .contentType(mediaType.toString())
            .build()

        logger.info("saveFile - PutObjectRequest: $putObjectRequest")

        return Mono.fromCallable {
            Mono.fromFuture(s3AsyncClient.putObject(putObjectRequest, asyncRequestBody)).doOnNext{logger.info("saveFile responseObject: $it.toString()")}
        }.flatMap {
            Mono.just(true) }
    }

    override fun getFile(query: GetFileOutputPort.Query): Mono<ByteArray> {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(folder + query.hash)
            .build()

        logger.info("getFile - GetObjectRequest: $getObjectRequest")

        return Mono.fromCallable {
            Mono.fromFuture(s3AsyncClient.getObject(getObjectRequest, AsyncResponseTransformer.toBytes()))
                .map { response ->
                    response.asByteArray()
                }
        }.flatMap { it }
    }
}
