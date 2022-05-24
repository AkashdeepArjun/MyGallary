package com.example.mygallary

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.drawable.shapes.OvalShape
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import androidx.compose.ui.graphics.Path

class CircularImageView @JvmOverloads constructor(context: Context, attrs:AttributeSet?=null, defStyleAttr:Int=0):ImageView(context, attrs,defStyleAttr) {

    private var radius=10.0f

    private var center_position:PointF = PointF(0f,0f)

    val paint =Paint(Paint.ANTI_ALIAS_FLAG)

    private var border_width=1.5f

    val path= Path()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        this.radius= maxOf(w,oldw).toFloat()
        this.center_position= PointF((this.width/2).toFloat(), (this.height/2).toFloat())
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        paint.color= Color.CYAN
        paint.style=Paint.Style.STROKE
        paint.strokeWidth=border_width


    }



}