//package com.example.indra
//
//
//
//import android.os.Bundle
//import android.util.Log
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.fillMaxSize
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.platform.LocalContext
//import androidx.compose.ui.viewinterop.AndroidView
//
//
//@Composable
//fun TestMapplsApiKey() {
//    val context = LocalContext.current
//    val mapView = MapView(context).apply { onCreate(Bundle()) }
//
//    AndroidView(
//        factory = { mapView },
//        modifier = Modifier.fillMaxSize(),
//        update = { view ->
//            view.getMapAsync(object : OnMapReadyCallback {
//                override fun onMapReady(mapplsMap: MapplsMap) {
//                    Log.d("MapplsTest", "Map loaded successfully! API key works.")
//                }
//
//                override fun onMapError(errorCode: Int, errorMessage: String) {
//                    Log.e("MapplsTest", "Map failed to load. Error $errorCode: $errorMessage")
//                }
//            })
//        }
//    )
//}
