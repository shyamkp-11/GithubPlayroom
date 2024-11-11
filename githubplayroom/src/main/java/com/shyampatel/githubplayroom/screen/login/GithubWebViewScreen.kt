package com.shyampatel.githubplayroom.screen.login

import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.shyampatel.githubplayroom.BuildConfig
import org.koin.androidx.compose.koinViewModel

@Composable
internal fun GithubWebViewRoute(
    modifier: Modifier = Modifier,
    onFinish: (success: Boolean) -> Unit,
    loginViewModel: GithubLoginViewModel = koinViewModel(), ){
    GithubWebViewScreen(modifier, loginViewModel::generateToken, onFinish )
}
@Composable
fun GithubWebViewScreen(modifier: Modifier, saveCode: (code: String, onSaveComplete:()->Unit)-> Unit, onFinish: (success: Boolean) -> Unit) {

    val clientId = BuildConfig.CLIENT_ID
    val onlyAuthenticateUrl = "https://github.com/login/oauth/authorize?client_id=${clientId}&scope=repo"
//    val onlyAuthenticateUrl = "https://github.com/login/oauth/authorize?client_id=${BuildConfig.CLIENT_ID}"
//    val installAndAuthenticateUrl = "https://github.com/apps/${BuildConfig.APP_NAME}/installations/new"
    Scaffold { innerPadding ->

        AndroidView(
            modifier = Modifier.padding(top = innerPadding.calculateTopPadding()),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    webViewClient = CustomWebViewClient(saveCode, onFinish, onlyAuthenticateUrl )

                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.setSupportZoom(true)
                }
            },
            update = { webView ->
//                webView.loadUrl(installAndAuthenticateUrl)
                webView.loadUrl(onlyAuthenticateUrl)
            },
            onRelease = {}
        )
    }
}

class CustomWebViewClient(val saveCode: (code: String, onSaveComplete:()->Unit)-> Unit, val onFinish: (success: Boolean) -> Unit, private val onlyAuthenticateUrl: String): WebViewClient(){
    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
        if(request?.url != null &&
            (request.url.toString().startsWith("https://github.com"))){
            Log.e("WebViewScreen", request.url.toString())
            if(request.url.toString().startsWith("https://github.com/settings/installations/")) {
                Log.e("WebViewScreen", "Loading $onlyAuthenticateUrl")
                view?.loadUrl(onlyAuthenticateUrl)
                return true
            }
            if (request.url.toString().contains("github.com/?code=") && !request.url.getQueryParameter("code").isNullOrEmpty()) {
                saveCode(
                    request.url.getQueryParameter("code").toString()
                ) {
                    onFinish(true)
                }
            }
        }
        return false
    }
}