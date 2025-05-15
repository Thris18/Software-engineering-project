package no.uio.ifi.in2000.adriansv.adriansv.team45fiskeriapp.ui.alerts

import org.maplibre.android.maps.Style
import org.maplibre.android.style.expressions.Expression
import org.maplibre.android.style.layers.FillLayer
import org.maplibre.android.style.layers.PropertyFactory

object AlertUtils {
    fun updateAlertPolygon(style: Style, alertId: String?) {
        val layer = style.getLayer("alert-polygon-layer") as? FillLayer ?: return

        if (alertId != null) {
            layer.setFilter(
                Expression.all(
                    Expression.any(
                        Expression.eq(Expression.geometryType(), Expression.literal("Polygon")),
                        Expression.eq(Expression.geometryType(), Expression.literal("MultiPolygon"))
                    ),
                    Expression.eq(Expression.get("id"), Expression.literal(alertId))
                )
            )
            layer.setProperties(PropertyFactory.fillOpacity(0.5f))
        } else {
            layer.setProperties(PropertyFactory.fillOpacity(0f))
        }
    }
} 