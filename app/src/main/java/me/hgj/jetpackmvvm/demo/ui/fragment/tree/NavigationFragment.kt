package me.hgj.jetpackmvvm.demo.ui.fragment.tree

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.blankj.utilcode.util.ConvertUtils
import com.kingja.loadsir.core.LoadService
import kotlinx.android.synthetic.main.include_list.*
import kotlinx.android.synthetic.main.include_recyclerview.*
import me.hgj.jetpackmvvm.demo.R
import me.hgj.jetpackmvvm.demo.app.base.BaseFragment
import me.hgj.jetpackmvvm.demo.app.ext.*
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.ErrorCallback
import me.hgj.jetpackmvvm.demo.app.weight.loadCallBack.LoadingCallback
import me.hgj.jetpackmvvm.demo.app.weight.recyclerview.SpaceItemDecoration
import me.hgj.jetpackmvvm.demo.data.model.bean.AriticleResponse
import me.hgj.jetpackmvvm.demo.databinding.IncludeListBinding
import me.hgj.jetpackmvvm.demo.ui.adapter.NavigationAdapter
import me.hgj.jetpackmvvm.demo.viewmodel.request.RequestTreeViewModel
import me.hgj.jetpackmvvm.demo.viewmodel.state.TreeViewModel
import me.hgj.jetpackmvvm.ext.getViewModel
import me.hgj.jetpackmvvm.ext.nav

/**
 * 作者　: hegaojian
 * 时间　: 2020/3/3
 * 描述　: 体系
 */
class NavigationFragment : BaseFragment<TreeViewModel, IncludeListBinding>() {

    //界面状态管理者
    private lateinit var loadsir: LoadService<Any>

    override fun layoutId() = R.layout.include_list

    private val navigationAdapter: NavigationAdapter by lazy { NavigationAdapter(arrayListOf()) }

    /** 注意，在by lazy中使用getViewModel一定要使用泛型，虽然他提示不报错，但是你不写是不行的 */
    private val requestTreeViewModel: RequestTreeViewModel by lazy { getViewModel<RequestTreeViewModel>() }

    override fun initView(savedInstanceState: Bundle?) {
        //状态页配置
        loadsir = LoadServiceInit(swipeRefresh) {
            //点击重试时触发的操作
            loadsir.showCallback(LoadingCallback::class.java)
            requestTreeViewModel.getNavigationData()
        }
        //初始化recyclerView
        recyclerView.init(LinearLayoutManager(context), navigationAdapter).let {
            it.addItemDecoration(SpaceItemDecoration(0, ConvertUtils.dp2px(8f)))
            it.initFloatBtn(floatbtn)
        }
        //初始化 SwipeRefreshLayout
        swipeRefresh.init {
            //触发刷新监听时请求数据
            requestTreeViewModel.getNavigationData()
        }
        navigationAdapter.setNavigationClickInterFace(object :
            NavigationAdapter.NavigationClickInterFace {
            override fun onNavigationClickListener(item: AriticleResponse, view: View) {
                nav().navigate(R.id.action_mainfragment_to_webFragment,
                    Bundle().apply {
                        putParcelable("ariticleData", item)
                    }
                )
            }
        })
    }

    override fun lazyLoadData() {
        requestTreeViewModel.getNavigationData()
    }

    override fun createObserver() {
        requestTreeViewModel.navigationDataState.observe(viewLifecycleOwner, Observer {
            swipeRefresh.isRefreshing = false
            if (it.isSuccess) {
                loadsir.showSuccess()
                navigationAdapter.setNewInstance(it.listData)
            } else {
                loadsir.setErrorText(it.errMessage)
                loadsir.showCallback(ErrorCallback::class.java)
            }
        })
        shareViewModel.run {
            //监听全局的主题颜色改变
            appColor.observe(viewLifecycleOwner, Observer {
                setUiTheme(it, floatbtn, swipeRefresh, loadsir)
            })
            //监听全局的列表动画改编
            appAnimation.observe(viewLifecycleOwner, Observer {
                navigationAdapter.setAdapterAnimion(it)
            })
        }

    }
}