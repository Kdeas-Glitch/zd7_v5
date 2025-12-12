package com.bignerbranch.android.zd7_v5

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class Choose_Action : AppCompatActivity() {

    lateinit var CheckBuss: AppCompatButton
    lateinit var CheckDrivers: AppCompatButton
    lateinit var CheckRoute: AppCompatButton
    lateinit var AddBuss: AppCompatButton
    lateinit var etRouteIdInput: EditText
    lateinit var btnFindBusesByRoute: Button
    lateinit var tvResult: TextView

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_choose_action)

        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val r = sharedPreferences.getString("rights", "")

        // Инициализация View
        CheckBuss = findViewById(R.id.CheckBus)
        CheckDrivers = findViewById(R.id.CheckDrive)
        CheckRoute = findViewById(R.id.CheckMap)
        AddBuss = findViewById(R.id.AddBus)
        etRouteIdInput = findViewById(R.id.etRouteIdInput)
        btnFindBusesByRoute = findViewById(R.id.btnFindBusesByRoute)
        tvResult = findViewById(R.id.tvResult)

        // Показываем элементы в зависимости от прав
        if (r == "Admin") {
            CheckBuss.isVisible = true
            CheckDrivers.isVisible = true
            CheckRoute.isVisible = true
            AddBuss.isVisible = true
            etRouteIdInput.isVisible = true
            btnFindBusesByRoute.isVisible = true
        }

        if (r == "Driver") {
            CheckBuss.isVisible = true
            CheckDrivers.isVisible = true
            CheckRoute.isVisible = true
            etRouteIdInput.isVisible = true
            btnFindBusesByRoute.isVisible = true
        }

        // Настраиваем кнопку поиска
        btnFindBusesByRoute.setOnClickListener {
            findBusesForRoute()
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun findBusesForRoute() {
        val routeIdText = etRouteIdInput.text.toString().trim()

        if (routeIdText.isEmpty()) {
            tvResult.text = "Введите ID маршрута."
            tvResult.isVisible = true
            return
        }

        tvResult.text = "🔍 Поиск автобусов для маршрута: '$routeIdText'..."
        tvResult.isVisible = true

        lifecycleScope.launch {
            try {
                // 1. Получаем доступ к базе данных
                val database = com.bignerbranch.android.zd7_v5.Room.BusDepotDatabase.getDatabase(this@Choose_Action)
                val busDao = database.busDao()
                val driverDao = database.driverDao()

                // 2. Получаем ВСЕХ водителей из базы - исправленный вариант
                val allDrivers = try {
                    // Способ 1: Используем first() из kotlinx.coroutines.flow
                    driverDao.getAll().first()
                } catch (e: NoSuchElementException) {
                    // Если Flow пустой
                    emptyList<com.bignerbranch.android.zd7_v5.Room.Driver>()
                } catch (e: Exception) {
                    // Любая другая ошибка
                    Log.e("Choose_Action", "Ошибка при получении водителей: ${e.message}", e)
                    emptyList<com.bignerbranch.android.zd7_v5.Room.Driver>()
                }

                if (allDrivers.isEmpty()) {
                    runOnUiThread {
                        tvResult.text = "❌ В базе нет водителей.\n" +
                                "Добавьте водителей через 'Управление базой данных'"
                    }
                    return@launch
                }

                // 3. Ищем водителей на указанном маршруте
                val driversOnRoute = mutableListOf<com.bignerbranch.android.zd7_v5.Room.Driver>()

                for (driver in allDrivers) {
                    val driverRoutes = driver.routesId
                    when {
                        driverRoutes.isEmpty() -> continue
                        driverRoutes == routeIdText -> driversOnRoute.add(driver)
                        driverRoutes.contains(routeIdText) -> {
                            // Проверяем чтобы "2" не находило "12" или "21"
                            val routeParts = driverRoutes.split(",").map { it.trim() }
                            if (routeParts.any { it == routeIdText }) {
                                driversOnRoute.add(driver)
                            }
                        }
                    }
                }

                // 4. Если водителей на маршруте нет
                if (driversOnRoute.isEmpty()) {
                    runOnUiThread {
                        // Собираем все маршруты которые есть в базе
                        val allRoutes = mutableSetOf<String>()

                        for (driver in allDrivers) {
                            val routes = driver.routesId
                            if (routes.isNotEmpty()) {
                                routes.split(",").map { it.trim() }.forEach { route ->
                                    if (route.isNotEmpty()) {
                                        allRoutes.add(route)
                                    }
                                }
                            }
                        }

                        val sortedRoutes = allRoutes.toList().sorted()

                        if (sortedRoutes.isEmpty()) {
                            tvResult.text = "❌ В базе нет данных о маршрутах.\n" +
                                    "Добавьте водителей и укажите их маршруты через 'Управление базой данных'"
                        } else {
                            tvResult.text = "❌ Нет водителей на маршруте '$routeIdText'\n" +
                                    "Доступные маршруты: ${sortedRoutes.joinToString(separator = ", ")}"
                        }
                    }
                    return@launch
                }

                // 5. Для каждого найденного водителя получаем его автобус
                val busNumbers = mutableListOf<String>()
                val busDetails = mutableListOf<String>()

                for (driver in driversOnRoute) {
                    val bus = busDao.getById(driver.busId)
                    if (bus != null) {
                        busNumbers.add(bus.number)
                        busDetails.add("🚌 ${bus.number} (Водитель: ${driver.name}, ID автобуса: ${bus.busId})")
                    } else {
                        busDetails.add("❌ Автобус ID:${driver.busId} не найден (Водитель: ${driver.name})")
                    }
                }

                // 6. Показываем результаты
                runOnUiThread {
                    val uniqueBusNumbers = busNumbers.distinct()

                    if (uniqueBusNumbers.isEmpty()) {
                        tvResult.text = "⚠️ Найдены водители на маршруте, но у них нет автобусов:\n" +
                                busDetails.joinToString(separator = "\n")
                    } else {
                        val resultText = StringBuilder()
                        resultText.append("✅ На маршруте '$routeIdText' работают ${driversOnRoute.size} водителей\n")
                        resultText.append("📊 Уникальных автобусов: ${uniqueBusNumbers.size}\n\n")
                        resultText.append("🚌 Автобусы:\n")
                        resultText.append(busDetails.joinToString(separator = "\n"))

                        // Дополнительная статистика
                        resultText.append("\n\n📈 Статистика:")
                        resultText.append("\n• Водителей на маршруте: ${driversOnRoute.size}")
                        resultText.append("\n• Уникальных автобусов: ${uniqueBusNumbers.size}")
                        resultText.append("\n• Номера автобусов: ${uniqueBusNumbers.joinToString(separator = ", ")}")

                        tvResult.text = resultText.toString()
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    tvResult.text = "❌ Ошибка базы данных: ${e.message}"
                    Log.e("Choose_Action", "Общая ошибка", e)
                }
            }
        }
    }

    fun CheckRouteBuss(view: View) {
        val intent = Intent(this@Choose_Action, CheckDB::class.java)
        intent.putExtra("KEY_STRING", "CheckRouteBuss")
        startActivity(intent)
        finish()
    }

    fun CheckBusses(view: View) {
        val intent = Intent(this@Choose_Action, CheckDB::class.java)
        intent.putExtra("KEY_STRING", "CheckBusses")
        startActivity(intent)
        finish()
    }

    fun CheckDrivers(view: View) {
        val intent = Intent(this@Choose_Action, CheckDB::class.java)
        intent.putExtra("KEY_STRING", "CheckDrivers")
        startActivity(intent)
        finish()
    }

    fun Checkroutes(view: View) {
        val intent = Intent(this@Choose_Action, CheckDB::class.java)
        intent.putExtra("KEY_STRING", "Checkroutes")
        startActivity(intent)
        finish()
    }

    fun AddBuss(view: View) {
        val intent = Intent(this@Choose_Action, ManageDBActivity::class.java)
        startActivity(intent)
        finish()
    }

}