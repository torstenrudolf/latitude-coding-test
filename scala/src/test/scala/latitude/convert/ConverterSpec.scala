package latitude.convert

import cats.effect.IO
import utest._
import fs2.{Pure, Stream}
import utest.framework.ExecutionContext

import scala.io.Source


object ConverterSpec extends TestSuite {

  val tests = Tests {

    test("convert") {
      object Conv extends Converter {
        def test(): Unit = {
          // "," == 44, "\n" == 10, windows-1252 encoded "¢" == -94 converts to Array(-62, -94) in utf-8
          assert(
            Stream[Pure, Byte](-94, 100, 115, 97, -94, 120, 32, -94, 100, 115, 97, -94, 120, -94, 100, 115, 97, -94, 120, 32, 32)
              .through(convert(Vector("col1", "col2", "col3"), Vector(7, 6, 8), ' ', ","))
              .compile.toList == List[Byte](99, 111, 108, 49, 44, 99, 111, 108, 50, 44, 99, 111, 108, 51, 10, -62, -94, 100, 115, 97, -62, -94, 120, 44, -62, -94, 100, 115, 97, -62, -94, 120, 44, -62, -94, 100, 115, 97, -62, -94, 120)
          )
        }
      }
      Conv.test()
    }

    test("convertFile") {

      import ExecutionContext.RunNow
      val file = "test2.txt"
      val outFile = "test2.csv"

      import java.io._
      val pw = new PrintWriter(new File(file), "windows-1252")
      pw.write("¢d ¢d   ¢d\n¢d ¢d   ¢d")
      pw.close()

      Converter(file, outFile, Vector("col1", "col2", "col3"), Vector(3, 5, 2), ' ', ",")(IO.contextShift(RunNow)).unsafeRunSync()

      val fileBuf = Source.fromFile(outFile, enc = "utf-8")
      val content = try
        fileBuf.getLines.mkString("\n")
      finally {
        fileBuf.close()
        new File(file).delete()
        new File(outFile).delete()
      }

      println(content)

      assert(
        content ==
          """col1,col2,col3
             |¢d,¢d,¢d
             |¢d,¢d,¢d""".stripMargin
      )
    }

  }
}
