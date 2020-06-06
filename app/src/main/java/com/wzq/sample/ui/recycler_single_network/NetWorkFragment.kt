package com.wzq.sample.ui.recycler_single_networkimport android.annotation.SuppressLintimport android.content.Contextimport android.content.pm.ActivityInfoimport android.os.Bundleimport android.view.LayoutInflaterimport android.view.ViewGroupimport androidx.appcompat.app.AlertDialogimport androidx.lifecycle.Observerimport androidx.recyclerview.widget.LinearLayoutManagerimport com.chad.library.adapter.base.BaseQuickAdapterimport com.scwang.smartrefresh.layout.api.RefreshLayoutimport com.wzq.mvvmsmart.event.StateLiveData.StateEnumimport com.wzq.mvvmsmart.utils.KLogimport com.wzq.mvvmsmart.utils.ToastUtilsimport com.wzq.sample.BRimport com.wzq.sample.Rimport com.wzq.sample.base.BaseFragmentimport com.wzq.sample.bean.DemoBean.ItemsEntityimport com.wzq.sample.bean.NewsDataimport com.wzq.sample.databinding.FragmentNetworkBindingimport com.wzq.sample.utils.TestUtilsimport java.util.*/** * 王志强 2019/12/20 * RecyclerView + 请求网络数据 +分页 + StateLiveData控制加载状态（开始加载，加载失败，加载成功，数据解析错误） */class NetWorkFragment : BaseFragment<FragmentNetworkBinding, NetWorkViewModel>() {    private lateinit var mAdapter: MAdapter    private var newsDataList: List<NewsData> = ArrayList()    @SuppressLint("SourceLockedOrientationActivity")    override fun initParam() {        super.initParam()        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT    }    override fun initContentView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): Int {        return R.layout.fragment_network    }    override fun initVariableId(): Int {        return BR.viewModel    }    override fun initData() {        viewModel.requestNetWork() //请求网络数据        initRecyclerView()    }    private fun initRecyclerView() {        mAdapter = MAdapter(R.layout.item_single, newsDataList)        binding.layoutManager = LinearLayoutManager(activity)        binding.adapter = mAdapter        mAdapter.setOnItemClickListener { adapter, view, position -> ToastUtils.showShort("点击了条目") }        mAdapter.onItemLongClickListener = BaseQuickAdapter.OnItemLongClickListener { adapter, view, position ->            ToastUtils.showShort("长按了条目")            false        }        mAdapter.setOnItemChildClickListener { adapter, view, position ->            if (view.id == R.id.btn2) {                val newsData = newsDataList[position]                //删除选择对话框                val builder = AlertDialog.Builder(activity as Context)                builder.setTitle("尊敬的用户")                builder.setMessage("你真的要卸载我吗？")                builder.setPositiveButton("残忍卸载") { dialog, which ->                    viewModel.deleteItem(newsData)                    //                            mAdapter.remove(position);                    mAdapter.notifyItemRemoved(position)                    builder.setNegativeButton("我再想想") { dialog, which ->                    }                    val alert = builder.create()                    alert.show()                }            }        }    }    override fun initViewObservable() {        super.initViewObservable()        viewModel.liveData.observe(this, Observer { newsDataList: List<NewsData?> ->            KLog.e("mLiveData的listBeans.size():" + newsDataList.size)            //            setBeautifulGirlImg(itemsEntities);  // 图片链接经常失效,设置美女图片,但每次上下拉头像会变;            if (viewModel.pageNum == 1) {                mAdapter.data.clear() // 请求多页数据后再请求第1页,先删除之前数据                if (newsDataList.isEmpty()) {                    //  第一页无数据,就显示默认页                    showEmptyLayout(binding.refreshLayout, this@NetWorkFragment.resources.getString(R.string.tip_a_page_no_data), R.mipmap.ic_launcher_mvvmsmart, false)                } else {                    showNormalLayout(binding.refreshLayout)                    mAdapter.addData(newsDataList)                }            } else { // 不是第一页                if (newsDataList.isEmpty()) {                    ToastUtils.showLong("开发者--本页无数据")                    binding.refreshLayout.finishLoadMoreWithNoMoreData()                    binding.refreshLayout.setNoMoreData(true)                } else {                    mAdapter.addData(newsDataList)                }            }        })        binding.refreshLayout.setOnRefreshListener {            viewModel.pageNum = 1            viewModel.requestNetWork()        }        //上拉加载更多        binding.refreshLayout.setOnLoadMoreListener { refreshLayout: RefreshLayout? ->            viewModel.pageNum++            //            loadMoreTestData();   // 模拟加载更多数据            viewModel.requestNetWork()        }        /**         * 每个界面默认页效果不同         * 在这里可以动态替换 无网络页,数据错误页, 无数据默认页;         */        viewModel.stateLiveData.stateEnumMutableLiveData                .observe(this, Observer { stateEnum: StateEnum ->                    if (stateEnum == StateEnum.Loading) {                        binding.refreshLayout.finishRefresh()                        binding.refreshLayout.finishLoadMore()                        KLog.e("请求数据中--显示loading")                    }                    if (stateEnum == StateEnum.Success) {                        binding.refreshLayout.finishRefresh()                        binding.refreshLayout.finishLoadMore()                        KLog.e("数据获取成功--关闭loading")                    }                    if (stateEnum == StateEnum.Idle) {                        KLog.e("空闲状态--关闭loading")                        binding.refreshLayout.finishRefresh()                        binding.refreshLayout.finishLoadMore()                    }                    if (stateEnum == StateEnum.NoData) {                        KLog.e("空闲状态--关闭loading")                        binding.refreshLayout.finishRefresh()                        binding.refreshLayout.finishLoadMore()                    }                })    }    private fun setBeautifulGirlImg(itemsEntities: List<ItemsEntity>) {        for (itemsEntity in itemsEntities) {            itemsEntity.img = TestUtils.getGirlImgUrl()        }    }    // 无数据默认页,点击请求网络    override fun onContentReload() {        super.onContentReload()        KLog.e("点击空白页")        viewModel.requestNetWork() //请求网络数据    }}