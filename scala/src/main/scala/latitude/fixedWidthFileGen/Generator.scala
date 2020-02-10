/**
 * Generate a fixed width file according to the given spec. populate with random data.
 *
 * spec:
 * {
 * "ColumnNames":"f1, f2, f3, f4, f5, f6, f7, f8, f9, f10",
 * "Offsets":"3,12,3,2,13,1,10,13,3,13",
 * "encoding":"windows-1252"
 * }
 */

package latitude.fixedWidthFileGen

import java.nio.charset.Charset
import java.nio.file.{Paths, StandardOpenOption}

import cats.effect.{Blocker, ContextShift, IO}
import fs2.{Stream, io, text}

import scala.util.Random


class Generator {

  protected def genStr(maxLength: Int): String = {
    val length = Random.nextInt(maxLength + 1)
    Random.alphanumeric.take(length).mkString
  }

  protected def pad(s: String, length: Int, padding: Char): String = {
    if (s.length < length)
      s + padding.toString * (length - s.length)
    else
      s
  }

  protected def genRow(colWidths: Vector[Int], padding: Char): IO[String] =
    IO(colWidths.map(cw => pad(genStr(cw), cw, padding)).mkString)

  protected def randomFixedWidthFileStream(colWidths: Vector[Int], padding: Char, numLines: Int): Stream[IO, Byte] = {
    Stream
      .eval(genRow(colWidths, padding))
      .repeatN(numLines)
      .intersperse("\n")
      .through(text.encode(Charset.forName("windows-1252")))
  }

  def apply(colWidths: Vector[Int], padding: Char, numLines: Int, outputPath: String)(implicit cs: ContextShift[IO]): IO[Unit] = {
    Stream.resource(Blocker[IO])
      .flatMap { blocker =>
        randomFixedWidthFileStream(colWidths, padding, numLines)
          .through(io.file.writeAll(Paths.get(outputPath), blocker, flags = Seq(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)))
      }
      .compile.drain
  }


}


object Generator extends Generator