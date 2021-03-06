package com.example.accountbook

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.accountbook.databinding.ActivityDateSearchBinding
import java.time.LocalDate
import java.util.*
import kotlin.collections.ArrayList

class DateSearchActivity : AppCompatActivity() {

    val b by lazy { ActivityDateSearchBinding.inflate(layoutInflater) }

    private var accountList = arrayListOf<accountVo>()

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(b.root)

        // Datepicker를 선택하지 않아도 오늘날짜로 변수값 초기화
        val nowDate = LocalDate.now().toString()
        var nowDateStr:String = nowDate.replace("-","")

        // 시작날짜
        var startDate = nowDateStr
        // 종료날짜
        var endDate = nowDateStr

        // 시작날짜 변경
        b.starDate.setOnDateChangedListener { datePicker, year, month, date ->
            startDate = choiceDate(year, (month+1), date)
        }
        // 종료날짜 변경
        b.endDate.setOnDateChangedListener { datePicker, year, month, date ->
            endDate = choiceDate(year, (month+1), date)
        }

        // 조회하기 버튼 클릭시
        b.dateBetweenBtn.setOnClickListener {

            var dbHelper = DBHelper(this, "account.db", null, 1)
            var accountData = dbHelper.writableDatabase

            val accountInfo = dbHelper!!.selectDate(accountData, startDate.toInt(), endDate.toInt())
            accountList = accountInfo as ArrayList<accountVo>
            println("startDate : $startDate")
            println("endDate : $endDate")

            // recyclerView를 DB에서 가져온 리스트로 적용
            val accountAdapter = CustomAdapter(this, accountList)
            b.dateRecycler.adapter = accountAdapter

            val layout = LinearLayoutManager(this)
            b.dateRecycler.layoutManager = layout

            b.dateRecycler.setHasFixedSize(true)

            // 총 수입
            var totalIncome:Int = 0
            // 총 지출
            var totalExpense:Int = 0

            for(i in accountList.indices) {
                if (accountList[i].type == "지출") {  // 내역이 지출인 경우 '총 지출'에 더하기
                    totalExpense += accountList[i].price!!.toInt()
                } else {    // 내역이 수입인 경우 '총 수입'에 더하기
                    totalIncome += accountList[i].price!!.toInt()
                }
            }
            // 반복문을 통해 가져온 총 입/지출 내역을 표시
            b.totalIncom.text = totalIncome.toString()
            b.totalExpense.text = totalExpense.toString()
            b.totalPrice.text = (totalIncome - totalExpense).toString()
        }

        b.dateToMain.setOnClickListener {
            val goMain = Intent(this,MainActivity::class.java)
            startActivity(goMain)
        }
    }

    // 날짜선택 함수
    private fun choiceDate(year:Int, month:Int, day:Int) : String{
        var Month = (month).toString()
        var Day = day.toString()
        if (Month.toInt() in 1..9){
            Month = "0$Month"
        }
        if (Day.toInt() in 1..9){
            Day = "0$Day"
        }
        return year.toString() + Month + Day

    }
}
