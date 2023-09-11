package uk.gov.nationalarchives.tre

import org.scalatest.flatspec._
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{JsNull, JsString, Json}

class MetadataConstructionUtilsSpec extends AnyFlatSpec {
  "buildMetadataFileContents" should "build expected metadata file content with appropriate indentation" in {
    val expectedFileContent = """{
      |  "parameters" : {
      |    "TRE" : {
      |      "reference" : "FCL-151",
      |      "payload" : {
      |        "filename" : "eat_2022_1.docx",
      |        "xml" : "FCL-151.xml",
      |        "metadata" : "TRE-FCL-151-metadata.json",
      |        "images" : [ ],
      |        "log" : "parser.log"
      |      }
      |    },
      |    "PARSER" : {
      |      "uri" : "https://caselaw.nationalarchives.gov.uk/id/eat/2022/1",
      |      "court" : "EAT",
      |      "cite" : "[2022] EAT 1",
      |      "date" : "2021-09-28",
      |      "name" : "SECRETARY OF STATE FOR JUSTICE v MR ALAN JOHNSON",
      |      "attachments" : [ ],
      |      "error-messages" : [ ]
      |    }
      |  }
      |}""".stripMargin
    val actualFileContent = MetadataConstructionUtils.buildMetadataFileContents(
      reference = "FCL-151",
      fileNames = Seq("eat_2022_1.docx", "FCL-151.xml", "metadata.json", "parser.log"),
      metadataFileName = "TRE-FCL-151-metadata.json",
      parserMetadata =
        Json.obj(
          "uri" -> "https://caselaw.nationalarchives.gov.uk/id/eat/2022/1",
          "court" -> "EAT",
          "cite" -> "[2022] EAT 1",
          "date" -> "2021-09-28",
          "name" -> "SECRETARY OF STATE FOR JUSTICE v MR ALAN JOHNSON",
          "attachments" -> Json.arr(),
          "error-messages" -> Json.arr()
        ),
      parserOutputs = Json.obj("xml" -> "FCL-151.xml", "log" -> "parser.log")
    )
    expectedFileContent shouldBe actualFileContent
  }

  it should "include an array of image names if present in the parser output" in {
    val expectedFileContent =
      """{
        |  "parameters" : {
        |    "TRE" : {
        |      "reference" : "FCL-151",
        |      "payload" : {
        |        "filename" : "eat_2022_1.docx",
        |        "xml" : "FCL-151.xml",
        |        "metadata" : "TRE-FCL-151-metadata.json",
        |        "images" : [ "image1.png", "image2.jpeg" ],
        |        "log" : "parser.log"
        |      }
        |    },
        |    "PARSER" : {
        |      "error-messages" : [ ]
        |    }
        |  }
        |}""".stripMargin
    val actualFileContent = MetadataConstructionUtils.buildMetadataFileContents(
      reference = "FCL-151",
      fileNames = Seq("eat_2022_1.docx", "FCL-151.xml", "metadata.json", "parser.log"),
      metadataFileName = "TRE-FCL-151-metadata.json",
      parserMetadata = Json.obj(),
      parserOutputs = Json.obj(
        "xml" -> "FCL-151.xml",
        "log" -> "parser.log",
        "images" -> Json.arr("image1.png", "image2.jpeg")
      )
    )
    expectedFileContent shouldBe actualFileContent
  }

  it should "return an empty array for the images field if the parser output has a null value for images" in {
    val expectedFileContent =
      """{
        |  "parameters" : {
        |    "TRE" : {
        |      "reference" : "FCL-151",
        |      "payload" : {
        |        "filename" : "eat_2022_1.docx",
        |        "xml" : "FCL-151.xml",
        |        "metadata" : "TRE-FCL-151-metadata.json",
        |        "images" : [ ],
        |        "log" : "parser.log"
        |      }
        |    },
        |    "PARSER" : {
        |      "error-messages" : [ ]
        |    }
        |  }
        |}""".stripMargin
    val actualFileContent = MetadataConstructionUtils.buildMetadataFileContents(
      reference = "FCL-151",
      fileNames = Seq("eat_2022_1.docx", "FCL-151.xml", "metadata.json", "parser.log"),
      metadataFileName = "TRE-FCL-151-metadata.json",
      parserMetadata = Json.obj(),
      parserOutputs = Json.obj(
        "xml" -> "FCL-151.xml",
        "log" -> "parser.log",
        "images" -> null
      )
    )
    expectedFileContent shouldBe actualFileContent
  }

  it should "return error messages in the parser metadata if there are errors in the parser output" in {
    val expectedFileContent =
      """{
        |  "parameters" : {
        |    "TRE" : {
        |      "reference" : "FCL-151",
        |      "payload" : {
        |        "filename" : null,
        |        "xml" : null,
        |        "metadata" : "TRE-FCL-151-metadata.json",
        |        "images" : [ ],
        |        "log" : "parser.log"
        |      }
        |    },
        |    "PARSER" : {
        |      "error-messages" : [ "error reading .docx file" ]
        |    }
        |  }
        |}""".stripMargin
    val actualFileContent = MetadataConstructionUtils.buildMetadataFileContents(
      reference = "FCL-151",
      fileNames = Seq("parser.log"),
      metadataFileName = "TRE-FCL-151-metadata.json",
      parserMetadata = Json.obj(),
      parserOutputs = Json.obj(
        "xml" -> null,
        "log" -> "parser.log",
        "images" -> null,
        "error-messages" -> Json.arr("error reading .docx file")
      )
    )
    expectedFileContent shouldBe actualFileContent
  }


  "getFileNameWithSuffix" should "return a filename with the given suffix when present" in {
    val expectedFileName = JsString("eat_2022_1.docx")
    val actualFileName = MetadataConstructionUtils
      .getFileNameWithSuffix(".docx")(Seq("eat_2022_1.docx", "FCL-151.xml", "metadata.json", "parser.log"))
    expectedFileName shouldBe actualFileName
  }

  it should "strip out any leading directory and return the filename only" in {
    val expectedDocxFileName = JsString("eat_2022_1.docx")
    val actualDocxFileName = MetadataConstructionUtils
      .getFileNameWithSuffix(".docx")(Seq("data/eat_2022_1.docx", "FCL-151.xml", "metadata.json", "parser.log"))
    expectedDocxFileName shouldBe actualDocxFileName
  }

  it should "return null if no files with the given suffix are present" in {
    val expectedFileName = JsNull
    val actualFileName = MetadataConstructionUtils.getFileNameWithSuffix(".docx")(Seq("parser.log"))
    expectedFileName shouldBe actualFileName
  }
}
