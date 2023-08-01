Feature: This lambda function will handle a lambda destination event

  Scenario: A lambda destination event is handled by the lambda handler
    When a lambda destination event is received with payload
    """
    {
        "properties": {
          "messageType": "uk.gov.nationalarchives.tre.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable"
        },
        "parameters": {
           "status": "COURT_DOCUMENT_PARSE_NO_ERRORS",
           "reference": "TRE-TEST",
           "s3Bucket": "test-tre-court-document-pack-out",
           "s3Key": "TRE-TEST/execution-id/TRE-TEST.tar.gz",
           "metadataFilePath": "/metadata.json",
           "metadataFileType": "Json"
        }
    }
    """

    Then a lambda destination event is logged with payload
    """
    {
        "properties": {
          "messageType": "uk.gov.nationalarchives.tre.messages.courtdocumentpackage.available.CourtDocumentPackageAvailable"
        },
        "parameters": {
           "status": "COURT_DOCUMENT_PARSE_NO_ERRORS",
           "reference": "TRE-TEST",
           "s3Bucket": "test-tre-court-document-pack-out",
           "s3Key": "TRE-TEST/execution-id/TRE-TEST.tar.gz",
           "metadataFilePath": "/metadata.json",
           "metadataFileType": "Json"
        }
    }
    """
