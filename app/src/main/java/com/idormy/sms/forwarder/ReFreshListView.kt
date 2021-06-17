package com.idormy.sms.forwarder

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.RotateAnimation
import android.widget.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 自定义listview
 */
class ReFreshListView : ListView, AbsListView.OnScrollListener {
    lateinit var header // 顶部布局文件；
            : View
    var headerHeight // 顶部布局文件的高度；
            = 0
    var firstVisibleItem // 当前第一个可见的item的位置；
            = 0
    var scrollState // listview 当前滚动状态；
            = 0
    var isRemark // 标记，当前是在listview最顶端摁下的；
            = false
    var startY // 摁下时的Y值；
            = 0
    var state // 当前的状态；
            = 0
    var iRefreshListener //刷新数据的接口
            : IRefreshListener? = null

    constructor(context: Context) : super(context) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initView(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        initView(context)
    }

    /**
     * 初始化界面，添加顶部布局文件到 listview
     *
     * @param context
     */
    private fun initView(context: Context) {
        val inflater = LayoutInflater.from(context)
        header = inflater.inflate(R.layout.header, null)
        measureView(header)
        headerHeight = header.measuredHeight
        Log.i("tag", "headerHeight = $headerHeight")
        topPadding(-headerHeight)
        this.addHeaderView(header)
        setOnScrollListener(this)
    }

    /**
     * 通知父布局，占用的宽，高；
     *
     * @param view
     */
    private fun measureView(view: View?) {
        var p = view!!.layoutParams
        if (p == null) {
            p = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        val width = getChildMeasureSpec(0, 0, p.width)
        val height: Int
        val tempHeight = p.height
        height = if (tempHeight > 0) {
            MeasureSpec.makeMeasureSpec(
                tempHeight,
                MeasureSpec.EXACTLY
            )
        } else {
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        }
        view.measure(width, height)
    }

    /**
     * 设置header 布局 上边距；
     *
     * @param topPadding
     */
    private fun topPadding(topPadding: Int) {
        header.setPadding(
            header.paddingLeft, topPadding,
            header.paddingRight, header.paddingBottom
        )
        header.invalidate()
    }

    override fun onScroll(
        view: AbsListView, firstVisibleItem: Int,
        visibleItemCount: Int, totalItemCount: Int
    ) {
        this.firstVisibleItem = firstVisibleItem
    }

    override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
        this.scrollState = scrollState
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> if (firstVisibleItem == 0) {
                isRemark = true
                startY = ev.y.toInt()
            }
            MotionEvent.ACTION_MOVE -> onMove(ev)
            MotionEvent.ACTION_UP -> if (state == RELEASE || state == PULL) {
                state = REFRESHING
                // 加载最新数据；
                refreshViewByState()
                iRefreshListener!!.onRefresh()
            }
        }
        return super.onTouchEvent(ev)
    }

    /**
     * 判断移动过程操作；
     *
     * @param ev
     */
    private fun onMove(ev: MotionEvent) {
        if (!isRemark) {
            return
        }
        val tempY = ev.y.toInt()
        val space = tempY - startY
        val topPadding = space - headerHeight
        when (state) {
            NONE -> if (space > 0) {
                state = PULL
                refreshViewByState()
            }
            PULL -> {
                topPadding(topPadding)
                if (space > headerHeight + 30
                    && scrollState == OnScrollListener.SCROLL_STATE_TOUCH_SCROLL
                ) {
                    state = RELEASE
                    refreshViewByState()
                }
            }
            RELEASE -> {
                topPadding(topPadding)
                if (space < headerHeight + 30) {
                    state = PULL
                    refreshViewByState()
                } else if (space <= 0) {
                    state = NONE
                    isRemark = false
                    refreshViewByState()
                }
            }
        }
    }

    /**
     * 根据当前状态，改变界面显示；
     */
    private fun refreshViewByState() {
        val tip = header.findViewById<View>(R.id.tip) as TextView
        val arrow = header.findViewById<View>(R.id.arrow) as ImageView
        val progress = header.findViewById<View>(R.id.progress) as ProgressBar
        val anim = RotateAnimation(
            0F, 180F,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        anim.duration = 500
        anim.fillAfter = true
        val anim1 = RotateAnimation(
            180F, 0F,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f,
            RotateAnimation.RELATIVE_TO_SELF, 0.5f
        )
        anim1.duration = 500
        anim1.fillAfter = true
        when (state) {
            NONE -> {
                arrow.clearAnimation()
                topPadding(-headerHeight)
            }
            PULL -> {
                arrow.visibility = VISIBLE
                progress.visibility = GONE
                tip.text = "下拉可以刷新！"
                arrow.clearAnimation()
                arrow.animation = anim1
            }
            RELEASE -> {
                arrow.visibility = VISIBLE
                progress.visibility = GONE
                tip.text = "松开可以刷新！"
                arrow.clearAnimation()
                arrow.animation = anim
            }
            REFRESHING -> {
                topPadding(50)
                arrow.visibility = GONE
                progress.visibility = VISIBLE
                tip.text = "正在刷新..."
                arrow.clearAnimation()
            }
        }
    }

    /**
     * 获取完数据；
     */
    fun refreshComplete() {
        state = NONE
        isRemark = false
        refreshViewByState()
        val lastUpdateTime = header.findViewById<View>(R.id.lastupdate_time) as TextView
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val time = sdf.format(Date())
        lastUpdateTime.text = time
    }

    fun setInterface(iRefreshListener: IRefreshListener?) {
        this.iRefreshListener = iRefreshListener
    }

    /**
     * 刷新数据接口
     *
     * @author Administrator
     */
    interface IRefreshListener {
        fun onRefresh()
    }

    companion object {
        const val NONE = 0 // 正常状态；
        const val PULL = 1 // 提示下拉状态；
        const val RELEASE = 2 // 提示释放状态；
        const val REFRESHING = 3 // 刷新状态；
    }
}