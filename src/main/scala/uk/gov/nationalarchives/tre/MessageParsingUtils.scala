package uk.gov.nationalarchives.tre

import io.circe.generic.semiauto.deriveEncoder
import io.circe.generic.auto._
import io.circe.{Decoder, Encoder, parser}
import uk.gov.nationalarchives.common.messages.{Producer, Properties, Status}
import uk.gov.nationalarchives.tre.messages.courtdocument.parse.CourtDocumentParse
import uk.gov.nationalarchives.tre.messages.courtdocumentpackage.prepare.CourtDocumentPackagePrepare

object MessageParsingUtils {
  implicit val propertiesEncoder: Encoder[Properties] = deriveEncoder[Properties]
  implicit val producerEncoder: Encoder[Producer.Value] = Encoder.encodeEnumeration(Producer)
  implicit val producerDecoder: Decoder[Producer.Value] = Decoder.decodeEnumeration(Producer)
  implicit val statusEncoder: Encoder[Status.Value] = Encoder.encodeEnumeration(Status)
  implicit val statusDecoder: Decoder[Status.Value] = Decoder.decodeEnumeration(Status)
  implicit val courtDocumentPackagePrepareEncoder: Encoder[CourtDocumentPackagePrepare] = deriveEncoder[CourtDocumentPackagePrepare]

  def parseCourtDocumentParseMessage(message: String): CourtDocumentParse =
    parser.decode[CourtDocumentParse](message).fold(error => throw new RuntimeException(error), identity)
}
