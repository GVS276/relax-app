package com.vg276.relaxapp.server

import android.content.Context
import android.os.StrictMode
import com.BoardiesITSolutions.AndroidMySQLConnector.*
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.InvalidSQLPacketException
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.MySQLConnException
import com.BoardiesITSolutions.AndroidMySQLConnector.Exceptions.MySQLException
import com.vg276.relaxapp.container.RecordContainer
import com.vg276.relaxapp.container.SpecialistContainer
import com.vg276.relaxapp.utils.hexToBytes
import com.vg276.relaxapp.utils.logE
import com.vg276.relaxapp.utils.logI
import java.io.IOException
import java.util.*

// Номер организации также нужен для входа под доступом - Администратор
// Логин  - +79196003070
// Пароль - relax_admin_password_2021

// Таблицы
const val TABLE_USERS    = "users_table"   // "CREATE TABLE users_table(Username TEXT,Password CHAR(30),Phone CHAR(30),Role CHAR(20))"
const val TABLE_SPECS    = "specs_table"   // "CREATE TABLE specs_table(LastName TEXT,FirstName TEXT,MiddleName TEXT,Biography TEXT,Phone CHAR(30))"
const val TABLE_RECORDS  = "records_table" // "CREATE TABLE records_table(RecId TEXT,FromUser CHAR(30),ToUser CHAR(30),TimeStamp TEXT)"

// Уровени доступа
enum class UserPrivileges
{
    // Обычный пользователь
    USER,
    // Сотрудник (массажист - специалист)
    STAFFER,
    // Администратор
    ADMIN
}

// Статусы
enum class UserFormError
{
    // Статус подключения к серверу SQL
    ConnectSuccess,
    ConnectFailure,

    // Статус авторизации
    AuthFailure,
    AuthSuccess,

    // Статус проверки пользователя на сервере
    UserCheckFailure,
    UserCheckSuccess,

    // Статус регистрации нового пользователя
    FormCreated,
    FormCreateFailure,

    // Статус для получения превилегий
    GetUserPrivileges,

    // Статус для получения данных о специалистах
    GetAllSpecs,

    // Статус для получения данных о всех записях
    GetAllRecords
}

// Обратный вызов
interface UserFormListener
{
    fun onResult(result: UserFormError, any: Any?)
}

class SQLServerCore(context: Context)
{
    private val ctx = context
    private var connection : Connection? = null

    init {
        strictModeInit()
    }

    // Игнорируем проверки безопасности
    private fun strictModeInit()
    {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
    }

    // Контекст приложения или активности
    fun getContext() : Context
    {
        return ctx
    }

    // Подключение к серверу SQL
    fun connect(listener: UserFormListener?)
    {
        val callback = object : IConnectionInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.ConnectFailure, null)
            }

            override fun actionCompleted() {
                logI("SQL server connected!")
                listener?.onResult(UserFormError.ConnectSuccess, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.ConnectFailure, null)
            }
        }

        try {
            connection = Connection(
                    "sql4.freesqldatabase.com",
                    "sql4419515",
                    "Aj6TnbzLgL", 3306,
                    "sql4419515", callback)
        } catch (e : Exception) {
            e.printStackTrace()
        }
    }

    // Переподключение к серверу
    fun reconnect()
    {
        if (connection == null)
            connect(null)
    }

    // Разрываем подключение к серверу
    fun closeConnection()
    {
        connection?.close()
        connection = null
    }

    // Можно создать новую таблицу
    fun createTable(table: String)
    {
        val callback = object : IConnectionInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
            }

            override fun actionCompleted() {
                logI("createTable success!")
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
            }
        }

        connection?.let {
            val statement = it.createStatement()
            statement.execute(table, callback)
        }
    }

    // Регистрация пользователя (создание новых ячеек в базе данных)
    fun createUser(userName: String, password: String, phone: String, role: String, listener: UserFormListener)
    {
        val callback = object : IConnectionInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun actionCompleted() {
                logI("createUser success!")
                listener.onResult(UserFormError.FormCreated, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.FormCreateFailure, null)
            }
        }

        connection?.let {
            val line = "INSERT INTO $TABLE_USERS (Username, Password, Phone, Role) VALUES ('$userName', '$password','$phone','$role')"
            val statement = it.createStatement()
            statement.execute(line, callback)
        }
    }

    // Добавляем информацию о специалистах
    fun createStaffer(
        lastName: String,
        firstName: String,
        middleName: String,
        biography: String,
        phone: String,
        listener: UserFormListener)
    {
        val callback = object : IConnectionInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun actionCompleted() {
                logI("createUser success!")
                listener.onResult(UserFormError.FormCreated, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.FormCreateFailure, null)
            }
        }

        connection?.let {
            val line = "INSERT INTO $TABLE_SPECS (LastName, FirstName, MiddleName, Biography, Phone) VALUES ('$lastName', '$firstName','$middleName','$biography','$phone')"
            val statement = it.createStatement()
            statement.execute(line, callback)
        }
    }

    // Проверка пользователя в базе данных
    fun checkUserForm(phone: String, listener: UserFormListener)
    {
        val callback = object : IResultInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun executionComplete(p0: ResultSet?) {
                p0?.let {
                    var row = it.nextRow
                    var result = UserFormError.UserCheckFailure
                    while (row != null)
                    {
                        val p = row.getString("Phone")
                        if (p == phone)
                        {
                            result = UserFormError.UserCheckSuccess
                            break
                        }
                        row = it.nextRow
                    }
                    listener.onResult(result, null)
                }
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

        }

        connection?.let {
            val line = "SELECT * FROM $TABLE_USERS WHERE Phone='$phone'"
            val statement = it.createStatement()
            statement.executeQuery(line, callback)
        }
    }

    // Авторизация пользователя
    fun doLogin(phone: String, password: String, listener: UserFormListener)
    {
        val callback = object : IResultInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun executionComplete(p0: ResultSet?) {
                p0?.let {
                    var row = it.nextRow
                    var result = UserFormError.AuthFailure
                    var u = ""
                    while (row != null)
                    {
                        val p = row.getString("Password")
                        if (p == password)
                        {
                            logI("Auth success!")
                            u = row.getString("Username")
                            result = UserFormError.AuthSuccess
                            break
                        }
                        row = it.nextRow
                    }
                    listener.onResult(result, u)
                }
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

        }

        connection?.let {
            val line = "SELECT * FROM $TABLE_USERS WHERE Phone='$phone'"
            val statement = it.createStatement()
            statement.executeQuery(line, callback)
        }
    }

    // Получение уровня доступа пользователя
    fun getUserPrivileges(phone: String, listener: UserFormListener)
    {
        val callback = object : IResultInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun executionComplete(p0: ResultSet?) {
                p0?.let {
                    var row = it.nextRow
                    while (row != null)
                    {
                        val login = row.getString("Phone")
                        if (login == phone)
                        {
                            val role = row.getString("Role")
                            val privilege = UserPrivileges.valueOf(role.toUpperCase(Locale.US))
                            listener.onResult(UserFormError.GetUserPrivileges, privilege)
                            break
                        }
                        row = it.nextRow
                    }
                }
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

        }

        connection?.let {
            val line = "SELECT * FROM $TABLE_USERS WHERE Phone='$phone'"
            val statement = it.createStatement()
            statement.executeQuery(line, callback)
        }
    }

    // Получение данных из таблицы specs_table
    fun getListSpecs(listener: UserFormListener)
    {
        val callback = object : IResultInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun executionComplete(p0: ResultSet?) {
                p0?.let {
                    var row = it.nextRow
                    val list: ArrayList<SpecialistContainer> = ArrayList()
                    while (row != null)
                    {
                        val spec = SpecialistContainer()
                        spec.lastName = String(hexToBytes(row.getString("LastName")), Charsets.UTF_8)
                        spec.firstName = String(hexToBytes(row.getString("FirstName")), Charsets.UTF_8)
                        spec.middleName = String(hexToBytes(row.getString("MiddleName")), Charsets.UTF_8)
                        spec.biography = String(hexToBytes(row.getString("Biography")), Charsets.UTF_8)
                        spec.phone = row.getString("Phone")
                        list.add(spec)
                        row = it.nextRow
                    }
                    listener.onResult(UserFormError.GetAllSpecs, list)
                }
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

        }

        connection?.let {
            val line = "SELECT * FROM $TABLE_SPECS"
            val statement = it.createStatement()
            statement.executeQuery(line, callback)
        }
    }

    // Запись на массаж
    fun recordUser(
            fromUser: String,
            toUser: String,
            timestamp: String,
            listener: UserFormListener?)
    {
        val callback = object : IConnectionInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.FormCreateFailure, null)
            }

            override fun actionCompleted() {
                logI("recordUser success!")
                listener?.onResult(UserFormError.FormCreated, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener?.onResult(UserFormError.FormCreateFailure, null)
            }
        }

        connection?.let {
            val id = UUID.randomUUID().toString()
            val line = "INSERT INTO $TABLE_RECORDS (RecId, FromUser, ToUser, TimeStamp) VALUES ('$id', '$fromUser','$toUser','$timestamp')"
            val statement = it.createStatement()
            statement.execute(line, callback)
        }
    }

    fun getAllRecords(listener: UserFormListener)
    {
        val callback = object : IResultInterface
        {
            override fun handleMySQLConnException(p0: MySQLConnException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleException(p0: java.lang.Exception?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleIOException(p0: IOException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun executionComplete(p0: ResultSet?) {
                p0?.let {
                    var row = it.nextRow
                    val list: ArrayList<RecordContainer> = ArrayList()
                    while (row != null)
                    {
                        val record = RecordContainer()
                        record.recId = row.getString("RecId")
                        record.fromUser = row.getString("FromUser")
                        record.toUser = row.getString("ToUser")
                        record.timestamp = row.getString("TimeStamp")
                        list.add(record)
                        row = it.nextRow
                    }
                    listener.onResult(UserFormError.GetAllRecords, list)
                }
            }

            override fun handleMySQLException(p0: MySQLException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

            override fun handleInvalidSQLPacketException(p0: InvalidSQLPacketException?) {
                logE(p0.toString())
                listener.onResult(UserFormError.ConnectFailure, null)
            }

        }

        connection?.let {
            val line = "SELECT * FROM $TABLE_RECORDS"
            val statement = it.createStatement()
            statement.executeQuery(line, callback)
        }
    }
}