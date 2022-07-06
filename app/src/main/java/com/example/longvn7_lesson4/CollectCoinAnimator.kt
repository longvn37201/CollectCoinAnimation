package com.example.longvn7_lesson4

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.Log
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.CycleInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class CollectCoinAnimator(
    private val mContext: Context,
    private val viewAttach: View,
    private var currentBalance: Int,
    private val increment: Int,
    private val coinNumber: Int = DEFAULT_COIN_NUMBER,
) : View(mContext) {

    companion object {
        //layout balance
        private const val LAYOUT_BALANCE_ANIM_DURATION = 300L
        private const val LAYOUT_BALANCE_HIDE_DELAY_DURATION = 500L
        private const val ICON_BALANCE_MARGIN = 30f
        private const val ICON_BALANCE_SIZE = 80f
        private const val ICON_BALANCE_LEFT = 30f
        private const val ICON_BALANCE_RIGHT = 110f
        private const val TEXT_BALANCE_LEFT = ICON_BALANCE_RIGHT + 30f
        private const val TEXT_LABEL_LEFT = ICON_BALANCE_RIGHT + 30f
        private const val BALANCE_INCREMENT_DURATION = 700L
        private const val TEXT_SIZE = 33f
        private const val BACKGROUND_BALANCE_HEIGHT = 140f
        private val BACKGROUND_BALANCE_SLIDE_RANGE = -140..0
        private val BACKGROUND_BALANCE_COLOR = Color.parseColor("#ff669900")

        //coin
        private val COIN_INIT_RANGE = -70..70
        private val COIN_ALPHA_RANGE = 0..255
        private const val COIN_DELAY_APPEAR = 50L
        private const val DEFAULT_COIN_NUMBER = 9
        private const val ICON_COIN_SIZE = 80f
        private const val COIN_SHAKE_DURATION = 500L
        private const val COIN_FLY_DURATION = 1200L
        private val COIN_SHAKE_RANGE_Y = 0..10
        private const val COIN_SHAKE_CYCLES = 1f


    }

    private val iconBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.ic_coin)

    //balance
    private lateinit var backgroundBalance: Paint
    private lateinit var text: Paint
    private var backgroundBalanceTop = 0f
    private var backgroundBalanceBottom = 0f
    private var iconBalanceTop = 0f
    private var iconBalanceBottom = 0f
    private var textLabelTop = 0f
    private var textBalanceTop = 0f

    //coin
    private val listCoin = mutableListOf<Coin>()


    init {
        initBalanceView()
        initCoinFly()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        drawLayoutBalance(canvas)
        drawCoinIcon(canvas)
    }

    fun startAnimation() {
        showHideBalanceView(isShow = true)
        coinAnimation()
    }

    private fun drawCoinIcon(canvas: Canvas?) {
        listCoin.forEach { coin ->
            coin.paint.alpha = coin.alpha
            canvas?.drawBitmap(
                iconBitmap,
                null,
                RectF(
                    coin.position.x.toFloat(),
                    coin.position.y.toFloat(),
                    coin.position.x.toFloat() + ICON_COIN_SIZE,
                    coin.position.y.toFloat() + ICON_COIN_SIZE
                ),
                coin.paint
            )
        }
    }


    private fun drawLayoutBalance(canvas: Canvas?) {
        canvas?.apply {
            //background
            drawRect(
                left.toFloat(),
                backgroundBalanceTop,
                right.toFloat(),
                backgroundBalanceBottom,
                backgroundBalance
            )
            //icon
            drawBitmap(
                iconBitmap,
                null,
                RectF(
                    ICON_BALANCE_LEFT,
                    iconBalanceTop,
                    ICON_BALANCE_RIGHT,
                    iconBalanceBottom
                ),
                null
            )
            //text label
            drawText(
                "$currentBalance",
                TEXT_LABEL_LEFT,
                textLabelTop,
                text
            )
            //text balance
            drawText(
                "Số dư coin",
                TEXT_BALANCE_LEFT,
                textBalanceTop,
                text
            )
        }
    }

    private fun initBalanceView() {
        backgroundBalanceTop = BACKGROUND_BALANCE_SLIDE_RANGE.first.toFloat()
        iconBalanceTop = backgroundBalanceTop + ICON_BALANCE_MARGIN

        backgroundBalance = Paint().apply {
            color = BACKGROUND_BALANCE_COLOR
        }
        text = Paint().apply {
            color = Color.WHITE
            textSize = TEXT_SIZE
        }
    }

    private fun initCoinFly() {
        val viewAttachX = (viewAttach.right + viewAttach.left) / 2
        val viewAttachY = (viewAttach.top + viewAttach.bottom) / 2
        for (i in 1..coinNumber) {
            listCoin.add(
                Coin(
                    Point(
                        viewAttachX - (ICON_COIN_SIZE / 2).toInt() + COIN_INIT_RANGE.random(),
                        viewAttachY - (ICON_COIN_SIZE / 2).toInt() + COIN_INIT_RANGE.random()
                    ),
                    0,
                    Paint()
                )
            )
        }
    }


    private fun showHideBalanceView(isShow: Boolean) {
        if (isShow) {
            ValueAnimator.ofInt(
                BACKGROUND_BALANCE_SLIDE_RANGE.first,
                BACKGROUND_BALANCE_SLIDE_RANGE.last
            ).apply {
                duration = LAYOUT_BALANCE_ANIM_DURATION
                interpolator = LinearInterpolator()
                start()
            }.addUpdateListener {
                backgroundBalanceTop = (it.animatedValue as Int).toFloat()
                backgroundBalanceBottom = backgroundBalanceTop + BACKGROUND_BALANCE_HEIGHT
                iconBalanceTop = backgroundBalanceTop + ICON_BALANCE_MARGIN
                iconBalanceBottom = iconBalanceTop + ICON_BALANCE_SIZE
                textLabelTop = iconBalanceTop + TEXT_SIZE
                textBalanceTop = iconBalanceBottom - 5f
                invalidate()
            }
        } else {
            CoroutineScope(Main).launch {
                delay(LAYOUT_BALANCE_HIDE_DELAY_DURATION)
                ValueAnimator.ofInt(
                    BACKGROUND_BALANCE_SLIDE_RANGE.last,
                    BACKGROUND_BALANCE_SLIDE_RANGE.first
                ).apply {
                    duration = LAYOUT_BALANCE_ANIM_DURATION
                    interpolator = LinearInterpolator()
                    start()
                }.addUpdateListener {
                    backgroundBalanceTop = (it.animatedValue as Int).toFloat()
                    backgroundBalanceBottom = backgroundBalanceTop + BACKGROUND_BALANCE_HEIGHT
                    iconBalanceTop = backgroundBalanceTop + ICON_BALANCE_MARGIN
                    iconBalanceBottom = iconBalanceTop + ICON_BALANCE_SIZE
                    textLabelTop = iconBalanceTop + TEXT_SIZE
                    textBalanceTop = iconBalanceBottom - 5f
                    invalidate()
                }
            }
        }
    }

    private fun coinAnimation() {
        var countCoinFlyEnd = 0
        CoroutineScope(Main).launch {
            listCoin.forEach { coin ->
                showCoinAnimation(
                    coin,
                    onCoinMaxAlpha = {
                        shakeCoinAnimation(
                            coin,
                            onCoinShakeEnd = {
                                flyCoinAnimation(
                                    coin,
                                    onCoinFlyEnd = {
                                        countCoinFlyEnd++
                                        if (countCoinFlyEnd == 1) {
                                            textIncreaseAnimation()
                                        }
                                    },
                                )
                            },
                        )
                    }
                )
                delay(COIN_DELAY_APPEAR)
            }
        }
    }

    private fun textIncreaseAnimation() {
        ValueAnimator.ofInt(
            currentBalance,
            currentBalance + increment
        ).apply {
            duration = BALANCE_INCREMENT_DURATION
            addUpdateListener { animation ->
                currentBalance = (animation.animatedValue) as Int
                invalidate()
            }
            doOnEnd {
                showHideBalanceView(isShow = false)
            }
            start()
        }
    }

    private fun showCoinAnimation(coin: Coin, onCoinMaxAlpha: () -> Unit) {
        coin.alpha = COIN_ALPHA_RANGE.last
        invalidate()
        onCoinMaxAlpha()
    }

    private fun shakeCoinAnimation(coin: Coin, onCoinShakeEnd: () -> Unit) {
        ValueAnimator.ofInt(coin.position.y, coin.position.y + COIN_SHAKE_RANGE_Y.last).apply {
            duration = COIN_SHAKE_DURATION
            interpolator = CycleInterpolator(COIN_SHAKE_CYCLES)
            addUpdateListener {
                val value = (it.animatedValue as Int)
                coin.position.y = value
                invalidate()
            }
            doOnEnd {
                onCoinShakeEnd()
            }
            start()
        }
    }

    private fun flyCoinAnimation(coin: Coin, onCoinFlyEnd: () -> Unit) {
        //x
        val maxX = coin.position.x
        ValueAnimator.ofInt(coin.position.x, ICON_BALANCE_LEFT.toInt()).apply {
            duration = COIN_FLY_DURATION
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Int
                coin.position.x = value
                val percent =
                    (value - ICON_BALANCE_LEFT.toInt()).toFloat() / (maxX - ICON_BALANCE_LEFT.toInt())
                coin.alpha = (percent * COIN_ALPHA_RANGE.last).toInt()
                invalidate()
            }
            doOnEnd {
                onCoinFlyEnd()
            }
            start()
        }
        //y
        ValueAnimator.ofInt(coin.position.y, iconBalanceTop.toInt()).apply {
            duration = COIN_FLY_DURATION
            interpolator = AccelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Int
                coin.position.y = value
                invalidate()
            }
            doOnEnd {
            }
            start()
        }
    }

    inner class Coin(
        var position: Point,
        var alpha: Int = 0,
        var paint: Paint
    )

}