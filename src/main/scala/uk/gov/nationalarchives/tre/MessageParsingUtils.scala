package uk.gov.nationalarchives.tre

import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder, parser}
import uk.gov.nationalarchives.common.messages.{Producer, Properties}
import uk.gov.nationalarchives.da.messages.courtdocumentpackage.available.{CourtDocumentPackageAvailable, Status}

object MessageParsingUtils {
  // Sample codecs for CourtDocumentPackageAvailable
  implicit val propertiesEncoder: Encoder[Properties] = deriveEncoder[Properties]
  implicit val producerEncoder: Encoder[Producer.Value] = Encoder.encodeEnumeration(Producer)
  implicit val producerDecoder: Decoder[Producer.Value] = Decoder.decodeEnumeration(Producer)
  implicit val statusEncoder: Encoder[Status.Value] = Encoder.encodeEnumeration(Status)
  implicit val statusDecoder: Decoder[Status.Value] = Decoder.decodeEnumeration(Status)

  // Example TRE message parsing
  def parseCourtDocumentPackageAvailableMessage(message: String): CourtDocumentPackageAvailable =
    parser.decode[CourtDocumentPackageAvailable](message).fold(error => throw new RuntimeException(error), identity)
}
