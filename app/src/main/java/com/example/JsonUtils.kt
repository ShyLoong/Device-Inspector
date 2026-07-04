package com.example

import org.json.JSONArray
import org.json.JSONObject

object JsonUtils {
    fun exportToJson(data: List<DeviceInfoItem>): String {
        val jsonArray = JSONArray()
        data.forEach { item ->
            val jsonObject = JSONObject()
            jsonObject.put("category", item.category)
            jsonObject.put("key", item.key)
            jsonObject.put("value", item.value)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString(2)
    }

    fun importFromJson(jsonString: String): List<DeviceInfoItem> {
        val list = mutableListOf<DeviceInfoItem>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val category = jsonObject.getString("category")
                val key = jsonObject.getString("key")
                val value = jsonObject.getString("value")
                list.add(DeviceInfoItem(category, key, value))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}
