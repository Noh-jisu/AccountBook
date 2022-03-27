# 간단한 입/출내역 가계부 만들기
- Android를 공부하면서 배운 기능을 활용한 간단한 가계부어플 만들기
- SQLite를 활용한 데이터 저장 

<br>

## 목차
### 1. 기능 요약
### 2. 화면구성 및 상세기능
### 3. 오류해결
### 4. 느낀점 및 소감

<br>

## 1. 기능요약
- 가계부 작성하기
  - 수입 / 지출 선택(RadioButton)
  - 날짜 선택(DatePicker)
  - 제목 / 금액 / 내용작성(EditText)
- 내역조회
  - 조회할 카테고리 선택(Spinner)
  - 검색된 내용 표기(RecyclerView)
- 기간검색
  - 시작 및 종료날짜 선택(DatePicker)
  - 검색된 내용 표기(RecyclerView)
  - 총 합 수입/지출 표기(TextView)

<br>

## 3. 화면구성 및 상세 기능

<br>

### 1.홈화면<br>
![Screenshot_20220327-135455_accountbook](https://user-images.githubusercontent.com/61276416/160267283-18d77090-f5ad-4ff0-b486-70f744e6f3be.jpg)

<br>

### 2.가계부 작성<br>
![Screenshot_20220327-115857_accountbook](https://user-images.githubusercontent.com/61276416/160267362-63c44141-15e0-4700-b0fc-5cc1cf9951d3.jpg)
- 1번 : 수입 / 지출 선택 (RadioButton)
```
// 라디오버튼 선택
b.radioGroup.setOnCheckedChangeListener { _, checkedId ->
    when(checkedId){  // 해당 RadioButton 선택시 글로벌변수의 값을 지정
        R.id.income -> {
            addType = "수입"
        }
        R.id.expense -> {
            addType = "지출"
        }
    }
}
```

- 2번 : 날짜 선택
```
// 날짜선택
var month = (b.datePicker.month+1).toString()
var day = b.datePicker.dayOfMonth.toString()
if (month.toInt() in 1..9){ // 선택한 날짜의 월이 한자리일 경우 0을 붙인다
    month = "0$month"
}
if (day.toInt() in 1..9){ // 선택한 날짜의 일이 한자리일 경우 0을 붙인다
    day = "0$day"
}
// 글로벌 변수에 선택한 날짜를 String형태로 값을 지정
date = b.datePicker.year.toString() + month + day
```

- 3번 : 작성하기
```
fun addDB(){
    var dbHelper = DBHelper(this, "account.db", null, 1)

    var database = dbHelper.writableDatabase    //이걸 해야 db가 들어감

    // 공란 체크
    if(addType == ""){
        println("addType : " + addType)
        AlertDialog.Builder(this@AddActivity)
            .setMessage("수입/지출 선택은 필수항목입니다.")
            .setCancelable(false)
            .setPositiveButton("확인", DialogInterface.OnClickListener{ dialog, whitch->
            }).show()
    }else if (title == ""){
        AlertDialog.Builder(this@AddActivity)
            .setMessage("제목을 입력해주세요.")
            .setCancelable(false)
            .setPositiveButton("확인", DialogInterface.OnClickListener{ dialog, whitch->
            }).show()
    }else if(date == ""){
        AlertDialog.Builder(this@AddActivity)
            .setMessage("날짜를 선택해주세요.")
            .setCancelable(false)
            .setPositiveButton("확인", DialogInterface.OnClickListener{ dialog, whitch->
            }).show()
    }else if(price == ""){
        AlertDialog.Builder(this@AddActivity)
            .setMessage("금액을 입력해주세요.")
            .setCancelable(false)
            .setPositiveButton("확인", DialogInterface.OnClickListener{ dialog, whitch->
            }).show()
    }else{  // 공란 없을시 실행되는 부분
    // 선택 및 입력한 값으로 지정된 글로벌변수를 SQLite DB에 저장
        dbHelper.insert(database, addType, title, date.toInt(), price, content)
        AlertDialog.Builder(this@AddActivity)
            .setMessage("등록이 완료되었습니다!")
            .setCancelable(false)
            .setPositiveButton("확인", DialogInterface.OnClickListener{ dialog, whitch->
                val addToMain = Intent(this,MainActivity::class.java)
                startActivity(addToMain)  // 확인버튼을 누르면 메인으로 이동
            }).show()
    }
}
```

### 3.내역조회
![Screenshot_20220327-141353_accountbook](https://user-images.githubusercontent.com/61276416/160267743-513ff0fe-ea16-4451-8e3e-04124da1c966.jpg)
- 1번 : 카테고리선택(Spinner)
```
b.goSearchBtn.setOnClickListener {
    when (selectSpinner) {
        "입/지출" -> {
            val accountInfo = dbHelper!!.selectType(accountData, b.searchBar.text.toString())
            accountList = accountInfo as ArrayList<accountVo>
            val accountAdapter = CustomAdapter(this, accountList)
            b.recyclerView.adapter = accountAdapter

            val layout = LinearLayoutManager(this)
            b.recyclerView.layoutManager = layout

            b.recyclerView.setHasFixedSize(true)
        }
        "제목" -> {
            val accountInfo = dbHelper!!.selectTitle(accountData, b.searchBar.text.toString())
            accountList = accountInfo as ArrayList<accountVo>
            val accountAdapter = CustomAdapter(this, accountList)
            b.recyclerView.adapter = accountAdapter

            val layout = LinearLayoutManager(this)
            b.recyclerView.layoutManager = layout

            b.recyclerView.setHasFixedSize(true)
        }
        "내용" -> {
            val accountInfo = dbHelper!!.selectContent(accountData, b.searchBar.text.toString())
            accountList = accountInfo as ArrayList<accountVo>
            val accountAdapter = CustomAdapter(this, accountList)
            b.recyclerView.adapter = accountAdapter

            val layout = LinearLayoutManager(this)
            b.recyclerView.layoutManager = layout

            b.recyclerView.setHasFixedSize(true)
        }
    }
}
```
선택된 카테고리마다 다른 쿼리문 적용<br>

- 2번 : 쿼리문에서 LIKE를 사용하여 해당 문자 및 단어가 포함된 모든 내역 출력
```
// 제목으로 조회
@SuppressLint("Range")
fun selectTitle(db:SQLiteDatabase, title:String) : MutableList<accountVo>{
    var sql = "SELECT * FROM ACCOUNTBOOK WHERE TITLE LIKE '%${title}%'"

    var result = db.rawQuery(sql, null)

    var account:accountVo? = null
    var accountList:MutableList<accountVo> = mutableListOf<accountVo>()
    while (result.moveToNext()){
        account = accountVo(
            result.getString(result.getColumnIndex("TYPE")),
            result.getString(result.getColumnIndex("TITLE")),
            result.getInt(result.getColumnIndex("DATE")),
            result.getString(result.getColumnIndex("PRICE")),
            result.getString(result.getColumnIndex("CONTENT")))
        accountList.add(account)
    }
    return  accountList
}
```

### 4. 기간조회
![Screenshot_20220327-142158_accountbook](https://user-images.githubusercontent.com/61276416/160268009-afbb0a4f-895d-42de-b4fe-6d44fb81c6e6.jpg)
- 1번 : 날짜 선택
```
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
```

<br>

- 2번 : 총 입 / 지출내역 합계
```
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
```

## 4. 오류해결

<br>

### 1. onCreate의 특징
 - 문제점 : onCreate()는 앱이 실행될 때 한번만 실행이 되는 부분으로 앱에서 DatePicker의 값을 바꿔도 새로운 값이 글로벌 변수에 저장이 되지 않는 문제
 - 해결방법 : setOnDateChangedListener를 사용해 DatePicker의 선택 날짜에 따라 변수값도 바뀌게 설정한다
![image](https://user-images.githubusercontent.com/61276416/160268190-2cd75842-e305-45cc-89b3-bf1205c145d8.png)
![image](https://user-images.githubusercontent.com/61276416/160268193-4b6ddf2e-17a7-4952-bd94-7fcb08013992.png)

<br>

### 2. 기본날짜 초기화
- 문제점 : DatePicker에서 선택한 날짜를 글로벌변수에 String타입으로 저장하기 이전에 초기화상태("")로 둔 상태.<br>
  DatePicker를 건드리지 않고 표기되는 날짜로 조회 했을경우 글로벌변수의 값이 빈값("")이라서 날짜계산 오류발생
- 해결방법 : 글로벌 변수를 선언과 동시에 오늘날짜로 초기화해서 사용한다
![image](https://user-images.githubusercontent.com/61276416/160268278-5c1158e4-4da3-4523-8470-bd91e587b874.png)


<br>

## 5. 느낀점 및 소감
- onCreate의 특징에 대해 이해하는 과정이었다
- 변수를 초기화하고 지정하는 과정에서 다양한 방법을 배웠다.<br>
datePicker자체의 값을 오늘날짜로 지정하게 할 수도 있었지만, 나는 사용할 변수를 오늘날짜로 초기화해서 사용하였다.
