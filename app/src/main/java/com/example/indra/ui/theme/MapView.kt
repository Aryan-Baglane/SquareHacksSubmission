package com.example.indra.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.indra.R
import com.example.indra.data.Property
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import androidx.core.graphics.scale


@SuppressLint("Lifecycle")
@Composable
fun MapView(
    properties: List<Property>,
    onPropertyClick: (Property) -> Unit,
    modifier: Modifier = Modifier,
    defaultZoom: Double = 500.0,
    accessToken: String
) {
    var mapViewRef: MapView? = null

    // Clean up MapView properly
    DisposableEffect(Unit) {
        onDispose {
            mapViewRef?.onStop()
            mapViewRef?.onDestroy()
        }
    }

    androidx.compose.ui.viewinterop.AndroidView(
        modifier = modifier.height(250.dp), // compact height for map
        factory = { context ->

            val mapInitOptions = MapInitOptions(
                context = context,
                resourceOptions = ResourceOptions.Builder()
                    .accessToken(accessToken)
                    .build()
            )

            MapView(context, mapInitOptions).also { mapView ->
                mapViewRef = mapView

                mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) { style ->

                    // Load and scale pin drawable
                    val originalBitmap = android.graphics.BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.my_pin
                    )
                    val scaledBitmap = originalBitmap.scale(60, 80, false)
                    style.addImage("pin", scaledBitmap)

                    val annotationApi = mapView.annotations
                    val pointAnnotationManager: PointAnnotationManager =
                        annotationApi.createPointAnnotationManager()

                    pointAnnotationManager.deleteAll()

                    if (properties.isNotEmpty()) {
                        val points = properties.map { Point.fromLngLat(it.longitude, it.latitude) }

                        properties.zip(points).forEach { (property, point) ->
                            val annotationOptions = PointAnnotationOptions()
                                .withPoint(point)
                                .withIconImage("pin")
                            pointAnnotationManager.create(annotationOptions)
                        }

                        // Marker click
                        pointAnnotationManager.addClickListener { clickedAnnotation ->
                            val clickedProperty = properties.find { property ->
                                val propertyPoint = Point.fromLngLat(property.longitude, property.latitude)
                                clickedAnnotation.point == propertyPoint
                            }
                            clickedProperty?.let {
                                mapView.getMapboxMap().flyTo(
                                    cameraOptions {
                                        center(Point.fromLngLat(it.longitude, it.latitude))
                                        zoom(defaultZoom)
                                    },
                                    com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions {
                                        duration(1500) // smooth camera animation
                                    }
                                )
                                onPropertyClick(it)
                                true
                            } ?: false
                        }

                        // Fit all markers in view with padding
                        val camera = mapView.getMapboxMap().cameraForCoordinates(
                            coordinates = points,
                            padding = com.mapbox.maps.EdgeInsets(80.0, 80.0, 80.0, 80.0)
                        )
                        mapView.getMapboxMap().flyTo(
                            camera,
                            com.mapbox.maps.plugin.animation.MapAnimationOptions.mapAnimationOptions {
                                duration(1500)
                            }
                        )
                    } else {
                        mapView.getMapboxMap().setCamera(
                            CameraOptions.Builder()
                                .center(Point.fromLngLat(77.2090, 28.6139))
                                .zoom(defaultZoom)
                                .build()
                        )
                    }
                }
            }
        }
    )
}
