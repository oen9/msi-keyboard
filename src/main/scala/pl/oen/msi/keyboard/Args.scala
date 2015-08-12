package pl.oen.msi.keyboard

import org.kohsuke.args4j
import org.kohsuke.args4j.spi.{OneArgumentOptionHandler, Setter}
import org.kohsuke.args4j.{CmdLineParser, OptionDef}

object Args {
  final val HIDE_PARAM_NAME = "-hide"
}

class Args {
  @args4j.Option(name = "-c1", aliases = Array("-colour1", "--colour1", "--c1"), usage = "Sets a colour1", handler = classOf[ScalaOptionHandler])
  var colour1: Option[Byte] = None

  @args4j.Option(name = "-c2", aliases = Array("-colour2", "--colour2", "--c2"), usage = "Sets a colour2", handler = classOf[ScalaOptionHandler])
  var colour2: Option[Byte] = None

  @args4j.Option(name = "-c3", aliases = Array("-colour3", "--colour3", "--c3"), usage = "Sets a colour3", handler = classOf[ScalaOptionHandler])
  var colour3: Option[Byte] = None

  @args4j.Option(name = "-x", aliases = Array("-X", "--x", "--X"), usage = "turn OFF graphics mode")
  var x: Boolean = false

  @args4j.Option(name = Args.HIDE_PARAM_NAME, usage = "Hides app to tray on startup")
  var hide: Boolean = false
}

class ScalaOptionHandler(parser: CmdLineParser, option: OptionDef, setter: Setter[_ >: Option[Byte]]) extends OneArgumentOptionHandler[Option[Byte]](parser, option, setter) {
  override def parse(argument: String): Option[Byte] = {
    if (argument.isEmpty) None else Some(argument.toByte)
  }
}

