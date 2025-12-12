package com.bignerbranch.android.zd7_v5

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log // ✅ Добавьте этот импорт
import android.view.View
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.lifecycleScope
import com.bignerbranch.android.zd7_v5.Room.*
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first // ✅ Добавьте этот импорт
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: BusDepotViewModel by viewModels {
        BusDepotViewModelFactory((application as BusDepotApplication).repository)
    }

    private val PREF_NAME = "AppPreferences"
    private val KEY_DISLOGIN = "dislogin"
    private val KEY_DIS_PASSWORD = "dispassword"
    lateinit var repository: BusDepotRepository

    private var currentState = 0 // 0: красный, 1: зеленый, 2: синий
    lateinit var DisLog: EditText
    lateinit var DisPas: EditText
    lateinit var DrivLog: EditText
    lateinit var DrivPas: EditText
    lateinit var PasLog: EditText
    lateinit var PasPas: EditText
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupButtons()
        setupMotionLayoutListener()
        sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        DisLog = findViewById(R.id.DisLog)
        DisPas = findViewById(R.id.DisPass)
        DrivLog = findViewById(R.id.DrivLog)
        DrivPas = findViewById(R.id.DrivPass)
        PasLog = findViewById(R.id.PassLog)
        PasPas = findViewById(R.id.PassPassw)

        val database = BusDepotDatabase.getDatabase(this@MainActivity)

        lifecycleScope.launch {
            // addTestData(database)
            checkDatabaseStructure() // ✅ Проверка структуры базы
        }
    }

    // ✅ Новый метод для проверки структуры базы данных
    private suspend fun checkDatabaseStructure() {
        try {
            val database = BusDepotDatabase.getDatabase(this@MainActivity)
            val driverDao = database.driverDao()

            // Проверить структуру таблицы
            val drivers = driverDao.getAllDrivers().first() // Используем .first() из kotlinx.coroutines.flow

            Log.d("DB Check", "Водителей в базе: ${drivers.size}")

            if (drivers.isNotEmpty()) {
                val driver = drivers.first()
                Log.d("DB Check", "Поля первого водителя: ")
                Log.d("DB Check", "- name: ${driver.name}")
                Log.d("DB Check", "- drivId: ${driver.drivId}")
                Log.d("DB Check", "- routesId: ${driver.routesId}") // ⬅️ Проверить здесь!
                Log.d("DB Check", "- baseSalary: ${driver.baseSalary}")
                Log.d("DB Check", "- busId: ${driver.busId}")
                Log.d("DB Check", "- bonusMultiplier: ${driver.bonusMultiplier}")
            } else {
                Log.d("DB Check", "Таблица Drivers пустая")
                // Добавим тестового водителя для проверки
                addTestDriverWithRoute()
            }
        } catch (e: Exception) {
            Log.e("DB Check", "Ошибка при проверке базы: ${e.message}")
        }
    }

    // ✅ Метод для добавления тестового водителя с маршрутом
    private suspend fun addTestDriverWithRoute() {
        try {
            val database = BusDepotDatabase.getDatabase(this@MainActivity)

            // Сначала создадим маршрут и автобус если их нет
            database.routesDao().insert(Routes(map = "Тестовый маршрут 1"))
            database.busDao().insert(Bus(condition = "Хорошее", number = "ТЕСТ001", busy = false))

            // Добавим водителя с маршрутом
            val driver = Driver(
                name = "Тестовый Водитель",
                password = "test123",
                baseSalary = 50000.0,
                routesId = "1", // ✅ Маршрут ID 1
                busId = 1,
                basebus = 1,
                bonusMultiplier = 1.0
            )

            database.driverDao().insert(driver)
            Log.d("DB Check", "Тестовый водитель с маршрутом добавлен: routesId = ${driver.routesId}")
        } catch (e: Exception) {
            Log.e("DB Check", "Ошибка при добавлении тестового водителя: ${e.message}")
        }
    }

    private suspend fun addTestData(database: BusDepotDatabase) {
        database.routesDao().insert(Routes(map = "Маршрут 1"))
        database.busDao().insert(Bus(condition = "Хорошее", number = "А123АА", busy = false))

        // Используем полный конструктор с routesId
        database.driverDao().insert(
            Driver(
                drivId = 0, // autoGenerate = true
                name = "Тест1",
                password = "1232",
                baseSalary = 50000.0,
                routesId = "1", // ✅ Добавлен маршрут
                busId = 1,
                basebus = 1,
                bonusMultiplier = 1.0
            )
        )
    }

    private fun setupButtons() {
        findViewById<android.widget.Button>(R.id.btn_prev).setOnClickListener {
            navigateToPrevious()
        }

        findViewById<android.widget.Button>(R.id.btn_next).setOnClickListener {
            navigateToNext()
        }
    }

    private fun setupMotionLayoutListener() {
        findViewById<MotionLayout>(R.id.motionLayout).setTransitionListener(
            object : MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {
                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    currentState = when (currentId) {
                        R.id.red_state -> 0
                        R.id.green_state -> 1
                        R.id.blue_state -> 2
                        else -> currentState
                    }
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {
                }
            }
        )
    }

    private fun navigateToNext() {
        val motionLayout = findViewById<MotionLayout>(R.id.motionLayout)
        when (currentState) {
            0 -> motionLayout.transitionToState(R.id.green_state)
            1 -> motionLayout.transitionToState(R.id.blue_state)
            2 -> motionLayout.transitionToState(R.id.red_state)
        }
    }

    private fun navigateToPrevious() {
        val motionLayout = findViewById<MotionLayout>(R.id.motionLayout)
        when (currentState) {
            0 -> motionLayout.transitionToState(R.id.blue_state)
            1 -> motionLayout.transitionToState(R.id.red_state)
            2 -> motionLayout.transitionToState(R.id.green_state)
        }
    }

    fun DispLog(view: View) {
        val savedDislogin = sharedPreferences.getString("dislogin", "") ?: ""
        val savedDisPassword = sharedPreferences.getString("dispassword", "") ?: ""

        val inputLogin = DisLog.text.toString()
        val inputPassword = DisPas.text.toString()

        if (inputLogin == savedDislogin && inputPassword == savedDisPassword) {
            sharedPreferences.edit()
                .putString("dislogin", inputLogin)
                .putString("dispassword", inputPassword)
                .putString("rights", "Admin")
                .apply()

            val intent = Intent(this@MainActivity, Choose_Action::class.java)
            startActivity(intent)
            finish()
        } else {
            Snackbar.make(view, "Неверные Данные", Snackbar.LENGTH_SHORT).show()
        }
    }

    fun DrivLog(view: View) {
        lifecycleScope.launch {
            val inputName = DrivLog.text.toString()
            val inputPassword = DrivPas.text.toString()

            val driver = viewModel.getPasswordByName(inputName)

            if (driver != null) {
                if (driver.password == inputPassword) {
                    sharedPreferences.edit()
                        .putString("rights", "Driver")
                        .putString("driverId", driver.drivId.toString())
                        .putString("driverName", driver.name)
                        .putString("driverRoute", driver.routesId.toString()) // ✅ Сохраняем маршрут
                        .apply()

                    val intent = Intent(this@MainActivity, Choose_Action::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Snackbar.make(view, "Неверный пароль", Snackbar.LENGTH_SHORT).show()
                }
            } else {
                Snackbar.make(view, "Такого пользователя нет", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun PassLog(view: View) {
        val login = PasLog.text.toString()
        val password = PasPas.text.toString()

        if (login.isNotEmpty() && password.isNotEmpty()) {
            sharedPreferences.edit()
                .putString("rights", "Pass")
                .putString("passengerLogin", login)
                .apply()

            val intent = Intent(this@MainActivity, Choose_Action::class.java)
            startActivity(intent)
            finish()
        } else {
            Snackbar.make(view, "Есть пустые поля", Snackbar.LENGTH_SHORT).show()
        }
    }
}