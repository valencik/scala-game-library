package sgl
package android

import sgl.util.LoggingProvider

import _root_.android.app.Activity
import _root_.android.os.Bundle

import _root_.android.view.View
import _root_.android.view.GestureDetector
import _root_.android.view.MotionEvent
import _root_.android.view.KeyEvent

//TODO: not clear why we need an InputProvider? Only thing
//      that we need is to register the event, which is not
//      something that needs to be visible at the type level
//      for the clients.
trait AndroidInputProvider extends Activity with InputProvider {
  this: AndroidWindowProvider with LoggingProvider =>

  def registerInputsListeners(): Unit = {
    val gestureDetector = new GestureDetector(gameView.getContext, new GameGestureListener)
    gestureDetector.setIsLongpressEnabled(false) //no long press, user can long press and then scroll

    gameView.setOnTouchListener(new View.OnTouchListener {
      override def onTouch(view: View, event: MotionEvent): Boolean = {
        gestureDetector.onTouchEvent(event)

        /*
         * Multi-touch on android starts with a first ACTION_DOWN event for the
         * very first pointer, then following down are ACTION_POINTER_DOWN, and 
         * first up (as long as one finger is still left) is ACTION_POINTER_UP.
         * Seems like we don't need that level of precision in the engine, so we
         * only have the notion of Up and Down, relying on the pointer id if we
         * need to track a multitouch gesture
         */

        val action = event.getActionMasked
        for(p <- 0 until event.getPointerCount()) {
          if(action == MotionEvent.ACTION_DOWN) {
            val x = event.getX(p).toInt
            val y = event.getY(p).toInt
            Input.newEvent(Input.TouchDownEvent(x, y, event.getPointerId(p)))
          } else if(action == MotionEvent.ACTION_POINTER_DOWN) {
            val x = event.getX(p).toInt
            val y = event.getY(p).toInt
            Input.newEvent(Input.TouchDownEvent(x, y, event.getPointerId(p)))
          } else if(action == MotionEvent.ACTION_MOVE) {

            //ACTION_MOVE is sometimes batched, meaning that we need to consume
            //historical data from the event, that shows intermediate position
            //before getting to the final getX/getY positions
            for(h <- 0 until event.getHistorySize) {
              val x = event.getHistoricalX(p, h).toInt
              val y = event.getHistoricalY(p, h).toInt
              Input.newEvent(Input.TouchMovedEvent(x, y, event.getPointerId(p)))
            }

            val x = event.getX(p).toInt
            val y = event.getY(p).toInt
            Input.newEvent(Input.TouchMovedEvent(x, y, event.getPointerId(p)))
          } else if(action == MotionEvent.ACTION_POINTER_UP) {
            val x = event.getX(p).toInt
            val y = event.getY(p).toInt
            Input.newEvent(Input.TouchUpEvent(x, y, event.getPointerId(p)))
          } else if(action == MotionEvent.ACTION_UP) {
            val x = event.getX(p).toInt
            val y = event.getY(p).toInt
            Input.newEvent(Input.TouchUpEvent(x, y, event.getPointerId(p)))
          }
        }

        /*
         * if true is not returned then we do not get follow up events 
         * (like UP after DOWN) and the gesture detector is not working properly (missing some events)
         */
        true
      }
    })

    //TODO: clarify what is proper way to detect those events
    gameView.setOnKeyListener(new View.OnKeyListener {
      override def onKey(v: View, keyCode: Int, event: KeyEvent): Boolean = {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
          if(event.getAction == KeyEvent.ACTION_UP) {
            Input.newEvent(Input.KeyUpEvent(Input.Keys.ButtonBack))
            true
          } else if(event.getAction == KeyEvent.ACTION_DOWN) {
            Input.newEvent(Input.KeyDownEvent(Input.Keys.ButtonBack))
            true
          } else false
        } else if(keyCode == KeyEvent.KEYCODE_MENU) {
          if(event.getAction == KeyEvent.ACTION_UP) {
            Input.newEvent(Input.KeyUpEvent(Input.Keys.ButtonMenu))
            true
          } else if(event.getAction == KeyEvent.ACTION_DOWN) {
            Input.newEvent(Input.KeyDownEvent(Input.Keys.ButtonMenu))
            true
          } else false
        } else {
          false
        }
      }
    })

  }

  //TODO: must reintegrate these scrolling detection somewhere in the framework
  class GameGestureListener extends GestureDetector.SimpleOnGestureListener {
    override def onScroll(ev1: MotionEvent, ev2: MotionEvent, distanceX: Float, distanceY: Float): Boolean = {
      //inputBuffer.touchScrollVector match {
      //  case None =>
      //    //inputBuffer.touchScrollVector = Some((distanceX, distanceY))
      //  case Some((dx, dy)) =>
      //    //inputBuffer.touchScrollVector = Some((dx + distanceX, dy + distanceY))
      //}
      true
    }
    override def onSingleTapUp(event: MotionEvent): Boolean = {
      val x = event.getX.toInt
      val y = event.getY.toInt
      //inputBuffer.touchPoint =  Some((x, y))
      true
    }
  }

}
