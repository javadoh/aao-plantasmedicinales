package com.javadoh.plantasmedicinales.io

/**
 * Created by luiseliberal on 20-09-2015.
 * Modernized to Kotlin.
 */
object Constants {

    const val URL_SERVIDOR_RMT_APP_HIERBAS = "http://srv1103347.hstgr.cloud:8000/"
    const val GET_ALL_OPERACION = "hierbas/findAll"
    const val GET_HIERBAS_OPERACION = "hierbas/findByName" // ?nombre= &localization=ES o EN
    const val GET_SINTOMAS_OPERACION = "hierbas/findBySymptom" // ?sintoma= &localization=ES o EN
    const val INSERT_HIERBAS_OPERACION = ""
    const val INSERT_SINTOMAS_OPERACION = ""
    const val URL_IMAGEN_PREDEFINIDA = "/drawable/back_herb_predef.png"
    const val URL_POST_COMENTARIO_HIERBA = "hierbas/addComment/"

    var isAdsDisabled: Boolean = false
    var isInAppSetupCreated: Boolean = false
    var internetOn: Boolean = false

    val europeCountries = arrayOf(
        "Spain", "España", "Francia", "France", "Alemania", "Germany", "Deutschland",
        "Holanda", "Holland", "Bélgica", "Belgium", "Italia", "Italy", "Grecia", "Grecce",
        "Suiza", "Switzerland", "Suecia", "Sweden", "Noruega", "Norwey", "Turquía", "Turkey",
        "Bulgaria", "Croatia", "Croacia", "Austria", "Cyprus", "Chipre", "Czech Republic",
        "República Checa", "Estonia", "Denmark", "Dinamarca", "Finland", "Finlandia",
        "Hungary", "Hungría", "Irlanda", "Ireland", "Latvia", "Lithuania", "Slovakia",
        "Eslovaquia", "Slovenia", "Eslovenia", "Europe", "Europa", "European Union", "Unión Europea"
    )

    const val URL_GRAPH_FACEBOOK_ME_DATA = "https://graph.facebook.com/me?fields=id,name,gender,birthday,email,location&access_token="

    // APP KEY
    const val APP_KEY_ACC = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnSbkMq/xVyYeCSEdXxj6AmJ24X9/pb6U6LnfpS1ehan4FdIihHkdWL9kwVxA3lAPgQfGvhkB/VtbO7zeu9C7NTbJKlZ1xZp1+UAFFI8KvwvUKl1/7Pbuxu4oC/+U/OthxO/CAt8Gt3C97E4qHqKkbeIsAciYwMqRQpNt4/SWy37yr4w1qRI34JTu6gDfS/AvOtZneZOKLCd0fLshHvHOuQ0qKFXfjvWXrjkWT5vj5wXaaMuxN+X/EQxLTs7XYU9QbZDpfy1neZOAUoBoG/V1oAT2f0xsxHtWXnw75K5qCKeB5ESOiB1Tv4U+iFgUwnu+xC38cwCIzuyRDwcXBsN95wIDAQAB"
    const val REMOVE_ADD_PRODUCT = "remove_ads"
}