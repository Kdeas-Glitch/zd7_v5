package com.bignerbranch.android.zd7_v5

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bignerbranch.android.zd7_v5.Room.*

class CheckDB : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var titleTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var emptyStateLayout: LinearLayout
    private lateinit var emptyStateTextView: TextView
    private lateinit var groupButton: Button
    private lateinit var ungroupButton: Button

    private lateinit var viewModel: CheckDBViewModel
    private var currentDatabaseType = ""

    private var isGroupedByCondition = false
    private var isGroupedByRoute = false
    private var isGroupedByBus = false // Изменили с isGroupedByRoute на isGroupedByBus

    private var originalBuses: List<Bus> = emptyList()
    private var originalDrivers: List<Driver> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_check_db)

        Log.d("CheckDB", "Activity создается")

        // Инициализируем View
        recyclerView = findViewById(R.id.recyclerView)
        titleTextView = findViewById(R.id.titleTextView)
        progressBar = findViewById(R.id.progressBar)
        errorTextView = findViewById(R.id.errorTextView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
        emptyStateTextView = findViewById(R.id.emptyStateTextView)
        groupButton = findViewById(R.id.groupButton)
        ungroupButton = findViewById(R.id.ungroupButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Инициализация ViewModel
        val repository = (application as BusDepotApplication).repository
        Log.d("CheckDB", "Repository: $repository")

        viewModel = ViewModelProvider(
            this,
            CheckDBViewModelFactory(repository)
        )[CheckDBViewModel::class.java]

        currentDatabaseType = intent.getStringExtra("KEY_STRING") ?: "CheckBusses"
        Log.d("CheckDB", "Тип базы данных: $currentDatabaseType")

        titleTextView.text = viewModel.getTitleForType(currentDatabaseType)

        // Показываем кнопки группировки только для автобусов и водителей
        if (currentDatabaseType == "CheckBusses" || currentDatabaseType == "CheckDrivers") {
            groupButton.visibility = View.VISIBLE
            groupButton.text = when (currentDatabaseType) {
                "CheckBusses" -> "📊 Группировать по состоянию"
                "CheckDrivers" -> "📊 Группировать по автобусу" // Изменили текст
                else -> "📊 Группировать"
            }
        }

        // Настройка кнопок
        groupButton.setOnClickListener {
            when (currentDatabaseType) {
                "CheckBusses" -> {
                    if (!isGroupedByCondition) {
                        groupBusesByCondition()
                    }
                }
                "CheckDrivers" -> {
                    if (!isGroupedByBus) { // Исправлено на isGroupedByBus
                        groupDriversByBus() // Исправлено на groupDriversByBus
                    }
                }
            }
        }

        ungroupButton.setOnClickListener {
            resetGrouping()
        }

        // Наблюдаем за загрузкой
        viewModel.isLoading.observe(this) { isLoading ->
            Log.d("CheckDB", "isLoading: $isLoading")
            if (isLoading) {
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
            }
        }

        // Наблюдаем за ошибками
        viewModel.errorMessage.observe(this) { error ->
            if (error.isNotEmpty()) {
                showError(error)
            }
        }

        // Наблюдаем за данными
        when (currentDatabaseType) {
            "CheckBusses" -> {
                viewModel.buses.observe(this) { buses ->
                    Log.d("CheckDB", "LiveData buses обновлена: ${buses.size} элементов")
                    originalBuses = buses
                    handleData(buses)
                }
            }
            "CheckDrivers" -> {
                viewModel.drivers.observe(this) { drivers ->
                    Log.d("CheckDB", "LiveData drivers обновлена: ${drivers.size} элементов")
                    originalDrivers = drivers
                    handleData(drivers)
                }
            }
            "Checkroutes" -> {
                viewModel.routes.observe(this) { routes ->
                    Log.d("CheckDB", "LiveData routes обновлена: ${routes.size} элементов")
                    handleData(routes)
                }
            }
            "CheckRouteBuss" -> {
                viewModel.routeBusLinks.observe(this) { links ->
                    Log.d("CheckDB", "LiveData links обновлена: ${links.size} элементов")
                    handleData(links)
                }
            }
        }

        // Первоначальная загрузка данных
        viewModel.loadData(currentDatabaseType)
    }

    private fun <T> handleData(data: List<T>) {
        if (data.isNotEmpty()) {
            showData()
            setupAdapter(data)
        } else {
            showEmptyState("Нет данных")
        }
    }

    private fun <T> setupAdapter(data: List<T>) {
        val adapter = when (currentDatabaseType) {
            "CheckBusses" -> BusAdapter(data as List<Bus>)
            "CheckDrivers" -> DriverAdapter(data as List<Driver>)
            "Checkroutes" -> RouteAdapter(data as List<Routes>)
            "CheckRouteBuss" -> RouteBusLinkAdapter(data as List<RouteBusLink>)
            else -> null
        }

        if (adapter != null) {
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(this)
            Log.d("CheckDB", "Адаптер установлен для $currentDatabaseType")
        }
    }
    private fun groupDriversByRoute() {
        val groupedDrivers: Map<String, List<Driver>> = originalDrivers.groupBy { it.routesId } // ⬅️ Map<String, ...>

        val groupedList: MutableList<Any> = mutableListOf()

        groupedDrivers.forEach { (routeId, drivers) ->
            groupedList.add(GroupHeader(
                title = "Маршрут: ${if (routeId.isEmpty()) "нет" else routeId}",
                itemCount = drivers.size
            ))
            groupedList.addAll(drivers)
        }

        recyclerView.adapter = GroupedDriverAdapter(groupedList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        isGroupedByRoute = true
        updateGroupButtons()
    }

    // Группировка автобусов по состоянию
    private fun groupBusesByCondition() {
        val groupedBuses: Map<String, List<Bus>> = originalBuses.groupBy { it.condition } // Явно указываем тип

        // Создаем список с заголовками групп
        val groupedList: MutableList<Any> = mutableListOf()

        groupedBuses.forEach { (condition, buses) ->
            // Добавляем заголовок группы
            groupedList.add(GroupHeader(
                title = "Состояние: $condition",
                itemCount = buses.size
            ))

            // Добавляем автобусы этой группы
            groupedList.addAll(buses)
        }

        // Используем адаптер для группировки
        recyclerView.adapter = GroupedBusAdapter(groupedList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        isGroupedByCondition = true
        updateGroupButtons()
    }

    // Группировка водителей по автобусу (вместо маршрута)
    private fun groupDriversByBus() {
        val groupedDrivers: Map<Int, List<Driver>> = originalDrivers.groupBy { it.busId }

        val groupedList: MutableList<Any> = mutableListOf()

        groupedDrivers.forEach { (busId, drivers) ->
            groupedList.add(GroupHeader(
                title = "Автобус ID: $busId",
                itemCount = drivers.size
            ))
            groupedList.addAll(drivers)
        }

        recyclerView.adapter = GroupedDriverAdapter(groupedList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        isGroupedByBus = true
        updateGroupButtons()
    }

    // Сброс группировки
    private fun resetGrouping() {
        when (currentDatabaseType) {
            "CheckBusses" -> {
                isGroupedByCondition = false
                setupAdapter(originalBuses)
            }
            "CheckDrivers" -> {
                isGroupedByBus = false // Исправлено
                setupAdapter(originalDrivers)
            }
        }
        updateGroupButtons()
    }

    // Обновление состояния кнопок
    private fun updateGroupButtons() {
        val isGrouped = when (currentDatabaseType) {
            "CheckBusses" -> isGroupedByCondition
            "CheckDrivers" -> isGroupedByBus // Исправлено
            else -> false
        }

        if (isGrouped) {
            groupButton.visibility = View.GONE
            ungroupButton.visibility = View.VISIBLE
        } else {
            groupButton.visibility = View.VISIBLE
            ungroupButton.visibility = View.GONE
        }
    }

    private fun showData() {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.VISIBLE
        errorTextView.visibility = View.GONE
        emptyStateLayout.visibility = View.GONE
        Log.d("CheckDB", "Показываем данные")
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorTextView.visibility = View.VISIBLE
        emptyStateLayout.visibility = View.GONE
        errorTextView.text = message
        Log.e("CheckDB", "Ошибка: $message")
    }

    private fun showEmptyState(message: String) {
        progressBar.visibility = View.GONE
        recyclerView.visibility = View.GONE
        errorTextView.visibility = View.GONE
        emptyStateLayout.visibility = View.VISIBLE
        emptyStateTextView.text = message
        Log.d("CheckDB", "Пустое состояние: $message")
    }

    fun back(view: View) {
        val intent = Intent(this@CheckDB, Choose_Action::class.java)
        startActivity(intent)
        finish()
    }
}

// Класс для заголовка группы
data class GroupHeader(
    val title: String,
    val itemCount: Int
)

// Адаптер для группировки автобусов
class GroupedBusAdapter(private val items: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_BUS = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GroupHeader -> TYPE_HEADER
            is Bus -> TYPE_BUS
            else -> TYPE_BUS
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                // Создаем простой заголовок
                val textView = TextView(parent.context).apply {
                    textSize = 18f
                    setTextColor(parent.context.getColor(android.R.color.white))
                    setPadding(16, 16, 16, 16)
                    setBackgroundColor(parent.context.getColor(android.R.color.holo_blue_dark))
                }
                HeaderViewHolder(textView)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_bus, parent, false)
                BusViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = items[position] as GroupHeader
                holder.bind(header)
            }
            is BusViewHolder -> {
                val bus = items[position] as Bus
                holder.bind(bus)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolder для заголовка
    class HeaderViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(header: GroupHeader) {
            textView.text = "${header.title} (${header.itemCount})"
        }
    }

    // ViewHolder для автобуса
    class BusViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val busId: TextView = itemView.findViewById(R.id.busIdTextView)
        private val busNumber: TextView = itemView.findViewById(R.id.busNumberTextView)
        private val busCondition: TextView = itemView.findViewById(R.id.busConditionTextView)
        private val busStatus: TextView = itemView.findViewById(R.id.busStatusTextView)

        fun bind(bus: Bus) {
            busId.text = "ID: ${bus.busId}"
            busNumber.text = bus.number
            busCondition.text = "Состояние: ${bus.condition}"
            busStatus.text = if (bus.busy) "Занят" else "Свободен"
            busStatus.setTextColor(
                if (bus.busy) itemView.context.getColor(android.R.color.holo_red_dark)
                else itemView.context.getColor(android.R.color.holo_green_dark)
            )
        }
    }
}

// Адаптер для группировки водителей - ИСПРАВЛЕННАЯ ВЕРСИЯ
class GroupedDriverAdapter(private val items: List<Any>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_DRIVER = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is GroupHeader -> TYPE_HEADER
            is Driver -> TYPE_DRIVER
            else -> TYPE_DRIVER
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                // Создаем простой заголовок
                val textView = TextView(parent.context).apply {
                    textSize = 18f
                    setTextColor(parent.context.getColor(android.R.color.white))
                    setPadding(16, 16, 16, 16)
                    setBackgroundColor(parent.context.getColor(android.R.color.holo_green_dark))
                }
                HeaderViewHolder(textView)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_driver, parent, false)
                DriverViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val header = items[position] as GroupHeader
                holder.bind(header)
            }
            is DriverViewHolder -> {
                val driver = items[position] as Driver
                holder.bind(driver)
            }
        }
    }

    override fun getItemCount(): Int = items.size

    // ViewHolder для заголовка
    class HeaderViewHolder(private val textView: TextView) : RecyclerView.ViewHolder(textView) {
        fun bind(header: GroupHeader) {
            textView.text = "${header.title} (${header.itemCount})"
        }
    }

    // ViewHolder для водителя - ИСПРАВЛЕННЫЙ
    class DriverViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val driverName: TextView = itemView.findViewById(R.id.driverNameTextView)
        private val driverId: TextView = itemView.findViewById(R.id.driverIdTextView)
        private val driverBusId: TextView = itemView.findViewById(R.id.driverBusIdTextView)
        private val driverBaseSalary: TextView = itemView.findViewById(R.id.driverSalaryTextView)

        fun bind(driver: Driver) {
            driverName.text = driver.name
            driverId.text = "ID: ${driver.drivId}"
            driverBusId.text = "Автобус ID: ${driver.busId}"
            driverBaseSalary.text = "Зарплата: ${driver.baseSalary} ₽"

            // Если у вас есть поле bonusMultiplier, можно его тоже показать
            // val driverBonus: TextView = itemView.findViewById(R.id.driverBonusTextView)
            // driverBonus.text = "Бонус: x${driver.bonusMultiplier}"
        }
    }
}