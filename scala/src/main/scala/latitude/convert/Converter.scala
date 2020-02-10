package latitude.convert

import java.nio.charset.Charset
import java.nio.file.{Paths, StandardOpenOption}

import cats.effect.{Blocker, ContextShift, ExitCode, IO}
import cats.implicits._
import fs2.{Pipe, Stream, io, text}


/**
 * Convert a fixed-width data file in windows-1252 encoding to csv file in utf-8 encoding
 */
trait Converter {

  protected def windows1252Decode[F[_]]: Pipe[F, Byte, String] =
  // luckily windows-1252 is a single-byte encoding, so we don't need to do complicated stuff like in text.utf8Decode
    _.map(b => new String(Array(b), Charset.forName("windows-1252")))

  protected def removePadding(s: String, padding: Char): String =
    s.reverse.dropWhile(_ == padding).reverse

  protected def convertRow(s: String, colWidths: Vector[Int], padding: Char, sep: String): String =
    colWidths.foldLeft(("", s)) { case ((acc, rem), cw) =>
      (acc + sep + removePadding(rem.take(cw), padding), rem.drop(cw))
    }
      ._1.drop(1)

  protected def convert[F[_]](colNames: Vector[String], colWidths: Vector[Int], padding: Char, sep: String): Pipe[F, Byte, Byte] =
    inWindows1252Stream =>
    (
      Stream(colNames.mkString(sep)) ++
      inWindows1252Stream
        .through(windows1252Decode)
        .through(text.lines)
        .map(line => convertRow(line, colWidths, padding, sep))
      )
      .intersperse("\n")
      .through(text.utf8Encode)

  def apply(inPath: String, outPath: String, colNames: Vector[String], colWidths: Vector[Int], padding: Char, sep: String)(implicit cs: ContextShift[IO]): IO[Unit] =
    Stream.resource(Blocker[IO]).flatMap { blocker =>
      io.file.readAll[IO](Paths.get(inPath), blocker, 4096)
        .through(convert(colNames, colWidths, padding, sep))
        .through(io.file.writeAll(Paths.get(outPath), blocker, flags = Seq(StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)))
    }
      .compile.drain
}

object Converter extends Converter