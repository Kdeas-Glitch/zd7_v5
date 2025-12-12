package com.bignerbranch.android.zd7_v5

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.bignerbranch.android.zd7_v5.Room.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManageDBActivity : AppCompatActivity() {

    private lateinit var tableSpinner: Spinner
    private lateinit var operationRadioGroup: RadioGroup
    private lateinit var formContainer: LinearLayout
    private lateinit var actionButton: Button
    private lateinit var cancelButton: Button
    private lateinit var statusTextView: TextView

    private lateinit var viewModel: ManageDBViewModel
    private var selectedTable = "Bus"
    private var selectedOperation = "add"

    //  для динамических полей
    private val inputFields = mutableMapOf<String, EditText>()

    //  все спиннеры в Map для удобного доступа
    private val spinnerMap = mutableMapOf<String, Spinner>()

    //  списки для спиннеров
    private var busList: List<Bus> = emptyList()
    private var driverList: List<Driver> = emptyList()
    private var routeList: List<Routes> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mange_db)

        // Инициализация View
        tableSpinner = findViewById(R.id.tableSpinner)
        operationRadioGroup = findViewById(R.id.operationRadioGroup)
        formContainer = findViewById(R.id.formContainer)
        actionButton = findViewById(R.id.actionButton)
        cancelButton = findViewById(R.id.cancelButton)
        statusTextView = findViewById(R.id.statusTextView)

        // Инициализация ViewModel
        val repository = (application as BusDepotApplication).repository
        viewModel = ViewModelProvider(
            this,
            ManageDBViewModelFactory(repository)
        )[ManageDBViewModel::class.java]

        // Настройка Spinner для выбора таблицы
        setupTableSpinner()

        // Настройка RadioGroup для выбора операции
        setupOperationRadioGroup()

        // Наблюдение за данными
        observeData()

        // Кнопка выполнения
        actionButton.setOnClickListener {
            performAction()
        }

        // Кнопка отмены
        cancelButton.setOnClickListener {
            val intent = Intent(this@ManageDBActivity, Choose_Action::class.java)
            startActivity(intent)
            finish()
        }

        // загрузка формы
        loadForm()
    }

    private fun setupTableSpinner() {
        val tables = arrayOf("Автобусы (Bus)", "Водители (Driver)", "Маршруты (Routes)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tables)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        tableSpinner.adapter = adapter

        tableSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedTable = when (position) {
                    0 -> "Bus"
                    1 -> "Driver"
                    2 -> "Routes"
                    else -> "Bus"
                }
                loadForm()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupOperationRadioGroup() {
        operationRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            selectedOperation = when (checkedId) {
                R.id.addRadio -> "add"
                R.id.editRadio -> "edit"
                R.id.deleteRadio -> "delete"
                else -> "add"
            }
            loadForm()
        }
    }

    private fun observeData() {
        viewModel.buses.observe(this) { buses ->
            busList = buses
            updateSpinner("bus", buses.map { "ID: ${it.busId} - ${it.number}" })
        }

        viewModel.drivers.observe(this) { drivers ->
            driverList = drivers
            updateSpinner("driver", drivers.map { "ID: ${it.drivId} - ${it.name}" })
        }

        viewModel.routes.observe(this) { routes ->
            routeList = routes
            updateSpinner("route", routes.map { "ID: ${it.routesId} - ${it.map.take(30)}..." })
        }

        viewModel.statusMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                showStatus(message, android.R.color.holo_green_dark)
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message.isNotEmpty()) {
                showStatus("Ошибка: $message", android.R.color.holo_red_dark)
            }
        }
    }

    private fun loadForm() {
        // Очищаем
        formContainer.removeAllViews()
        inputFields.clear()
        spinnerMap.clear()

        when (selectedTable) {
            "Bus" -> loadBusForm()
            "Driver" -> loadDriverForm()
            "Routes" -> loadRouteForm()
        }

        // Обновляем текст кнопки
        actionButton.text = when (selectedOperation) {
            "add" -> "Добавить"
            "edit" -> "Изменить"
            "delete" -> "Удалить"
            else -> "Выполнить"
        }

        //  обновляем ВСЕ спиннеры
        updateAllSpinners()
    }

    private fun loadBusForm() {
        when (selectedOperation) {
            "add" -> {
                //  добавления автобуса
                addTextField("number", "Номер автобуса:", "")
                addTextField("condition", "Состояние:", "Хорошее")
                addCheckBox("busy", "Занят", false)
            }
            "edit", "delete" -> {
                //  выбираем автобус
                addSpinnerField("busId", "Выберите автобус:", "bus")

                if (selectedOperation == "edit") {
                    //  для редактирования
                    addTextField("number", "Новый номер:", "")
                    addTextField("condition", "Новое состояние:", "")
                    addCheckBox("busy", "Занят", false)
                }
            }
        }
    }

    private fun loadDriverForm() {
        when (selectedOperation) {
            "add" -> {
                addTextField("name", "ФИО водителя:", "")
                addTextField("password", "Пароль:", "")
                addTextField("baseSalary", "Базовая зарплата:", "50000")
                addTextField("routesId", "Маршрут (например: '1,2,3' или 'городской'):", "") // ⬅️ String поле!
                addSpinnerField("busId", "ID автобуса:", "bus")
                addSpinnerField("baseBus", "Базовый автобус:", "bus")
                addTextField("bonusMultiplier", "Множитель бонуса:", "1.0")
            }
            "edit", "delete" -> {
                addSpinnerField("driverId", "Выберите водителя:", "driver")

                if (selectedOperation == "edit") {
                    addTextField("name", "Новое ФИО:", "")
                    addTextField("password", "Новый пароль:", "")
                    addTextField("baseSalary", "Новая базовая зарплата:", "")
                    addTextField("routesId", "Новый ID маршрута:", "") // Добавили поле
                    addSpinnerField("busId", "Новый ID автобуса:", "bus")
                    addSpinnerField("baseBus", "Новый базовый автобус:", "bus")
                    addTextField("bonusMultiplier", "Новый множитель бонуса:", "")
                }
            }
        }
    }

    private fun loadRouteForm() {
        when (selectedOperation) {
            "add" -> {
                addTextField("map", "Описание маршрута:", "")
            }
            "edit", "delete" -> {
                addSpinnerField("routeId", "Выберите маршрут:", "route")

                if (selectedOperation == "edit") {
                    addTextField("map", "Новое описание:", "")
                }
            }
        }
    }

    private fun addTextField(key: String, label: String, defaultValue: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val textView = TextView(this).apply {
            text = label
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val editText = EditText(this).apply {
            setText(defaultValue)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        layout.addView(textView)
        layout.addView(editText)
        formContainer.addView(layout)

        inputFields[key] = editText
    }

    private fun addSpinnerField(key: String, label: String, type: String) {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        val textView = TextView(this).apply {
            text = label
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val spinner = Spinner(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            // Начальное значение
            val defaultItems = listOf("Выберите...", "Нет данных")
            adapter = ArrayAdapter(this@ManageDBActivity,
                android.R.layout.simple_spinner_item,
                defaultItems)
            tag = type // Сохраняем тип в теге
        }

        layout.addView(textView)
        layout.addView(spinner)
        formContainer.addView(layout)

        //  спиннер в Map по ключу
        spinnerMap[key] = spinner
    }

    private fun addCheckBox(key: String, label: String, checked: Boolean) {
        val checkBox = CheckBox(this).apply {
            text = label
            isChecked = checked
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, 16)
            }
        }

        formContainer.addView(checkBox)

        // Создаем скрытое поле для хранения значения
        val hiddenField = EditText(this).apply {
            setText(checked.toString())
            visibility = View.GONE
        }
        formContainer.addView(hiddenField)
        inputFields[key] = hiddenField

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            hiddenField.setText(isChecked.toString())
        }
    }

    private fun updateAllSpinners() {
        updateSpinner("bus", busList.map { "ID: ${it.busId} - ${it.number}" })
        updateSpinner("driver", driverList.map { "ID: ${it.drivId} - ${it.name}" })
        updateSpinner("route", routeList.map { "ID: ${it.routesId} - ${it.map.take(30)}..." })
    }

    private fun updateSpinner(type: String, items: List<String>) {
        // все спиннеры этого типа
        spinnerMap.forEach { (key, spinner) ->
            if (spinner.tag == type) {
                val spinnerItems = if (items.isEmpty()) {
                    listOf("Нет данных")
                } else {
                    listOf("Выберите...") + items
                }
                updateSpinnerAdapter(spinner, spinnerItems)
            }
        }
    }

    private fun updateSpinnerAdapter(spinner: Spinner, items: List<String>) {
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
    }

    private fun performAction() {
        when (selectedOperation) {
            "add" -> addRecord()
            "edit" -> editRecord()
            "delete" -> deleteRecord()
        }
    }

    private fun addRecord() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (selectedTable) {
                    "Bus" -> {
                        val number = inputFields["number"]?.text.toString().trim()
                        val condition = inputFields["condition"]?.text.toString().trim()
                        val busy = inputFields["busy"]?.text.toString().toBoolean()

                        if (number.isEmpty() || condition.isEmpty()) {
                            showError("Заполните все поля")
                            return@launch
                        }

                        val bus = Bus(
                            condition = condition,
                            number = number,
                            busy = busy
                        )
                        viewModel.addBus(bus)
                    }
                    "Driver" -> {
                        val name = inputFields["name"]?.text.toString().trim()
                        val password = inputFields["password"]?.text.toString().trim()
                        val baseSalaryText = inputFields["baseSalary"]?.text.toString().trim()
                        val baseSalary = baseSalaryText.toDoubleOrNull() ?: 0.0
                        val routesId = inputFields["routesId"]?.text.toString().trim() // ⬅️ String, не нужно конвертировать в Int!
                        val bonusMultiplierText = inputFields["bonusMultiplier"]?.text.toString().trim()
                        val bonusMultiplier = bonusMultiplierText.toDoubleOrNull() ?: 1.0

                        val busId = getSelectedIdFromSpinner("busId")
                        val baseBus = getSelectedIdFromSpinner("baseBus")

                        if (name.isEmpty()) {
                            showError("Введите ФИО водителя")
                            return@launch
                        }

                        val driver = Driver(
                            name = name,
                            password = password,
                            baseSalary = baseSalary,
                            routesId = routesId, // ⬅️ Просто String
                            busId = busId,
                            basebus = baseBus,
                            bonusMultiplier = bonusMultiplier
                        )
                        viewModel.addDriver(driver)
                    }
                    "Routes" -> {
                        val map = inputFields["map"]?.text.toString().trim()

                        if (map.isEmpty()) {
                            showError("Введите описание маршрута")
                            return@launch
                        }

                        val route = Routes(map = map)
                        viewModel.addRoute(route)
                    }
                }

                // данные после добавления
                CoroutineScope(Dispatchers.Main).launch {
                    clearForm()
                    // Перезагружаем форму для обновления спиннеров
                    loadForm()
                }

            } catch (e: Exception) {
                showError(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    private fun editRecord() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (selectedTable) {
                    "Bus" -> {
                        val selectedBus = getSelectedBus()
                        if (selectedBus != null) {
                            val newNumber = inputFields["number"]?.text.toString().trim()
                            val newCondition = inputFields["condition"]?.text.toString().trim()
                            val newBusy = inputFields["busy"]?.text.toString().toBoolean()

                            selectedBus.number = if (newNumber.isNotEmpty()) newNumber else selectedBus.number
                            selectedBus.condition = if (newCondition.isNotEmpty()) newCondition else selectedBus.condition
                            selectedBus.busy = newBusy

                            viewModel.updateBus(selectedBus)
                        } else {
                            showError("Выберите автобус")
                        }
                    }
                    "Driver" -> {
                        val selectedDriver = getSelectedDriver()
                        if (selectedDriver != null) {
                            val newName = inputFields["name"]?.text.toString().trim()
                            val newPassword = inputFields["password"]?.text.toString().trim()
                            val newBaseSalaryText = inputFields["baseSalary"]?.text.toString().trim()
                            val newBaseSalary = if (newBaseSalaryText.isNotEmpty())
                                newBaseSalaryText.toDoubleOrNull() ?: selectedDriver.baseSalary
                            else selectedDriver.baseSalary

                            val newBonusMultiplierText = inputFields["bonusMultiplier"]?.text.toString().trim()
                            val newBonusMultiplier = if (newBonusMultiplierText.isNotEmpty())
                                newBonusMultiplierText.toDoubleOrNull() ?: selectedDriver.bonusMultiplier
                            else selectedDriver.bonusMultiplier

                            // Получаем новые ID из спиннеров (убрали routesId)
                            val newBusId = getSelectedIdFromSpinner("busId", selectedDriver.busId)
                            val newBaseBus = getSelectedIdFromSpinner("baseBus", selectedDriver.basebus)

                            selectedDriver.name = if (newName.isNotEmpty()) newName else selectedDriver.name
                            selectedDriver.password = if (newPassword.isNotEmpty()) newPassword else selectedDriver.password
                            selectedDriver.baseSalary = newBaseSalary
                            selectedDriver.busId = newBusId
                            selectedDriver.basebus = newBaseBus
                            selectedDriver.bonusMultiplier = newBonusMultiplier

                            viewModel.updateDriver(selectedDriver)
                        } else {
                            showError("Выберите водителя")
                        }
                    }
                    "Routes" -> {
                        val selectedRoute = getSelectedRoute()
                        if (selectedRoute != null) {
                            val newMap = inputFields["map"]?.text.toString().trim()

                            selectedRoute.map = if (newMap.isNotEmpty()) newMap else selectedRoute.map
                            viewModel.updateRoute(selectedRoute)
                        } else {
                            showError("Выберите маршрут")
                        }
                    }
                }

                // Обновляем данные после редактирования
                CoroutineScope(Dispatchers.Main).launch {
                    clearForm()
                    loadForm()
                }

            } catch (e: Exception) {
                showError(e.message ?: "Неизвестная ошибка")
            }
        }
    }

    private fun deleteRecord() {
        AlertDialog.Builder(this)
            .setTitle("Подтверждение удаления")
            .setMessage("Вы уверены, что хотите удалить эту запись?")
            .setPositiveButton("Удалить") { _, _ ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        when (selectedTable) {
                            "Bus" -> {
                                val selectedBus = getSelectedBus()
                                selectedBus?.let { viewModel.deleteBus(it) }
                                    ?: showError("Выберите автобус")
                            }
                            "Driver" -> {
                                val selectedDriver = getSelectedDriver()
                                selectedDriver?.let { viewModel.deleteDriver(it) }
                                    ?: showError("Выберите водителя")
                            }
                            "Routes" -> {
                                val selectedRoute = getSelectedRoute()
                                selectedRoute?.let { viewModel.deleteRoute(it) }
                                    ?: showError("Выберите маршрут")
                            }
                        }

                        // Обновляем данные после удаления
                        CoroutineScope(Dispatchers.Main).launch {
                            clearForm()
                            loadForm()
                        }

                    } catch (e: Exception) {
                        showError(e.message ?: "Неизвестная ошибка")
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun getSelectedIdFromSpinner(spinnerKey: String, defaultValue: Int = 1): Int {
        val spinner = spinnerMap[spinnerKey] ?: return defaultValue
        val selectedText = spinner.selectedItem as? String ?: return defaultValue

        if (selectedText == "Выберите..." || selectedText == "Нет данных") {
            return defaultValue
        }

        // Парсим ID из текста "ID: 1 - Номер/Имя"
        return try {
            selectedText.substringAfter("ID: ").substringBefore(" -").toInt()
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun getSelectedBus(): Bus? {
        val spinner = spinnerMap["busId"] ?: return null
        val selectedText = spinner.selectedItem as? String ?: return null
        if (selectedText == "Выберите..." || selectedText == "Нет данных") return null

        val id = try {
            selectedText.substringAfter("ID: ").substringBefore(" -").toInt()
        } catch (e: Exception) {
            return null
        }

        return busList.find { it.busId == id }
    }

    private fun getSelectedDriver(): Driver? {
        val spinner = spinnerMap["driverId"] ?: return null
        val selectedText = spinner.selectedItem as? String ?: return null
        if (selectedText == "Выберите..." || selectedText == "Нет данных") return null

        val id = try {
            selectedText.substringAfter("ID: ").substringBefore(" -").toInt()
        } catch (e: Exception) {
            return null
        }

        return driverList.find { it.drivId == id }
    }

    private fun getSelectedRoute(): Routes? {
        val spinner = spinnerMap["routeId"] ?: return null
        val selectedText = spinner.selectedItem as? String ?: return null
        if (selectedText == "Выберите..." || selectedText == "Нет данных") return null

        val id = try {
            selectedText.substringAfter("ID: ").substringBefore(" -").toInt()
        } catch (e: Exception) {
            return null
        }

        return routeList.find { it.routesId == id }
    }

    private fun clearForm() {
        inputFields.values.forEach { it.setText("") }
    }

    private fun showStatus(message: String, colorResId: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            statusTextView.text = message
            statusTextView.visibility = View.VISIBLE
            statusTextView.setTextColor(getColor(colorResId))

            // Скрыть сообщение через 3 секунды
            statusTextView.postDelayed({
                statusTextView.visibility = View.GONE
            }, 3000)
        }
    }

    private fun showError(message: String) {
        showStatus("Ошибка: $message", android.R.color.holo_red_dark)
    }
}