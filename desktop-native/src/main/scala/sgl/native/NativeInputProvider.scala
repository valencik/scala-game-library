package sgl
package native

import scalanative.native._

import SDL._
import SDLExtra._

trait NativeInputProvider extends InputProvider {
  this: NativeWindowProvider with NativeGraphicsProvider =>

  def registerInputListeners(): Unit = { }

  def collectAllEvents(): Unit = {
    val event = stackalloc[SDL_Event]
    while (SDL_PollEvent(event) != 0) {
      event.type_ match {
        case SDL_QUIT => //special handle quit event
          sys.exit(0)
        case SDL_KEYDOWN =>
          val keyEvent = keycodeToEvent(event.cast[Ptr[SDL_KeyboardEvent]].keycode)
          keyEvent.foreach(key => {
            Input.newEvent(Input.KeyDownEvent(key))
          })
        case SDL_KEYUP =>
          val keyEvent = keycodeToEvent(event.cast[Ptr[SDL_KeyboardEvent]].keycode)
          keyEvent.foreach(key => {
            Input.newEvent(Input.KeyUpEvent(key))
          })
        case _ =>
          ()
      }
    }
  }

  def keycodeToEvent(keycode: Keycode): Option[Input.Keys.Key] = keycode match {
    case UP_KEY => Some(Input.Keys.Up)
    case DOWN_KEY => Some(Input.Keys.Down)
    case LEFT_KEY => Some(Input.Keys.Left)
    case RIGHT_KEY => Some(Input.Keys.Right)
  }

}