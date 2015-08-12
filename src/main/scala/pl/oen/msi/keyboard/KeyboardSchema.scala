package pl.oen.msi.keyboard

import java.util.prefs.Preferences

case class KeyboardSchema(colour1: Byte, colour2: Byte, colour3: Byte)

object KeyboardSchema {
  val defaultColour = "6"

  lazy val preferences: Preferences = Preferences.systemNodeForPackage(classOf[KeyboardSchema])

  def apply(): KeyboardSchema = {
    apply(None, None, None)
  }

  def apply(colour1: Option[Byte], colour2: Option[Byte], colour3: Option[Byte]) = {
    val c1 = getOrReadSavedColour(colour1, "colour1")
    val c2 = getOrReadSavedColour(colour2, "colour2")
    val c3 = getOrReadSavedColour(colour3, "colour3")

    new KeyboardSchema(c1, c2, c3)
  }

  def getOrReadSavedColour(colour: Option[Byte], colourName: String) = {
    colour.foreach(c => savePrefColour(colourName, c))
    colour.getOrElse(getPrefColour(colourName))
  }

  protected def getPrefColour(colour: String) = {
    preferences.get(colour, defaultColour).toByte
  }

  protected def savePrefColour(colourName:  String, colour: Byte): Unit = {
    preferences.put(colourName, colour.toString)
  }
}