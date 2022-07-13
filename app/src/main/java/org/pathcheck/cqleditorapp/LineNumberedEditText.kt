package org.pathcheck.cqleditorapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet

class LineNumberedEditText: androidx.appcompat.widget.AppCompatEditText {

  constructor(context: Context) : super(context) {
  }

  constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
  }

  constructor(context: Context, attrs: AttributeSet, styleAttr: Int) : super(context,attrs, styleAttr) {
  }

  private var lineNumberRect: Rect = Rect()
  private var lineNumberPaint: Paint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.FILL
  }

  /**
   * the difference between line text size and the normal text size.
   */
  protected var LINE_NUMBER_TEXTSIZE_GAP = 15
  protected var LINE_NUMBER_PADDING_LEFT = 30

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    var baseLine: Float;
    var number = "";

    lineNumberPaint.color = currentTextColor
    lineNumberPaint.textSize = getTextSize() - LINE_NUMBER_TEXTSIZE_GAP

    for (i in 0 until getLineCount()) {
      number = " " + (i + 1);
      baseLine = getLineBounds(i, null).toFloat()
      canvas.drawText(number, lineNumberRect.left.toFloat(), baseLine, lineNumberPaint)
    }

    val paddingLeft = LINE_NUMBER_PADDING_LEFT + lineNumberPaint.measureText(number)
    setPadding(paddingLeft.toInt(), getPaddingTop(), getPaddingRight(), getPaddingBottom())
  }
}