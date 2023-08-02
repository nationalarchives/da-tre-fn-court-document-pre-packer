Feature: This lambda function will handle a CourtDocumentParse message and return a CourtDocumentPackagePrepare message

  Scenario: An SNS event is handled by the lambda handler
    When an SNS event is received with message content:
    """
      {
        "properties" : {
          "messageType" : "uk.gov.nationalarchives.tre.messages.courtdocument.parse.CourtDocumentParse",
          "timestamp" : "2023-03-29T11:00:12.280Z",
          "function" : "da-tre-tf-module-court-document-parse",
          "producer" : "TRE",
          "executionId" : "executionId344",
          "parentExecutionId" : null
        },
        "parameters" : {
          "status": "COURT_DOCUMENT_PARSE_NO_ERRORS",
          "originator" : "FCL",
          "s3FolderName" : "court-documents/FCL-151/2545ffce-a313-4824-8db5-9c7e5debd1cf",
          "s3Bucket" : "pte-ah-tre-common-data",
          "reference" : "FCL-151"
        }
      }
    """

    Then a message is returned containing json data:
      | properties.messageType   | uk.gov.nationalarchives.tre.messages.courtdocumentpackage.prepare.CourtDocumentPackagePrepare |
      | properties.function      | da-tre-fn-module-court-document-package-prepare                                               |
      | parameters.status        | COURT_DOCUMENT_PARSE_NO_ERRORS                                                                |

