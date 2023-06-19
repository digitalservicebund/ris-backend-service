package de.bund.digitalservice.ris.norms.application.service

import de.bund.digitalservice.ris.norms.application.port.input.GenerateNormFileUseCase
import de.bund.digitalservice.ris.norms.application.port.output.GenerateNormFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.GetNormByGuidOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileOutputPort
import de.bund.digitalservice.ris.norms.application.port.output.SaveFileReferenceOutputPort
import de.bund.digitalservice.ris.norms.domain.entity.FileReference
import de.bund.digitalservice.ris.norms.domain.entity.Norm
import de.bund.digitalservice.ris.norms.domain.entity.getHashFromContent
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class GenerateNormFileService(
    private val getNormByGuidOutputPort: GetNormByGuidOutputPort,
    private val getFileOutputPort: GetFileOutputPort,
    private val generateNormFileOutputPort: GenerateNormFileOutputPort,
    private val saveFileOutputPort: SaveFileOutputPort,
    private val saveFileReferenceOutputPort: SaveFileReferenceOutputPort,
) : GenerateNormFileUseCase {

    inner class StreamDataBox(val file: ByteArray, var norm: Norm, val lastFileReference: FileReference)
    companion object {
        private val logger = LoggerFactory.getLogger(GenerateNormFileService::class.java)
    }

    override fun generateNormFile(command: GenerateNormFileUseCase.Command): Mono<FileReference> {
        val queryGetNorm = GetNormByGuidOutputPort.Query(command.guid)
        return getNormByGuidOutputPort.getNormByGuid(queryGetNorm)
            .switchIfEmpty(Mono.empty())
            .flatMap { getFileFromBucket(it) }
            .flatMap { generateNewZip(it) }
            .flatMap { saveFile(it) }
            .flatMap { createNewFileReference(it) }
            .doOnError {
                    exception ->
                logger.error("Error occurred while generating the norm file:", exception)
            }
    }

    private fun getFileFromBucket(norm: Norm): Mono<StreamDataBox> {
        val lastCreatedFile = norm.files.maxBy { it.createdAt }
        val queryGetFile = GetFileOutputPort.Query(lastCreatedFile.hash)
        return getFileOutputPort.getFile(queryGetFile).map { previousFile ->
            StreamDataBox(previousFile, norm, lastCreatedFile)
        }
    }

    private fun generateNewZip(streamDataBox: StreamDataBox): Mono<StreamDataBox> {
        val commandGenerateNormFile = GenerateNormFileOutputPort.Command(streamDataBox.norm, streamDataBox.file)
        return generateNormFileOutputPort.generateNormFile(commandGenerateNormFile)
            .map { generatedFile -> StreamDataBox(generatedFile, streamDataBox.norm, streamDataBox.lastFileReference) }
    }

    private fun saveFile(streamDataBox: StreamDataBox): Mono<StreamDataBox> {
        val commandSaveFile = SaveFileOutputPort.Command(streamDataBox.file, streamDataBox.lastFileReference.name, streamDataBox.file.size.toLong())
        return saveFileOutputPort.saveFile(commandSaveFile)
            .map { streamDataBox }
    }

    private fun createNewFileReference(streamDataBox: StreamDataBox): Mono<FileReference> {
        val fileReference = FileReference(streamDataBox.lastFileReference.name, getHashFromContent(streamDataBox.file))
        val commandSaveFileReference = SaveFileReferenceOutputPort.Command(fileReference, streamDataBox.norm)
        return saveFileReferenceOutputPort.saveFileReference(commandSaveFileReference)
            .flatMap { Mono.just(fileReference) }
    }
}
