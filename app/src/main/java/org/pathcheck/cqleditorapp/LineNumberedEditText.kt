package org.pathcheck.cqleditorapp

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import kotlin.math.max

class LineNumberedEditText: androidx.appcompat.widget.AppCompatEditText {
  constructor(cxt: Context) : super(cxt) {}
  constructor(cxt: Context, attrs: AttributeSet) : super(cxt, attrs) {}
  constructor(cxt: Context, attrs: AttributeSet, styleAttr: Int) : super(cxt,attrs, styleAttr) {}

  private var lineNumberRect: Rect = Rect()
  private var lineNumberPaint: Paint = Paint().apply {
    isAntiAlias = true
    style = Paint.Style.FILL
  }

  /**
   * the difference between line text size and the normal text size.
   */
  protected var LINE_NUMBER_TEXTSIZE_GAP = 5
  protected var LINE_NUMBER_PADDING_LEFT = 30

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    var baseLine = 0.0f;
    var number = "";

    lineNumberPaint.color = currentTextColor
    lineNumberPaint.textSize = getTextSize() - LINE_NUMBER_TEXTSIZE_GAP

    for (i in 0 until getLineCount()) {
      number = " " + (i + 1);
      baseLine = getLineBounds(i, null).toFloat()
      canvas.drawText(number, lineNumberRect.left.toFloat(), baseLine, lineNumberPaint)
    }

    val lineX = LINE_NUMBER_PADDING_LEFT/2 + lineNumberPaint.measureText(number)
    canvas.drawLine(lineX, 0.0f, lineX, max(baseLine, canvas.height.toFloat()), lineNumberPaint);

    val paddingLeft = LINE_NUMBER_PADDING_LEFT + lineNumberPaint.measureText(number)
    setPadding(paddingLeft.toInt(), getPaddingTop(), getPaddingRight(), getPaddingBottom())
  }
}