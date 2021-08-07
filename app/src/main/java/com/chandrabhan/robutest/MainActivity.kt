package com.chandrabhan.robutest

import android.annotation.SuppressLint
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import butterknife.BindView
import butterknife.ButterKnife
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.baianat.floadingcellanimationsample.utils.GridSpacingItemDecoration
import com.baianat.floadingcellanimationsample.utils.ViewUtils
import com.chandrabhan.robutest.adapter.ProductAdapter
import com.chandrabhan.robutest.models.ResponseData
import com.chandrabhan.robutest.until.ConnectivityReceiver
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

@Suppress("DEPRECATION")
@SuppressLint("Registered")
class MainActivity : AppCompatActivity(),  ConnectivityReceiver.ConnectivityReceiverListener{

    @BindView(R.id.progressBar)
    lateinit var progressBar: ProgressBar

    val connection  = ConnectivityReceiver()

    @BindView(R.id.scrollView)
    lateinit var scrollView: NestedScrollView

    var searchView: SearchView?= null
    @BindView(R.id.productItems)
    lateinit var productItems: RecyclerView

    @BindView(R.id.btnSearch)
    lateinit var btnSearch: Button
    
    @BindView(R.id.txtText)
    lateinit var txtSearch: EditText

    var productList: MutableList<ResponseData> = ArrayList()
    lateinit var productAdapter: ProductAdapter
    var page =1
    val limit = 10

    lateinit var requestQueue : RequestQueue
    @SuppressLint("NewApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ButterKnife.bind(this)

        requestQueue = Volley.newRequestQueue(this)

        productItems.setHasFixedSize(true)
        productItems.layoutManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)
        productItems.addItemDecoration(GridSpacingItemDecoration(2, ViewUtils.dpToPx(12F), true, 0))
        productAdapter = ProductAdapter(this, productList)
        productItems.adapter = productAdapter


        val idName = txtSearch.text.toString().trim()

        scrollView.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v, scrollX, scrollY, oldScrollX, oldScrollY ->
            if (scrollY == v!!.getChildAt(0).measuredHeight - v.measuredHeight) {
                page++
                progressBar.visibility = View.VISIBLE

                if (idName.isNotEmpty()) {
                    productIdNameWise(idName, page, limit)
                } else {
                    getProductList(page, limit)
                }
            }
        })

        btnSearch.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            productList.clear()
            productAdapter.notifyDataSetChanged()
            val idName = txtSearch.text.toString()
            if(idName.isNotEmpty()){
                if (isNumber(idName)) {
                    productIdWise(idName)
                }else{
                    productIdNameWise(idName, page, limit)
                }
            }else{
                getProductList(page, limit)
            }
        }
    }

    private fun isNumber(s: String): Boolean {
        if (s == null || s.length == 0) {
            return false
        }
        for (c in s) {
            if (c < '0' || c > '9') {
                return false
            }
        }
        return true
    }

    private fun productIdNameWise(idName: String, page: Int, limit: Int) {
        productList.clear()
        val url = "https://test.robu.in/wp-json/wc/v3/products?search=$idName"
        val stringRequest = object : StringRequest(Method.GET, url, Response.Listener { response ->
            try {
                if (response != null) {
                    var src: String = ""
                    progressBar.visibility = View.GONE
                    Log.d("Response", response)
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getString("id")
                        val name = jsonObject.getString("name")
                        val price = jsonObject.getString("price")
                        val images: JSONArray = jsonObject.getJSONArray("images")
                        if (images.length() > 0) {
                            src = images.getJSONObject(0).getString("src")
                        }
                        productList.add(ResponseData(id, name, price, src))
                        productAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this, "Items not found", Toast.LENGTH_LONG).show()
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }, Response.ErrorListener { error ->
            progressBar.visibility = View.GONE

        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                val credentials = "ck_b40d0f2485cf99306e197efd910b004d200d7d1b:cs_c30f50b2f4df0bb63f54d34b628b21591de90c3f"
                val auth = "Basic " + Base64.encodeToString(
                    credentials.toByteArray(),
                    Base64.NO_WRAP
                )
                headers["Authorization"] = auth
                return headers
            }
        }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            900,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue?.add(stringRequest)
    }
    private fun productIdWise(idName: String) {
        productList.clear()
        val url = "https://test.robu.in/wp-json/wc/v3/products/$idName"
        val stringRequest = object : StringRequest(Method.GET, url, Response.Listener { response ->
            try {
                if (response != null) {
                    progressBar.visibility = View.GONE
                    var src: String = ""
                    Log.d("Response", response)
                    val jsonObject = JSONObject(response)
                    val id = jsonObject.getString("id")
                    val name = jsonObject.getString("name")
                    val price = jsonObject.getString("price")
                    val images: JSONArray = jsonObject.getJSONArray("images")
                    if (images.length() > 0) {
                        src = images.getJSONObject(0).getString("src")
                    }
                    productList.add(ResponseData(id, name, price, src))
                    productAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Items not found", Toast.LENGTH_LONG).show()

                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }, Response.ErrorListener { _ ->
            progressBar.visibility = View.GONE

        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                val credentials = "ck_b40d0f2485cf99306e197efd910b004d200d7d1b:cs_c30f50b2f4df0bb63f54d34b628b21591de90c3f"
                val auth = "Basic " + Base64.encodeToString(
                    credentials.toByteArray(),
                    Base64.NO_WRAP
                )
                headers["Authorization"] = auth
                return headers
            }
        }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            900,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue?.add(stringRequest)
    }
    private fun getProductList(page: Int, limit: Int) {

        val url = "https://test.robu.in/wp-json/wc/v3/products?page=$page&per_page=$limit"
        val stringRequest = object : StringRequest(Method.GET, url, Response.Listener { response ->
            try {
                if (response != null) {
                    var src: String = ""
                    progressBar.visibility = View.GONE
                    Log.d("Response", response)
                    val jsonArray = JSONArray(response)
                    for (i in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(i)
                        val id = jsonObject.getString("id")
                        val name = jsonObject.getString("name")
                        val price = jsonObject.getString("price")
                        val images: JSONArray = jsonObject.getJSONArray("images")
                        if (images.length() > 0) {
                            src = images.getJSONObject(0).getString("src")
                        }
                        productList.add(ResponseData(id, name, price, src))
                        productAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(this, "Items not found", Toast.LENGTH_LONG).show()
                }

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }, Response.ErrorListener { error ->
            progressBar.visibility = View.GONE

        }) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json"
                val credentials = "ck_b40d0f2485cf99306e197efd910b004d200d7d1b:cs_c30f50b2f4df0bb63f54d34b628b21591de90c3f"
                val auth = "Basic " + Base64.encodeToString(
                    credentials.toByteArray(),
                    Base64.NO_WRAP
                )
                headers["Authorization"] = auth
                return headers
            }
        }
        stringRequest.retryPolicy = DefaultRetryPolicy(
            9000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        requestQueue?.add(stringRequest)

    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        if(isConnected){
            progressBar.visibility = View.VISIBLE
            getProductList(page, limit)
        }else{
            progressBar.visibility = View.GONE
            Toast.makeText(
                this@MainActivity,
                "Please check your internet connection",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onStart() {
        super.onStart()
        registerReceiver(connection, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        ConnectivityReceiver.connectivityReceiverListener = this
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(connection)
    }
}