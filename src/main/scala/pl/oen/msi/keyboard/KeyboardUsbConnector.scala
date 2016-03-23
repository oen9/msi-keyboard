package pl.oen.msi.keyboard

trait KeyboardUsbConnector {
  def setColours(colour1: Option[Byte], colour2: Option[Byte], colour3: Option[Byte])
}
