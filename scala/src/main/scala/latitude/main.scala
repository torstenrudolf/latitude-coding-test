package latitude

import java.io.File

import cats.effect.{ExitCode, IO, IOApp}


case class Config(colNames: Vector[String],
                  colWidths: Vector[Int],
                  padding: Char,
                  numLines: Int,
                  fixedWidthPath: String,
                  csvPath: String,
                  csvSep: String)

object main extends IOApp {
  
  val dataPath = Option(System.getenv("DATA_DIR")).getOrElse("data")

  val config = Config(
    colNames = Vector(
      "f1",
      "f2",
      "f3",
      "f4",
      "f5",
      "f6",
      "f7",
      "f8",
      "f9",
      "f10"
    ),
    colWidths = Vector(
      3,
      12,
      3,
      2,
      13,
      1,
      10,
      13,
      3,
      13
    ),
    padding = ' ',
    numLines = 128,
    fixedWidthPath = s"$dataPath/fixedWidth.txt",
    csvPath = s"$dataPath/data.csv",
    csvSep = ","
  )

  def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- IO{
        val dataDir = new File(dataPath)
        if (!dataDir.exists()) dataDir.mkdir()
      }
      _ <- fixedWidthFileGen.Generator(config.colWidths, config.padding, config.numLines, config.fixedWidthPath)
      _ <- convert.Converter(config.fixedWidthPath, config.csvPath, config.colNames, config.colWidths, config.padding, config.csvSep)
    } yield ExitCode.Success
  }

}
