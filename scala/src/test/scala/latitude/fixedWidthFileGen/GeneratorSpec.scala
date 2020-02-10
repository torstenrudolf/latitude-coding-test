package latitude.fixedWidthFileGen

import java.io.File

import cats.effect.IO
import utest._
import utest.framework.ExecutionContext

import scala.io.Source

object GeneratorSpec extends TestSuite {


  val tests = Tests {

    test("paddings") {
      object Gen extends Generator {
        def test() {
          assert(pad("asd", 11, padding = ' ') == "asd        ")
        }
      }
      Gen.test()
    }

    test("genRow") {
      val s = "asd"
      object Gen extends Generator {
        override def genStr(maxLength: Int): String = s

        def test(): Unit = {
          assert(genRow(Vector(3, 5, 4), '_').unsafeRunSync() == s"$s${s}__${s}_")
        }
      }
      Gen.test()
    }

    test("stream") {
      val s = "¢dsa¢x"
      object Gen extends Generator {
        override def genStr(maxLength: Int): String = s

        def test(): Unit = {
          val stream = randomFixedWidthFileStream(Vector(7, 6, 8), ' ', 3)
          val list = stream.compile.toList.unsafeRunSync()

          assert(list ==
            List[Byte](
              -94, 100, 115, 97, -94, 120, 32, -94, 100, 115, 97, -94, 120, -94, 100, 115, 97, -94, 120, 32, 32,
              10,
              -94, 100, 115, 97, -94, 120, 32, -94, 100, 115, 97, -94, 120, -94, 100, 115, 97, -94, 120, 32, 32,
              10,
              -94, 100, 115, 97, -94, 120, 32, -94, 100, 115, 97, -94, 120, -94, 100, 115, 97, -94, 120, 32, 32)
          )
        }
      }
      Gen.test()
    }

    test("generateFile") {
      val s = "¢d"
      object Gen extends Generator {
        override def genStr(maxLength: Int): String = s
      }

      import ExecutionContext.RunNow
      val file = "test.txt"
      Gen(Vector(3, 5, 2), ' ', 3, file)(IO.contextShift(RunNow)).unsafeRunSync()
      val fileBuf = Source.fromFile(file, enc = "windows-1252")
      val content =
        try fileBuf.getLines.mkString("\n")
        finally {
          fileBuf.close()
          new File(file).delete()
        }

      assert(
        content ==
          """|¢d ¢d   ¢d
             |¢d ¢d   ¢d
             |¢d ¢d   ¢d""".stripMargin
      )

    }

  }
}
